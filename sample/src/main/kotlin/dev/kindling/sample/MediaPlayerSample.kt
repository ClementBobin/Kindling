package dev.kindling.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import dev.kindling.core.components.prefab.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Sample screen wiring up [KMediaPlayer] with mock data so you can test it
 * standalone — drop this composable into your `:sample` module's NavHost
 * or call it directly from an Activity's `setContent {}`.
 *
 * Simulates:
 * - a 23m42s episode with a fake playback clock (advances when `isPlaying`)
 * - 5 episodes in the list (mirrors the screenshots)
 * - 2 subtitle tracks, 1 audio track, 4 quality options
 * - the "+85 s" custom skip button wired to actually seek
 */
@Composable
fun MediaPlayerSample() {
    // ── Mock episode list (matches screenshot data) ─────────────────────────
    val episodes = remember {
        listOf(
            KMediaEpisode(5, "Elf Girl & Dragon Girl", "01/06/2026"),
            KMediaEpisode(4, "Awaken - Behemoth!", "01/06/2026"),
            KMediaEpisode(3, "Urgent Quest - Defeat the Demons", "01/06/2026"),
            KMediaEpisode(2, "The Tiger-Eared Blacksmith and a New Skill", "01/06/2026"),
        )
    }

    var currentEpisode by remember { mutableStateOf(episodes.first()) }

    // ── Mock player state ────────────────────────────────────────────────
    var state by remember {
        mutableStateOf(
            KPlayerState(
                isPlaying     = false,
                autoPlay      = false,
                positionMs    = 0L,
                durationMs    = (23 * 60 + 42) * 1000L,   // 23:42 like the screenshot
                speed         = 1.00f,
                subtitleTracks = listOf(
                    KMediaSubtitleTrack(id = 1, label = "English"),
                    KMediaSubtitleTrack(id = 2, label = "English 2"),
                ),
                activeSubtitleIds = setOf(1),
                audioTracks = listOf(
                    KMediaAudioTrack(id = 1, label = "Japanese"),
                    KMediaAudioTrack(id = 2, label = "English Dub"),
                ),
                activeAudioId = 1,
                qualityOptions = listOf(
                    KMediaQualityOption(0, "Auto"),
                    KMediaQualityOption(1, "1080p"),
                    KMediaQualityOption(2, "720p"),
                    KMediaQualityOption(3, "480p"),
                ),
                activeQualityId = 0,
                customSkipButtons = listOf(KCustomSkipButton("+85 s", 85))
            )
        )
    }

    // ── Fake playback clock — advances position while "playing" ─────────────
    LaunchedEffect(state.isPlaying) {
        while (isActive && state.isPlaying) {
            delay(1000)
            state = state.copy(
                positionMs = (state.positionMs + 1000).coerceAtMost(state.durationMs)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Stand-in for the actual video surface (AndroidView/PlayerView in a real app)
        Text(
            text       = "▶ ${currentEpisode.title}",
            color      = Color.White.copy(alpha = 0.15f),
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.align(Alignment.Center)
        )

        KMediaPlayer(
            title    = "Beheneko: The Elf-Girl's Cat is Secretly an S-Ranked Monster!",
            subtitle = "Episode ${currentEpisode.number}: ${currentEpisode.title}",
            episodes = episodes,
            playerState = state,
            onBack = {
                // navController.popBackStack()
                println("Back pressed")
            },
            onStateChange = { newState ->
                state = newState
            },
            onSeek = { ms ->
                state = state.copy(positionMs = ms.coerceIn(0, state.durationMs))
            },
            onSkip = { seconds ->
                state = state.copy(
                    positionMs = (state.positionMs + seconds * 1000L)
                        .coerceIn(0, state.durationMs)
                )
            },
            onEpisodeSelect = { ep ->
                currentEpisode = ep
                state = state.copy(positionMs = 0L, isPlaying = true)
            }
        )
    }
}