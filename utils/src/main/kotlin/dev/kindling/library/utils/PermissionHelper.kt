package dev.kindling.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// ─────────────────────────────────────────────────────────────────────────────
//  PermissionResult
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The outcome of a permission request.
 */
sealed class PermissionResult {
    /** The user granted the permission. */
    data object Granted : PermissionResult()

    /** The user denied the permission. */
    data class Denied(
        /** `true` when "Don't ask again" was selected; direct the user to Settings. */
        val isPermanent: Boolean,
    ) : PermissionResult()
}

// ─────────────────────────────────────────────────────────────────────────────
//  MultiPermissionResult
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The outcome of a multi-permission request — one entry per requested permission.
 */
data class MultiPermissionResult(
    val results: Map<String, PermissionResult>,
) {
    /** `true` when every requested permission was granted. */
    val allGranted: Boolean get() = results.values.all { it is PermissionResult.Granted }

    /** The subset of permissions that were denied. */
    val denied: List<String> get() = results
        .filterValues { it is PermissionResult.Denied }
        .keys.toList()

    /** The subset of permissions permanently denied ("Don't ask again"). */
    val permanentlyDenied: List<String> get() = results
        .filterValues { (it as? PermissionResult.Denied)?.isPermanent == true }
        .keys.toList()
}

// ─────────────────────────────────────────────────────────────────────────────
//  PermissionHelper
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Coroutine-friendly wrapper for Android runtime permissions.
 *
 * Android equivalent of the `usePermission` React hook.
 *
 * ### Setup — in your Activity or Fragment:
 * ```kotlin
 * // FragmentActivity:
 * val helper = PermissionHelper.from(this)
 *
 * // Fragment:
 * val helper = PermissionHelper.from(this)
 * ```
 *
 * ### Single permission:
 * ```kotlin
 * lifecycleScope.launch {
 *     when (helper.request(Manifest.permission.CAMERA)) {
 *         PermissionResult.Granted        -> openCamera()
 *         is PermissionResult.Denied      -> showRationale()
 *     }
 * }
 * ```
 *
 * ### Multiple permissions:
 * ```kotlin
 * lifecycleScope.launch {
 *     val result = helper.requestMultiple(
 *         Manifest.permission.READ_CONTACTS,
 *         Manifest.permission.WRITE_CONTACTS,
 *     )
 *     if (result.allGranted) syncContacts()
 *     else showDeniedMessage(result.denied)
 * }
 * ```
 *
 * ### Check without requesting:
 * ```kotlin
 * if (helper.isGranted(Manifest.permission.CAMERA)) openCamera()
 * ```
 */
class PermissionHelper private constructor(
    private val context: Context,
    private val singleLauncher: ActivityResultLauncher<String>,
    private val multiLauncher: ActivityResultLauncher<Array<String>>,
    private val shouldShowRationale: (permission: String) -> Boolean,
) {
    private var singleContinuation: ((PermissionResult) -> Unit)? = null
    private var multiContinuation: ((Map<String, Boolean>) -> Unit)? = null

    /** Returns `true` when [permission] is already granted. */
    fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED

    /** Returns `true` when ALL of [permissions] are already granted. */
    fun allGranted(vararg permissions: String): Boolean =
        permissions.all { isGranted(it) }

    /**
     * Requests a single [permission], suspending until the user responds.
     * If already granted, returns [PermissionResult.Granted] immediately.
     */
    suspend fun request(permission: String): PermissionResult {
        if (isGranted(permission)) return PermissionResult.Granted

        return suspendCancellableCoroutine { cont ->
            singleContinuation = { result -> cont.resume(result) }
            cont.invokeOnCancellation { singleContinuation = null }
            singleLauncher.launch(permission)
        }
    }

    /**
     * Requests multiple [permissions] at once, suspending until the user responds.
     * Permissions already granted are not re-requested.
     */
    suspend fun requestMultiple(vararg permissions: String): MultiPermissionResult {
        val alreadyGranted = permissions.filter { isGranted(it) }
        val toRequest = permissions.filter { !isGranted(it) }

        if (toRequest.isEmpty()) {
            return MultiPermissionResult(
                alreadyGranted.associateWith { PermissionResult.Granted }
            )
        }

        val rawResults: Map<String, Boolean> = suspendCancellableCoroutine { cont ->
            multiContinuation = { results -> cont.resume(results) }
            cont.invokeOnCancellation { multiContinuation = null }
            multiLauncher.launch(toRequest.toTypedArray())
        }

        val results = buildMap {
            alreadyGranted.forEach { put(it, PermissionResult.Granted) }
            rawResults.forEach { (perm, granted) ->
                put(
                    perm,
                    if (granted) PermissionResult.Granted
                    else PermissionResult.Denied(
                        isPermanent = !shouldShowRationale(perm)
                    )
                )
            }
        }
        return MultiPermissionResult(results)
    }

    // ── Internal callbacks invoked by the launchers ───────────────────────────

    internal fun onSingleResult(permission: String, granted: Boolean) {
        val result = if (granted) PermissionResult.Granted
        else PermissionResult.Denied(isPermanent = !shouldShowRationale(permission))
        singleContinuation?.invoke(result)
        singleContinuation = null
    }

    internal fun onMultiResult(results: Map<String, Boolean>) {
        multiContinuation?.invoke(results)
        multiContinuation = null
    }

    // ── Factories ─────────────────────────────────────────────────────────────

    companion object {

        /**
         * Creates a [PermissionHelper] bound to a [FragmentActivity].
         * Call this during `Activity.onCreate()` — before the activity starts.
         */
        fun from(activity: FragmentActivity): PermissionHelper {
            lateinit var helper: PermissionHelper

            val single = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                helper.onSingleResult(
                    // The permission string is not passed back by the contract;
                    // we recover it via the pending continuation closure above.
                    permission = "",
                    granted    = granted,
                )
            }

            val multi = activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                helper.onMultiResult(results)
            }

            helper = PermissionHelper(
                context              = activity,
                singleLauncher       = single,
                multiLauncher        = multi,
                shouldShowRationale  = { perm ->
                    activity.shouldShowRequestPermissionRationale(perm)
                },
            )
            return helper
        }

        /**
         * Creates a [PermissionHelper] bound to a [Fragment].
         * Call this during `Fragment.onAttach()` or before `Fragment.onStart()`.
         */
        fun from(fragment: Fragment): PermissionHelper {
            lateinit var helper: PermissionHelper

            val single = fragment.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                helper.onSingleResult(permission = "", granted = granted)
            }

            val multi = fragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                helper.onMultiResult(results)
            }

            helper = PermissionHelper(
                context              = fragment.requireContext(),
                singleLauncher       = single,
                multiLauncher        = multi,
                shouldShowRationale  = { perm ->
                    fragment.shouldShowRequestPermissionRationale(perm)
                },
            )
            return helper
        }
    }
}