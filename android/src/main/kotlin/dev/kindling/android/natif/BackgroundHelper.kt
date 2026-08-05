package dev.kindling.android.natif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────
//  BackgroundMode — which strategy to use
// ─────────────────────────────────────────────

/**
 * Describes which background execution strategy fits your use case.
 *
 * | Mode                  | Survives app kill | Visible to user | Best for                        |
 * |-----------------------|-------------------|-----------------|----------------------------------|
 * | [Coroutine]           | ❌                | ❌              | Short async work while app is open |
 * | [WorkManagerOnce]     | ✅                | ❌              | Deferred one-shot tasks          |
 * | [WorkManagerPeriodic]  | ✅                | ❌              | Recurring background sync        |
 * | [WorkManagerScheduled] | ✅                | ❌              | Calendar-based (every Friday 7PM) |
 * | [Foreground]          | ✅                | ✅ (notif)      | Music, GPS, long downloads       |
 */
sealed class BackgroundMode {

    /** Coroutine tied to a [CoroutineScope] — cancelled when scope is cancelled. */
    data class Coroutine(
        val scope: CoroutineScope,
        val dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
    ) : BackgroundMode()

    /** WorkManager one-time task — survives app kill, runs once. */
    data class WorkManagerOnce(
        val uniqueName: String,
        val policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP,
        val constraints: Constraints   = Constraints.NONE,
        val inputData: Map<String, Any> = emptyMap()
    ) : BackgroundMode()


    /** WorkManager calendar-scheduled task — runs at a specific day/time, self-reschedules. */
    data class WorkManagerScheduled(
        val uniqueName: String,
        val dayOfWeek: java.time.DayOfWeek,
        val hour: Int,
        val minute: Int                        = 0,
        val policy: ExistingWorkPolicy         = ExistingWorkPolicy.REPLACE,
        val constraints: Constraints           = Constraints.NONE
    ) : BackgroundMode()

    /** WorkManager periodic task — survives app kill, repeats on interval. */
    data class WorkManagerPeriodic(
        val uniqueName: String,
        val intervalMinutes: Long,
        val policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
        val constraints: Constraints           = Constraints.NONE,
        val inputData: Map<String, Any>        = emptyMap()
    ) : BackgroundMode()

    /** Foreground service with a visible notification — survives app kill. */
    data class Foreground(
        val channelId: String,
        val channelName: String,
        val notificationId: Int    = 1,
        val title: String,
        val text: String           = "",
        @DrawableRes val icon: Int
    ) : BackgroundMode()
}

// ─────────────────────────────────────────────
//  BackgroundTask — what to run
// ─────────────────────────────────────────────

/**
 * The actual work to execute in the background.
 * Always a suspend lambda so it works uniformly across all modes.
 */
typealias BackgroundTask = suspend () -> Unit

// ─────────────────────────────────────────────
//  KWorker — reusable WorkManager worker
// ─────────────────────────────────────────────

/**
 * Generic [CoroutineWorker] used internally by [BackgroundHelper].
 * Register your task via [BackgroundHelper.registerWorker] before scheduling.
 */
class KWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val tag  = tags.firstOrNull { it.startsWith(TAG_PREFIX) } ?: return Result.failure()
        val key  = tag.removePrefix(TAG_PREFIX)
        val task = registry[key] ?: return Result.failure()
        return runCatching { task() }
            .fold(onSuccess = { Result.success() }, onFailure = { Result.failure() })
    }

    companion object {
        internal const val TAG_PREFIX = "kindling_worker_"
        internal val registry = mutableMapOf<String, BackgroundTask>()
    }
}

// ─────────────────────────────────────────────
//  KForegroundService — reusable foreground service
// ─────────────────────────────────────────────

/**
 * Generic foreground [Service] used internally by [BackgroundHelper].
 * Task is injected via [BackgroundHelper.startForeground].
 */
class KForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val key = intent?.getStringExtra(EXTRA_TASK_KEY) ?: return START_NOT_STICKY

        val channelId   = intent.getStringExtra(EXTRA_CHANNEL_ID)   ?: "kindling_fg"
        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: "Background"
        val notifId     = intent.getIntExtra(EXTRA_NOTIF_ID, 1)
        val title       = intent.getStringExtra(EXTRA_TITLE)        ?: ""
        val text        = intent.getStringExtra(EXTRA_TEXT)         ?: ""
        val iconRes     = intent.getIntExtra(EXTRA_ICON, 0)

        // Create notification channel (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .apply { if (iconRes != 0) setSmallIcon(iconRes) }
            .setOngoing(true)
            .build()

        startForeground(notifId, notification)

        val task = KWorker.registry[key]
        if (task != null) {
            scope.launch {
                runCatching { task() }
                stopSelf()
            }
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        internal const val EXTRA_TASK_KEY    = "task_key"
        internal const val EXTRA_CHANNEL_ID  = "channel_id"
        internal const val EXTRA_CHANNEL_NAME = "channel_name"
        internal const val EXTRA_NOTIF_ID    = "notif_id"
        internal const val EXTRA_TITLE       = "title"
        internal const val EXTRA_TEXT        = "text"
        internal const val EXTRA_ICON        = "icon"
    }
}

// ─────────────────────────────────────────────
//  BackgroundHelper
// ─────────────────────────────────────────────

/**
 * Unified helper for running tasks in the background.
 *
 * Picks the right Android mechanism automatically based on [BackgroundMode]:
 *
 * ```kotlin
 * // 1. Short async work tied to a ViewModel
 * backgroundHelper.run(
 *     mode = BackgroundMode.Coroutine(viewModelScope),
 *     task = { fetchData() }
 * )
 *
 * // 2. One-shot deferred task (survives app kill)
 * backgroundHelper.registerWorker("sync") { syncContacts() }
 * backgroundHelper.run(
 *     mode = BackgroundMode.WorkManagerOnce(
 *         uniqueName  = "sync",
 *         constraints = Constraints.Builder()
 *             .setRequiredNetworkType(NetworkType.CONNECTED)
 *             .build()
 *     )
 * )
 *
 * // 3. Periodic sync every 15 minutes
 * backgroundHelper.registerWorker("periodic-sync") { syncData() }
 * backgroundHelper.run(
 *     mode = BackgroundMode.WorkManagerPeriodic(
 *         uniqueName      = "periodic-sync",
 *         intervalMinutes = 15
 *     )
 * )
 *
 * // 4. Long-running foreground service (music, GPS, download)
 * backgroundHelper.registerWorker("download") { downloadFile() }
 * backgroundHelper.run(
 *     mode = BackgroundMode.Foreground(
 *         channelId   = "downloads",
 *         channelName = "Downloads",
 *         title       = "Downloading…",
 *         icon        = R.drawable.ic_download
 *     ),
 *     taskKey = "download"
 * )
 * ```
 *
 * Register in Koin:
 * ```kotlin
 * single { BackgroundHelper(androidContext()) }
 * ```
 *
 * Declare in AndroidManifest.xml:
 * ```xml
 * <service
 *     android:name="dev.kindling.android.natif.KForegroundService"
 *     android:foregroundServiceType="dataSync"
 *     android:exported="false" />
 *
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
 * ```
 */
class BackgroundHelper(private val context: Context) {

    private val appContext   = context.applicationContext
    private val workManager  = WorkManager.getInstance(appContext)

    // ── Worker registry ───────────────────────────────────────────────────────

    /**
     * Registers a suspend [task] under a [key] so it can be referenced by
     * [BackgroundMode.WorkManagerOnce], [BackgroundMode.WorkManagerPeriodic],
     * or [BackgroundMode.Foreground].
     *
     * Call this before [run] — typically in Application.onCreate or a Koin module.
     */
    fun registerWorker(key: String, task: BackgroundTask) {
        KWorker.registry[key] = task
    }

    // ── Main entry point ──────────────────────────────────────────────────────

    /**
     * Runs [task] (or the pre-registered worker at [taskKey]) using the given [mode].
     *
     * @param mode     Which background strategy to use — see [BackgroundMode].
     * @param task     Inline task for [BackgroundMode.Coroutine] (ignored for other modes).
     * @param taskKey  Key of a pre-registered worker for WorkManager / Foreground modes.
     *                 Defaults to the [BackgroundMode.WorkManagerOnce.uniqueName] or
     *                 [BackgroundMode.WorkManagerPeriodic.uniqueName] when applicable.
     */
    fun run(
        mode: BackgroundMode,
        task: BackgroundTask? = null,
        taskKey: String? = null
    ) {
        when (mode) {
            is BackgroundMode.Coroutine -> {
                requireNotNull(task) { "BackgroundMode.Coroutine requires a task lambda." }
                mode.scope.launch(mode.dispatcher) { task() }
            }

            is BackgroundMode.WorkManagerOnce -> {
                val key = taskKey ?: mode.uniqueName
                val request = OneTimeWorkRequestBuilder<KWorker>()
                    .addTag("${KWorker.TAG_PREFIX}$key")
                    .setConstraints(mode.constraints)
                    .setInputData(workDataOf(*mode.inputData.entries
                        .map { it.key to it.value }.toTypedArray()))
                    .build()
                workManager.enqueueUniqueWork(mode.uniqueName, mode.policy, request)
            }

            is BackgroundMode.WorkManagerPeriodic -> {
                val key = taskKey ?: mode.uniqueName
                val request = PeriodicWorkRequestBuilder<KWorker>(
                    mode.intervalMinutes, TimeUnit.MINUTES
                )
                    .addTag("${KWorker.TAG_PREFIX}$key")
                    .setConstraints(mode.constraints)
                    .setInputData(workDataOf(*mode.inputData.entries
                        .map { it.key to it.value }.toTypedArray()))
                    .build()
                workManager.enqueueUniquePeriodicWork(
                    mode.uniqueName, mode.policy, request
                )
            }

            is BackgroundMode.Foreground -> {
                val key = requireNotNull(taskKey) {
                    "BackgroundMode.Foreground requires a taskKey referencing a registered worker."
                }
                val intent = Intent(appContext, KForegroundService::class.java).apply {
                    putExtra(KForegroundService.EXTRA_TASK_KEY,    key)
                    putExtra(KForegroundService.EXTRA_CHANNEL_ID,  mode.channelId)
                    putExtra(KForegroundService.EXTRA_CHANNEL_NAME, mode.channelName)
                    putExtra(KForegroundService.EXTRA_NOTIF_ID,    mode.notificationId)
                    putExtra(KForegroundService.EXTRA_TITLE,       mode.title)
                    putExtra(KForegroundService.EXTRA_TEXT,        mode.text)
                    putExtra(KForegroundService.EXTRA_ICON,        mode.icon)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(intent)
                } else {
                    appContext.startService(intent)
                }
            }

            is BackgroundMode.WorkManagerScheduled -> {
                scheduleNext(mode, taskKey ?: mode.uniqueName)
            }
        }
    }


    /**
     * Schedules the next occurrence of a [WorkManagerScheduled] task.
     * Called automatically by [run] — call it again inside your worker
     * to re-schedule for the following week:
     *
     * ```kotlin
     * backgroundHelper.registerWorker("weekly-report") {
     *     generateReport()
     *     backgroundHelper.scheduleNext(
     *         mode    = BackgroundMode.WorkManagerScheduled("weekly-report", DayOfWeek.FRIDAY, hour = 19),
     *         taskKey = "weekly-report"
     *     )
     * }
     * backgroundHelper.run(
     *     mode    = BackgroundMode.WorkManagerScheduled("weekly-report", DayOfWeek.FRIDAY, hour = 19)
     * )
     * ```
     */
    fun scheduleNext(mode: BackgroundMode.WorkManagerScheduled, taskKey: String) {
        val delayMs = delayUntilNext(mode.dayOfWeek, mode.hour, mode.minute)
        val request = OneTimeWorkRequestBuilder<KWorker>()
            .addTag("${KWorker.TAG_PREFIX}$taskKey")
            .setConstraints(mode.constraints)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(mode.uniqueName, mode.policy, request)
    }

    // ── Control ───────────────────────────────────────────────────────────────

    /** Cancels a scheduled WorkManager task by [uniqueName]. */
    fun cancel(uniqueName: String) {
        workManager.cancelUniqueWork(uniqueName)
    }

    /** Stops the foreground service if running. */
    fun stopForeground() {
        appContext.stopService(Intent(appContext, KForegroundService::class.java))
    }

    /** Returns the current [WorkInfo.State] of a WorkManager task, or null if not found. */
    suspend fun stateOf(uniqueName: String): WorkInfo.State? =
        workManager.getWorkInfosForUniqueWork(uniqueName).get()
            .firstOrNull()?.state

    companion object {
        /** Global registry of tasks to run on device boot. Add entries in Application.onCreate. */
        val bootTasks = mutableListOf<BootTask>()

        /** Registers a [BootTask] to be run by [KBootReceiver] on device boot. */
        fun addBootTask(task: BootTask) { bootTasks.add(task) }
    }
}


// ─────────────────────────────────────────────
//  Scheduled worker support
// ─────────────────────────────────────────────

/**
 * Calculates the delay in milliseconds until the next occurrence of
 * [dayOfWeek] at [hour]:[minute] in the system default timezone.
 *
 * If the target time is in the past this week, it jumps to next week.
 */
internal fun delayUntilNext(
    dayOfWeek: java.time.DayOfWeek,
    hour: Int,
    minute: Int
): Long {
    val now    = java.time.ZonedDateTime.now()
    var target = now
        .with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek))
        .withHour(hour)
        .withMinute(minute)
        .withSecond(0)
        .withNano(0)

    // If target is in the past (same day but earlier time), move to next week
    if (!target.isAfter(now)) {
        target = target.plusWeeks(1)
    }

    return java.time.Duration.between(now, target).toMillis()
}


// ─────────────────────────────────────────────
//  Boot receiver
// ─────────────────────────────────────────────

/**
 * Descriptor for a task to run automatically on device boot.
 *
 * @param taskKey       Key of a pre-registered worker (via [BackgroundHelper.registerWorker]).
 * @param mode          Which strategy to use after boot — defaults to [BackgroundMode.WorkManagerOnce].
 *                      Use [BackgroundMode.WorkManagerScheduled] to resume a weekly schedule after reboot.
 * @param rescheduleScheduled If the task was a [BackgroundMode.WorkManagerScheduled], pass the
 *                      original mode here so the schedule is re-registered correctly on boot.
 */
data class BootTask(
    val taskKey: String,
    val mode: BackgroundMode = BackgroundMode.WorkManagerOnce(uniqueName = taskKey),
    val rescheduleScheduled: BackgroundMode.WorkManagerScheduled? = null
)

/**
 * BroadcastReceiver that runs registered [BootTask]s on device boot.
 *
 * Declare in AndroidManifest.xml:
 * ```xml
 * <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 *
 * <receiver
 *     android:name="dev.kindling.android.natif.KBootReceiver"
 *     android:exported="true"
 *     android:enabled="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.BOOT_COMPLETED" />
 *         <action android:name="android.intent.action.LOCKED_BOOT_COMPLETED" />
 *     </intent-filter>
 * </receiver>
 * ```
 *
 * Register tasks in your [Application.onCreate] **before** boot fires —
 * i.e. always register on every app start, not just the first time:
 * ```kotlin
 * // Application.onCreate
 * BackgroundHelper.addBootTask(
 *     BootTask(
 *         taskKey = "weekly-report",
 *         rescheduleScheduled = BackgroundMode.WorkManagerScheduled(
 *             uniqueName = "weekly-report",
 *             dayOfWeek  = DayOfWeek.FRIDAY,
 *             hour       = 19
 *         )
 *     )
 * )
 *
 * // Or a simple one-shot on boot:
 * BackgroundHelper.addBootTask(
 *     BootTask(
 *         taskKey = "sync-on-boot",
 *         mode    = BackgroundMode.WorkManagerOnce(uniqueName = "sync-on-boot")
 *     )
 * )
 * ```
 *
 * Note: [LOCKED_BOOT_COMPLETED] fires even before the user unlocks — only
 * use it for tasks that don't need credential-encrypted storage.
 */
class KBootReceiver : android.content.BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED") return

        val helper = BackgroundHelper(context)

        // Re-register all worker tasks so KWorker.registry is populated
        BackgroundHelper.bootTasks.forEach { task ->
            KWorker.registry[task.taskKey]?.let { /* already registered */ }
        }

        BackgroundHelper.bootTasks.forEach { task ->
            when {
                // Resume a scheduled (calendar-based) task
                task.rescheduleScheduled != null ->
                    helper.scheduleNext(task.rescheduleScheduled, task.taskKey)

                // Run any other mode normally
                else -> helper.run(mode = task.mode, taskKey = task.taskKey)
            }
        }
    }
}