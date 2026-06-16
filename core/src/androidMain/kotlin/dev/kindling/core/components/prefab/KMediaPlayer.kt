package dev.kindling.core.components.prefab

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
//  Design tokens — Aniyomi-inspired dark player
// ─────────────────────────────────────────────

private val PlayerBackground   = Color(0xFF000000)
private val PlayerSurface      = Color(0xFF1C1520)   // deep purple-black, like the sheets
private val PlayerAccent       = Color(0xFFE91E8C)   // hot pink — the brand colour in screenshots
private val PlayerAccentDim    = Color(0xFFE91E8C).copy(alpha = 0.18f)
private val PlayerOnSurface    = Color(0xFFFFFFFF)
private val PlayerMuted        = Color(0xFFFFFFFF).copy(alpha = 0.55f)
private val PlayerDivider      = Color(0xFFFFFFFF).copy(alpha = 0.08f)
private val SheetBackground    = Color(0xFF1C1520)
private val SheetSelected      = Color(0xFFE91E8C).copy(alpha = 0.15f)

// ─────────────────────────────────────────────
//  Data models
// ─────────────────────────────────────────────

data class KMediaEpisode(
    val number: Int,
    val title: String,
    val date: String,
    val availableTracks: List<String> = listOf("Sub", "Dub")
)

data class KMediaSubtitleTrack(
    val id: Int,
    val label: String,
    val isExternal: Boolean = false
)

data class KMediaAudioTrack(
    val id: Int,
    val label: String,
    val isExternal: Boolean = false
)

data class KMediaQualityOption(
    val id: Int,
    val label: String   // e.g. "1080p", "720p", "Auto"
)

data class KCustomSkipButton(
    val label: String,       // e.g. "+85 s"
    val offsetSeconds: Int
)

enum class KSubtitleFont { SansSerif, Serif, Monospace }
enum class KSubtitleBorderStyle { OutlineAndShadow, Outline, Shadow, None }

data class KSubtitleStyle(
    val font: KSubtitleFont = KSubtitleFont.SansSerif,
    val fontSize: Int = 55,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val borderStyle: KSubtitleBorderStyle = KSubtitleBorderStyle.OutlineAndShadow
)

data class KVideoFilters(
    val brightness: Float = 0f,
    val saturation: Float = 0f,
    val contrast: Float = 0f,
    val gamma: Float = 0f,
    val hue: Float = 0f
)

data class KPlayerState(
    // Playback
    val isPlaying: Boolean = false,
    val autoPlay: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speed: Float = 1.00f,
    // Tracks
    val subtitleTracks: List<KMediaSubtitleTrack> = emptyList(),
    val activeSubtitleIds: Set<Int> = emptySet(),
    val subtitleDelayMs: Int = 0,
    val subtitleStyle: KSubtitleStyle = KSubtitleStyle(),
    val audioTracks: List<KMediaAudioTrack> = emptyList(),
    val activeAudioId: Int = 1,
    val audioDelayMs: Int = 0,
    // Quality
    val qualityOptions: List<KMediaQualityOption> = emptyList(),
    val activeQualityId: Int = 0,
    // Hardware decode
    val decodeMode: KDecodeMode = KDecodeMode.HWPlus,
    val defaultStatsPage: Int = 0,   // 0 = off
    val customSkipButtons: List<KCustomSkipButton> = listOf(KCustomSkipButton("+85 s", 85)),
    // Filters
    val videoFilters: KVideoFilters = KVideoFilters(),
    // UI
    val isLocked: Boolean = false,
    val sleepTimerMinutes: Int = 0,
)

enum class KDecodeMode(val label: String) {
    Auto("Auto"), SW("SW"), HW("HW"), HWPlus("HW+")
}

// ─────────────────────────────────────────────
//  KMediaPlayer — root composable
// ─────────────────────────────────────────────

/**
 * Aniyomi / Mangayomi–style full-screen media player preset.
 *
 * Handles its own UI overlays (controls, drawers, dialogs). The caller is
 * responsible for rendering the actual video surface behind this composable.
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize().background(Color.Black)) {
 *     // Your video surface here
 *     AndroidView(factory = { PlayerView(it) })
 *
 *     KMediaPlayer(
 *         title         = "Beheneko: The Elf-Girl's Cat",
 *         subtitle      = "Episode 5: Elf Girl & Dragon Girl",
 *         episodes      = episodes,
 *         playerState   = state,
 *         onBack        = { navController.popBackStack() },
 *         onStateChange = { newState -> viewModel.update(newState) },
 *         onSeek        = { ms -> player.seekTo(ms) },
 *         onSkip        = { ms -> player.seekTo(player.currentPosition + ms * 1000L) },
 *         onEpisodeSelect = { ep -> viewModel.playEpisode(ep) }
 *     )
 * }
 * ```
 *
 * Native helpers used internally (inject via Koin or pass directly):
 * - [WindowHelper]     → Immersive / edge-to-edge mode
 * - [BrightnessHelper] → per-window brightness
 * - [VibrationHelper]  → haptic on seek
 * - [DisplayHelper]    → orientation / dp conversions
 */
@Composable
fun KMediaPlayer(
    title: String,
    subtitle: String,
    episodes: List<KMediaEpisode> = emptyList(),
    playerState: KPlayerState = KPlayerState(),
    onBack: () -> Unit = {},
    onStateChange: (KPlayerState) -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onSkip: (Int) -> Unit = {},
    onEpisodeSelect: (KMediaEpisode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // ── Local UI state ──────────────────────────────────────────────────────
    var controlsVisible by remember { mutableStateOf(true) }
    var showEpisodeDialog by remember { mutableStateOf(false) }
    var showSubtitleSheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var showPlusSheet by remember { mutableStateOf(false) }
    var showSubtitleDelayDialog by remember { mutableStateOf(false) }
    var showSubtitleStyleDialog by remember { mutableStateOf(false) }
    var showAudioDelayDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Auto-hide controls after 3 s
    LaunchedEffect(controlsVisible, playerState.isPlaying) {
        if (controlsVisible && playerState.isPlaying) {
            delay(3_000)
            controlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlayerBackground)
            .pointerInput(Unit) {
                detectTapGestures { controlsVisible = !controlsVisible }
            }
    ) {
        // ── Gradient overlays ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = controlsVisible,
            enter   = fadeIn(tween(200)),
            exit    = fadeOut(tween(200))
        ) {
            Box(Modifier.fillMaxSize()) {
                // Top gradient
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Black.copy(0.75f), Color.Transparent)
                            )
                        )
                )
                // Bottom gradient
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.80f))
                            )
                        )
                )
            }
        }

        // ── TOP BAR ───────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = controlsVisible && !playerState.isLocked,
            enter    = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it },
            exit     = fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            KMediaTopBar(
                title          = title,
                subtitle       = subtitle,
                autoPlay       = playerState.autoPlay,
                onBack         = onBack,
                onTitleClick   = { showEpisodeDialog = true },
                onAutoPlayToggle = { onStateChange(playerState.copy(autoPlay = !playerState.autoPlay)) },
                onSubtitleClick = { showSubtitleSheet = true },
                onAudioClick    = { showAudioSheet = true },
                onQualityClick  = { showQualitySheet = true },
                onPlusClick     = { showPlusSheet = true }
            )
        }

        // ── CENTRE CONTROLS ───────────────────────────────────────────────
        AnimatedVisibility(
            visible  = controlsVisible && !playerState.isLocked,
            enter    = fadeIn(tween(200)),
            exit     = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            KMediaCentreControls(
                isPlaying = playerState.isPlaying,
                onPrev    = { /* caller handles */ },
                onPlay    = { onStateChange(playerState.copy(isPlaying = !playerState.isPlaying)) },
                onNext    = { /* caller handles */ }
            )
        }

        // ── LOCK ICON (always visible when locked) ────────────────────────
        if (playerState.isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(0.5f))
                    .clickable { onStateChange(playerState.copy(isLocked = false)) }
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Unlock",
                    tint = PlayerOnSurface, modifier = Modifier.size(22.dp))
            }
        }

        // ── BOTTOM BAR ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = controlsVisible,
            enter    = fadeIn(tween(200)) + slideInVertically(tween(200)) { it },
            exit     = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            KMediaBottomBar(
                state          = playerState,
                onSeek         = onSeek,
                onSkip         = onSkip,
                onLockToggle   = { onStateChange(playerState.copy(isLocked = !playerState.isLocked)) },
                isLocked       = playerState.isLocked
            )
        }

        // ── EPISODE LIST DIALOG ───────────────────────────────────────────
        if (showEpisodeDialog) {
            KEpisodeListDialog(
                episodes      = episodes,
                currentTitle  = subtitle,
                onDismiss     = { showEpisodeDialog = false },
                onSelect      = { ep ->
                    showEpisodeDialog = false
                    onEpisodeSelect(ep)
                }
            )
        }

        // ── SUBTITLE SHEET ────────────────────────────────────────────────
        if (showSubtitleSheet) {
            KSubtitleSheet(
                tracks        = playerState.subtitleTracks,
                activeIds     = playerState.activeSubtitleIds,
                onDismiss     = { showSubtitleSheet = false },
                onToggle      = { id ->
                    val next = if (id in playerState.activeSubtitleIds)
                        playerState.activeSubtitleIds - id
                    else playerState.activeSubtitleIds + id
                    onStateChange(playerState.copy(activeSubtitleIds = next))
                },
                onDelayClick  = { showSubtitleSheet = false; showSubtitleDelayDialog = true },
                onPaletteClick = { showSubtitleSheet = false; showSubtitleStyleDialog = true },
                onAddExternal = { /* caller handles SAF pick */ }
            )
        }

        // ── AUDIO SHEET ───────────────────────────────────────────────────
        if (showAudioSheet) {
            KAudioSheet(
                tracks       = playerState.audioTracks,
                activeId     = playerState.activeAudioId,
                onDismiss    = { showAudioSheet = false },
                onSelect     = { id -> onStateChange(playerState.copy(activeAudioId = id)) },
                onDelayClick = { showAudioSheet = false; showAudioDelayDialog = true },
                onAddExternal = { /* caller handles SAF pick */ }
            )
        }

        // ── QUALITY SHEET ─────────────────────────────────────────────────
        if (showQualitySheet) {
            KQualitySheet(
                options   = playerState.qualityOptions,
                activeId  = playerState.activeQualityId,
                onDismiss = { showQualitySheet = false },
                onSelect  = { id -> onStateChange(playerState.copy(activeQualityId = id)) }
            )
        }

        // ── PLUS SHEET ────────────────────────────────────────────────────
        if (showPlusSheet) {
            KPlusSheet(
                state      = playerState,
                onDismiss  = { showPlusSheet = false },
                onStateChange = onStateChange,
                onSleepTimer  = { showPlusSheet = false; showSleepTimerDialog = true },
                onFilter      = { showPlusSheet = false; showFilterDialog = true }
            )
        }

        // ── SUBTITLE DELAY DIALOG ─────────────────────────────────────────
        if (showSubtitleDelayDialog) {
            KSubtitleDelayDialog(
                delayMs    = playerState.subtitleDelayMs,
                onDismiss  = { showSubtitleDelayDialog = false },
                onConfirm  = { ms -> onStateChange(playerState.copy(subtitleDelayMs = ms)) }
            )
        }

        // ── SUBTITLE STYLE DIALOG ─────────────────────────────────────────
        if (showSubtitleStyleDialog) {
            KSubtitleStyleDialog(
                style     = playerState.subtitleStyle,
                onDismiss = { showSubtitleStyleDialog = false },
                onChange  = { s -> onStateChange(playerState.copy(subtitleStyle = s)) }
            )
        }

        // ── AUDIO DELAY DIALOG ────────────────────────────────────────────
        if (showAudioDelayDialog) {
            KAudioDelayDialog(
                delayMs   = playerState.audioDelayMs,
                onDismiss = { showAudioDelayDialog = false },
                onConfirm = { ms -> onStateChange(playerState.copy(audioDelayMs = ms)) }
            )
        }

        // ── SLEEP TIMER DIALOG ────────────────────────────────────────────
        if (showSleepTimerDialog) {
            KSleepTimerDialog(
                currentMinutes = playerState.sleepTimerMinutes,
                onDismiss      = { showSleepTimerDialog = false },
                onConfirm      = { mins -> onStateChange(playerState.copy(sleepTimerMinutes = mins)) }
            )
        }

        // ── VIDEO FILTER DIALOG ───────────────────────────────────────────
        if (showFilterDialog) {
            KVideoFilterDialog(
                filters   = playerState.videoFilters,
                onDismiss = { showFilterDialog = false },
                onChange  = { f -> onStateChange(playerState.copy(videoFilters = f)) }
            )
        }
    }
}

// ─────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────

@Composable
private fun KMediaTopBar(
    title: String,
    subtitle: String,
    autoPlay: Boolean,
    onBack: () -> Unit,
    onTitleClick: () -> Unit,
    onAutoPlayToggle: () -> Unit,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit,
    onQualityClick: () -> Unit,
    onPlusClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                tint = PlayerOnSurface, modifier = Modifier.size(22.dp))
        }

        // Title block (tappable → episode list)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onTitleClick)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text       = title,
                color      = PlayerOnSurface,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 1
            )
            Text(
                text      = subtitle,
                color     = PlayerMuted,
                fontSize  = 12.sp,
                fontStyle = FontStyle.Italic,
                maxLines  = 1
            )
        }

        // Auto-play toggle
        Switch(
            checked         = autoPlay,
            onCheckedChange = { onAutoPlayToggle() },
            modifier        = Modifier.height(24.dp),
            colors          = SwitchDefaults.colors(
                checkedThumbColor       = PlayerOnSurface,
                checkedTrackColor       = PlayerAccent,
                uncheckedThumbColor     = PlayerMuted,
                uncheckedTrackColor     = Color.White.copy(0.20f)
            )
        )

        // Subtitle
        PlayerIconBtn(
            icon = Icons.Default.Subtitles,
            contentDescription = "Subtitles",
            onClick = onSubtitleClick
        )

        // Audio
        PlayerIconBtn(
            icon = Icons.Default.MusicNote,
            contentDescription = "Audio tracks",
            onClick = onAudioClick
        )

        // Quality (HQ badge style)
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, PlayerOnSurface.copy(0.6f), RoundedCornerShape(4.dp))
                .clickable(onClick = onQualityClick)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("HQ", color = PlayerOnSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // More (⋮)
        PlayerIconBtn(
            icon = Icons.Default.MoreVert,
            contentDescription = "More options",
            onClick = onPlusClick
        )
    }
}

// ─────────────────────────────────────────────
//  CENTRE CONTROLS  (prev / play / next)
// ─────────────────────────────────────────────

@Composable
private fun KMediaCentreControls(
    isPlaying: Boolean,
    onPrev: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(36.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Skip to previous episode
        IconButton(onClick = onPrev, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous",
                tint = PlayerOnSurface, modifier = Modifier.size(34.dp))
        }

        // Play / Pause  (larger)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint               = PlayerOnSurface,
                modifier           = Modifier.size(42.dp)
            )
        }

        // Skip to next episode
        IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next",
                tint = PlayerOnSurface, modifier = Modifier.size(34.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  BOTTOM BAR  (lock / rotation / speed | progress | custom-skip / pip / resize)
// ─────────────────────────────────────────────

@Composable
private fun KMediaBottomBar(
    state: KPlayerState,
    onSeek: (Long) -> Unit,
    onSkip: (Int) -> Unit,
    onLockToggle: () -> Unit,
    isLocked: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
    ) {
        // ── Row 1 : left icons + custom skip button ──────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lock
            PlayerIconBtn(
                icon               = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isLocked) "Unlock" else "Lock",
                onClick            = onLockToggle
            )
            // Rotation lock
            PlayerIconBtn(
                icon               = Icons.Default.ScreenRotation,
                contentDescription = "Rotation",
                onClick            = {}
            )
            // Speed label
            Text(
                text     = "${"%.2f".format(state.speed)}x",
                color    = PlayerOnSurface,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(Modifier.weight(1f))

            // Custom skip buttons (e.g. +85 s)
            state.customSkipButtons.forEach { btn ->
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(PlayerAccent)
                        .clickable { onSkip(btn.offsetSeconds) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(btn.label, color = PlayerOnSurface,
                        fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            // PiP
            PlayerIconBtn(
                icon               = Icons.Default.PictureInPicture,
                contentDescription = "Picture-in-picture",
                onClick            = {}
            )
            // Resize / aspect ratio
            PlayerIconBtn(
                icon               = Icons.Default.AspectRatio,
                contentDescription = "Resize",
                onClick            = {}
            )
        }

        // ── Progress row ─────────────────────────────────────────────────
        KMediaProgressBar(
            positionMs  = state.positionMs,
            durationMs  = state.durationMs,
            onSeek      = onSeek,
            modifier    = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────
//  PROGRESS BAR
// ─────────────────────────────────────────────

@Composable
private fun KMediaProgressBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs.toFloat() else 0f

    Row(
        modifier          = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = formatMs(positionMs),
            color    = PlayerOnSurface,
            fontSize = 12.sp
        )

        Slider(
            value          = progress,
            onValueChange  = { p -> onSeek((p * durationMs).toLong()) },
            modifier       = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors         = SliderDefaults.colors(
                thumbColor            = PlayerAccent,
                activeTrackColor      = PlayerAccent,
                inactiveTrackColor    = PlayerOnSurface.copy(0.30f)
            )
        )

        // Remaining as "+MM:SS"
        val remaining = durationMs - positionMs
        Text(
            text     = "+${formatMs(remaining)}",
            color    = PlayerOnSurface,
            fontSize = 12.sp
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000L
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

// ─────────────────────────────────────────────
//  EPISODE LIST DIALOG
// ─────────────────────────────────────────────

@Composable
private fun KEpisodeListDialog(
    episodes: List<KMediaEpisode>,
    currentTitle: String,
    onDismiss: () -> Unit,
    onSelect: (KMediaEpisode) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // The sheet itself sits right-of-centre (like in screenshots)
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.6f)
                    .fillMaxWidth(0.7f)
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SheetBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = {}
                    )
                    .padding(vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text     = "Épisodes",
                        color    = PlayerOnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    HorizontalDivider(color = PlayerDivider, modifier = Modifier.padding(vertical = 8.dp))
                    LazyColumn {
                        items(episodes) { ep ->
                            val isCurrent = ep.title == currentTitle.removePrefix("Episode ${ep.number}: ")
                                    || currentTitle.contains("Episode ${ep.number}")
                            EpisodeRow(
                                episode   = ep,
                                isCurrent = isCurrent,
                                onSelect  = { onSelect(ep) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: KMediaEpisode,
    isCurrent: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bookmark placeholder
        Icon(Icons.Default.BookmarkBorder, null,
            tint = PlayerMuted, modifier = Modifier.size(18.dp))
        // Download/offline placeholder
        Icon(Icons.Default.Download, null,
            tint = PlayerMuted, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text       = if (isCurrent) "Episode ${episode.number}: ${episode.title}" else "Episode ${episode.number}: ${episode.title}",
                color      = if (isCurrent) PlayerAccent else PlayerOnSurface,
                fontSize   = 13.sp,
                fontStyle  = if (isCurrent) FontStyle.Italic else FontStyle.Normal,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                maxLines   = 1
            )
            Text(
                text     = "${episode.date} • ${episode.availableTracks.joinToString(", ")}",
                color    = PlayerMuted,
                fontSize = 11.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
//  SUBTITLE SHEET
// ─────────────────────────────────────────────

@Composable
private fun KSubtitleSheet(
    tracks: List<KMediaSubtitleTrack>,
    activeIds: Set<Int>,
    onDismiss: () -> Unit,
    onToggle: (Int) -> Unit,
    onDelayClick: () -> Unit,
    onPaletteClick: () -> Unit,
    onAddExternal: () -> Unit
) {
    PlayerBottomSheet(onDismiss = onDismiss) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sous-titres", color = PlayerOnSurface, fontSize = 20.sp,
                fontWeight = FontWeight.Light, modifier = Modifier.weight(1f))
            // Palette
            SheetIconTextBtn(Icons.Default.Palette, "Palette", onClick = onPaletteClick)
            SheetIconTextBtn(Icons.Default.Timer, "Délai", onClick = onDelayClick)
        }
        HorizontalDivider(color = PlayerDivider, modifier = Modifier.padding(bottom = 8.dp))

        // Add external
        SheetAddRow("Ajouter des sous-titres externes", onClick = onAddExternal)

        // Tracks
        tracks.forEach { track ->
            val isActive = track.id in activeIds
            SheetCheckRow(
                label    = if (track.label.isNotBlank()) "#${track.id}: ${track.label}" else "#${track.id}:",
                checked  = isActive,
                trailing = if (isActive) "#${track.id}" else null,
                onClick  = { onToggle(track.id) }
            )
        }

        Spacer(Modifier.height(8.dp))
        // Info note
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Info, null, tint = PlayerMuted, modifier = Modifier.size(16.dp))
            Text(
                "Les sous-titres secondaires n'auront pas de style ASS/SSA.",
                color = PlayerMuted, fontSize = 12.sp, lineHeight = 16.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
//  AUDIO SHEET
// ─────────────────────────────────────────────

@Composable
private fun KAudioSheet(
    tracks: List<KMediaAudioTrack>,
    activeId: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onDelayClick: () -> Unit,
    onAddExternal: () -> Unit
) {
    PlayerBottomSheet(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Audio", color = PlayerOnSurface, fontSize = 20.sp,
                fontWeight = FontWeight.Light, modifier = Modifier.weight(1f))
            SheetIconTextBtn(Icons.Default.Timer, "Délai", onClick = onDelayClick)
        }
        HorizontalDivider(color = PlayerDivider, modifier = Modifier.padding(bottom = 8.dp))

        SheetAddRow("Ajouter des pistes audio externes", onClick = onAddExternal)

        // Disabled option
        SheetRadioRow(
            label    = "Désactivé",
            selected = activeId == -1,
            onClick  = { onSelect(-1) }
        )

        tracks.forEach { track ->
            SheetRadioRow(
                label    = "#${track.id}: ${track.label}",
                selected = track.id == activeId,
                onClick  = { onSelect(track.id) }
            )
        }
    }
}

// ─────────────────────────────────────────────
//  QUALITY SHEET
// ─────────────────────────────────────────────

@Composable
private fun KQualitySheet(
    options: List<KMediaQualityOption>,
    activeId: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    PlayerBottomSheet(onDismiss = onDismiss) {
        Text("Qualité", color = PlayerOnSurface, fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(bottom = 12.dp))
        HorizontalDivider(color = PlayerDivider, modifier = Modifier.padding(bottom = 8.dp))

        options.forEach { opt ->
            SheetRadioRow(
                label    = opt.label,
                selected = opt.id == activeId,
                onClick  = { onSelect(opt.id) }
            )
        }
    }
}

// ─────────────────────────────────────────────
//  PLUS SHEET
// ─────────────────────────────────────────────

@Composable
private fun KPlusSheet(
    state: KPlayerState,
    onDismiss: () -> Unit,
    onStateChange: (KPlayerState) -> Unit,
    onSleepTimer: () -> Unit,
    onFilter: () -> Unit
) {
    PlayerBottomSheet(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Plus", color = PlayerOnSurface, fontSize = 20.sp,
                fontWeight = FontWeight.Light, modifier = Modifier.weight(1f))
            SheetIconTextBtn(Icons.Default.Alarm, "Délai de mise en veille", onClick = onSleepTimer)
            SheetIconTextBtn(Icons.Default.Tune, "Filtres", onClick = onFilter)
        }
        HorizontalDivider(color = PlayerDivider, modifier = Modifier.padding(bottom = 16.dp))

        // Decode mode
        Text("Définir le mode de décodage matériel par défaut",
            color = PlayerOnSurface, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KDecodeMode.entries.forEach { mode ->
                val active = state.decodeMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) PlayerAccent else Color.Transparent)
                        .border(1.dp,
                            if (active) PlayerAccent else PlayerOnSurface.copy(0.5f),
                            RoundedCornerShape(8.dp))
                        .clickable { onStateChange(state.copy(decodeMode = mode)) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(mode.label, color = PlayerOnSurface, fontSize = 13.sp,
                        fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats page
        Text("Page des statistiques par défaut",
            color = PlayerOnSurface, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val options = listOf(0 to "Éteint") + (1..5).map { it to "Page $it" }
            options.forEach { (idx, label) ->
                val active = state.defaultStatsPage == idx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) PlayerAccent else Color.Transparent)
                        .border(1.dp,
                            if (active) PlayerAccent else PlayerOnSurface.copy(0.5f),
                            RoundedCornerShape(8.dp))
                        .clickable { onStateChange(state.copy(defaultStatsPage = idx)) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(label, color = PlayerOnSurface, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Custom skip buttons display
        Text("Boutons personnalisés", color = PlayerOnSurface, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.customSkipButtons.forEach { btn ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, PlayerOnSurface.copy(0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(btn.label, color = PlayerOnSurface, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  SUBTITLE DELAY DIALOG
// ─────────────────────────────────────────────

@Composable
private fun KSubtitleDelayDialog(
    delayMs: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var delay by remember { mutableStateOf(delayMs) }
    var speed by remember { mutableStateOf(1) }
    var trackType by remember { mutableStateOf("Primaire") }

    PlayerDialog(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Délai des sous-titres", color = PlayerOnSurface,
                fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            // Track type chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { trackType = if (trackType == "Primaire") "Secondaire" else "Primaire" }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(trackType, color = PlayerMuted, fontSize = 12.sp)
                Icon(Icons.Default.ArrowDropDown, null, tint = PlayerMuted,
                    modifier = Modifier.size(16.dp).align(Alignment.CenterEnd))
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = PlayerOnSurface)
            }
        }

        // Delay stepper
        DelayStepperRow(
            label     = "Délai",
            value     = delay,
            unit      = "ms",
            onDecrement = { delay -= 500 },
            onIncrement = { delay += 500 }
        )

        Spacer(Modifier.height(12.dp))

        // Speed stepper
        DelayStepperRow(
            label     = "Vitesse",
            value     = speed,
            unit      = null,
            onDecrement = { if (speed > 0) speed-- },
            onIncrement = { speed++ }
        )

        Spacer(Modifier.height(20.dp))

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlayerPinkBtn(text = "Voix entendue", modifier = Modifier.weight(1f),
                onClick = { onConfirm(delay) })
            PlayerPinkBtn(text = "Texte vu", modifier = Modifier.weight(1f),
                onClick = { onConfirm(delay) })
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlayerPinkBtn(text = "Définir par défaut", modifier = Modifier.weight(1f),
                onClick = { onConfirm(delay) })
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PlayerAccent)
                    .clickable { delay = 0; speed = 1 },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, null, tint = PlayerOnSurface,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun DelayStepperRow(
    label: String,
    value: Int,
    unit: String?,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Minus
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, PlayerMuted, CircleShape)
                .clickable(onClick = onDecrement),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Remove, null, tint = PlayerOnSurface, modifier = Modifier.size(16.dp))
        }
        // Field
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, PlayerMuted, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text   = if (unit != null) "$value     $unit" else "$value",
                color  = PlayerOnSurface,
                fontSize = 15.sp
            )
            if (unit != null) {
                Text(label, color = PlayerMuted, fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.TopStart).offset(y = (-10).dp))
            }
        }
        // Plus
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, PlayerMuted, CircleShape)
                .clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, null, tint = PlayerOnSurface, modifier = Modifier.size(16.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  SUBTITLE STYLE DIALOG
// ─────────────────────────────────────────────

@Composable
private fun KSubtitleStyleDialog(
    style: KSubtitleStyle,
    onDismiss: () -> Unit,
    onChange: (KSubtitleStyle) -> Unit
) {
    var current by remember { mutableStateOf(style) }

    PlayerDialog(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paramètre des sous-titres", color = PlayerOnSurface,
                fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = PlayerOnSurface)
            }
        }

        // Typography section header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TextFields, null, tint = PlayerMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Typographie", color = PlayerOnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, null, tint = PlayerMuted)
        }

        Spacer(Modifier.height(12.dp))

        // Bold / Italic / Alignment
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StyleToggleBtn(label = "B", active = current.isBold, isBold = true,
                onClick = { current = current.copy(isBold = !current.isBold); onChange(current) })
            StyleToggleBtn(label = "I", active = current.isItalic, isItalic = true,
                onClick = { current = current.copy(isItalic = !current.isItalic); onChange(current) })
            Spacer(Modifier.weight(1f))
            Text("Réinitialiser", color = PlayerAccent, fontSize = 13.sp,
                modifier = Modifier.clickable { current = KSubtitleStyle(); onChange(current) })
        }

        Spacer(Modifier.height(16.dp))

        // Font dropdown (simplified)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TextFields, null, tint = PlayerMuted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, PlayerMuted, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row {
                    Text(current.font.name.replace("_", " ")
                        .replaceFirstChar { it.uppercase() },
                        color = PlayerOnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, null, tint = PlayerMuted)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Font size slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FormatSize, null, tint = PlayerMuted, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Taille de la police", color = PlayerOnSurface, fontSize = 13.sp)
                Slider(
                    value         = current.fontSize.toFloat(),
                    onValueChange = { s ->
                        current = current.copy(fontSize = s.toInt())
                        onChange(current)
                    },
                    valueRange    = 20f..120f,
                    colors        = SliderDefaults.colors(
                        thumbColor         = PlayerAccent,
                        activeTrackColor   = PlayerAccent,
                        inactiveTrackColor = PlayerMuted.copy(0.4f)
                    )
                )
                Text("${current.fontSize}", color = PlayerMuted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Border style
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BorderStyle, null, tint = PlayerMuted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Style de la bordure", color = PlayerOnSurface, fontSize = 13.sp)
                Text(
                    when (current.borderStyle) {
                        KSubtitleBorderStyle.OutlineAndShadow -> "Contour et ombre"
                        KSubtitleBorderStyle.Outline          -> "Contour"
                        KSubtitleBorderStyle.Shadow           -> "Ombre"
                        KSubtitleBorderStyle.None             -> "Aucun"
                    },
                    color = PlayerMuted, fontSize = 12.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  AUDIO DELAY DIALOG
// ─────────────────────────────────────────────

@Composable
private fun KAudioDelayDialog(
    delayMs: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var delay by remember { mutableStateOf(delayMs) }

    PlayerDialog(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Délai audio", color = PlayerOnSurface,
                fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = PlayerOnSurface)
            }
        }

        DelayStepperRow(
            label       = "Délai",
            value       = delay,
            unit        = "ms",
            onDecrement = { delay -= 500 },
            onIncrement = { delay += 500 }
        )

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlayerPinkBtn(text = "Définir par défaut", modifier = Modifier.weight(1f),
                onClick = { onConfirm(delay) })
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PlayerAccent)
                    .clickable { delay = 0 },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, null, tint = PlayerOnSurface,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
//  SLEEP TIMER DIALOG
// ─────────────────────────────────────────────

@Composable
private fun KSleepTimerDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours   by remember { mutableStateOf(currentMinutes / 60) }
    var minutes by remember { mutableStateOf(currentMinutes % 60) }
    var inputHours by remember { mutableStateOf(true) } // which column is selected

    PlayerDialog(onDismiss = onDismiss) {
        Text("Entrer une durée", color = PlayerOnSurface,
            fontSize = 16.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hours selector
            TimePickerColumn(
                value    = hours,
                label    = "Heure",
                selected = inputHours,
                onClick  = { inputHours = true }
            )

            Text(":", color = PlayerOnSurface, fontSize = 28.sp, fontWeight = FontWeight.Light)

            // Minutes selector
            TimePickerColumn(
                value    = minutes,
                label    = "Minute",
                selected = !inputHours,
                onClick  = { inputHours = false }
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccessTime, null, tint = PlayerMuted)
            Text(
                text    = "Valider",
                color   = PlayerAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onConfirm(hours * 60 + minutes) }
            )
        }
    }
}

@Composable
private fun TimePickerColumn(
    value: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp, 70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) PlayerAccent else SheetBackground.copy(0.6f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text     = "%02d".format(value),
                color    = PlayerOnSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = PlayerMuted, fontSize = 11.sp)
    }
}

// ─────────────────────────────────────────────
//  VIDEO FILTER DIALOG
// ─────────────────────────────────────────────

@Composable
private fun KVideoFilterDialog(
    filters: KVideoFilters,
    onDismiss: () -> Unit,
    onChange: (KVideoFilters) -> Unit
) {
    var current by remember { mutableStateOf(filters) }

    PlayerDialog(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filtres", color = PlayerOnSurface,
                fontSize = 20.sp, fontWeight = FontWeight.Light, modifier = Modifier.weight(1f))
            Text("Réinitialiser", color = PlayerAccent, fontSize = 13.sp,
                modifier = Modifier.clickable { current = KVideoFilters(); onChange(current) })
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = PlayerOnSurface)
            }
        }

        FilterSliderRow("Luminosité", current.brightness, -1f..1f) {
            current = current.copy(brightness = it); onChange(current)
        }
        FilterSliderRow("Saturation", current.saturation, -1f..1f) {
            current = current.copy(saturation = it); onChange(current)
        }
        FilterSliderRow("Contraste", current.contrast, -1f..1f) {
            current = current.copy(contrast = it); onChange(current)
        }
        FilterSliderRow("Gamma", current.gamma, -1f..1f) {
            current = current.copy(gamma = it); onChange(current)
        }
        FilterSliderRow("Teinte", current.hue, -180f..180f) {
            current = current.copy(hue = it); onChange(current)
        }
    }
}

@Composable
private fun FilterSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = PlayerOnSurface, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value         = value,
                onValueChange = onChange,
                valueRange    = range,
                modifier      = Modifier.weight(1f),
                colors        = SliderDefaults.colors(
                    thumbColor         = PlayerAccent,
                    activeTrackColor   = PlayerAccent,
                    inactiveTrackColor = PlayerMuted.copy(0.4f)
                )
            )
        }
        Text(
            "%.0f".format(value),
            color = PlayerMuted, fontSize = 11.sp
        )
    }
}

// ─────────────────────────────────────────────
//  Shared primitives
// ─────────────────────────────────────────────

@Composable
private fun PlayerBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(SheetBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = {}
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .navigationBarsPadding(),
                content = content
            )
        }
    }
}

@Composable
private fun PlayerDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onDismiss
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SheetBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = {}
                    )
                    .padding(20.dp),
                content = content
            )
        }
    }
}

@Composable
private fun PlayerIconBtn(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(icon, contentDescription = contentDescription,
            tint = PlayerOnSurface, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SheetIconTextBtn(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = PlayerAccent, modifier = Modifier.size(16.dp))
        Text(label, color = PlayerAccent, fontSize = 12.sp)
    }
}

@Composable
private fun SheetAddRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.Add, null, tint = PlayerOnSurface, modifier = Modifier.size(20.dp))
        Text(label, color = PlayerOnSurface, fontSize = 14.sp)
    }
}

@Composable
private fun SheetCheckRow(
    label: String,
    checked: Boolean,
    trailing: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) SheetSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) PlayerAccent else Color.Transparent)
                .border(1.5.dp,
                    if (checked) PlayerAccent else PlayerMuted,
                    RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Default.Check, null, tint = PlayerOnSurface,
                modifier = Modifier.size(14.dp))
        }
        Text(
            text       = label,
            color      = if (checked) PlayerOnSurface else PlayerMuted,
            fontSize   = 14.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            modifier   = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(trailing, color = PlayerMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SheetRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick  = onClick,
            colors   = RadioButtonDefaults.colors(
                selectedColor   = PlayerAccent,
                unselectedColor = PlayerMuted
            )
        )
        Text(
            text       = label,
            color      = if (selected) PlayerOnSurface else PlayerMuted,
            fontSize   = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun PlayerPinkBtn(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(PlayerAccent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = PlayerOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StyleToggleBtn(
    label: String,
    active: Boolean,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) PlayerAccent else Color.Transparent)
            .border(1.dp, if (active) PlayerAccent else PlayerMuted, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = PlayerOnSurface,
            fontSize   = 16.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle  = if (isItalic) FontStyle.Italic else FontStyle.Normal
        )
    }
}