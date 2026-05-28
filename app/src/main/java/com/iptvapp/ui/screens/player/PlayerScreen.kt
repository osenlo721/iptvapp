package com.iptvapp.ui.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.iptvapp.data.model.Channel
import com.iptvapp.data.model.StreamType
import com.iptvapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    channelId: String,
    onBack: () -> Unit,
    vm: PlayerViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(channelId) { vm.loadChannel(channelId) }

    when {
        state.isLoading -> LoadingOverlay()
        state.error != null -> ErrorOverlay(state.error!!, onBack)
        state.channel != null -> VideoPlayerContent(
            channel = state.channel!!,
            onBack = onBack,
            onFavoriteToggle = vm::toggleFavorite
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerContent(
    channel: Channel,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val context = LocalContext.current
    var controlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(1f) }
    var showVolumeSlider by remember { mutableStateOf(false) }

    // Auto-hide controls after 3s
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3_000)
            controlsVisible = false
        }
    }

    // Force landscape in fullscreen
    DisposableEffect(isFullscreen) {
        val activity = context as? Activity
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val exoPlayer = remember(channel.url) {
        ExoPlayer.Builder(context).build().also { player ->
            val mediaItem = when (channel.streamType) {
                StreamType.RTMP -> {
                    // RTMP requires a dedicated library; load as-is and let ExoPlayer fail gracefully
                    MediaItem.fromUri(channel.url)
                }
                else -> MediaItem.fromUri(channel.url)
            }
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // Sync volume
    LaunchedEffect(volume) { exoPlayer.volume = volume }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { controlsVisible = !controlsVisible })
            }
    ) {
        // --- ExoPlayer Surface ---
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering spinner
        if (isBuffering) {
            CircularProgressIndicator(
                color = NetflixRed,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- Controls overlay ---
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize()) {
                // Top scrim + bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.25f)
                        .align(Alignment.TopCenter)
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(.7f), Color.Transparent)))
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver", tint = OnDark)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f)) {
                        Text(channel.name, style = MaterialTheme.typography.titleMedium, color = OnDark,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (channel.streamType != StreamType.MP4) {
                            Text("EN VIVO", style = MaterialTheme.typography.labelSmall, color = LiveRed)
                        }
                    }
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            if (channel.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            "Favorito",
                            tint = if (channel.isFavorite) NetflixRed else OnDark
                        )
                    }
                }

                // Center play/pause button
                Box(
                    Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = OnDark,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Bottom scrim + controls
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.30f)
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.75f))))
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Volume
                    IconButton(onClick = { showVolumeSlider = !showVolumeSlider }) {
                        Icon(
                            imageVector = when {
                                volume == 0f -> Icons.Filled.VolumeOff
                                volume < 0.5f -> Icons.Filled.VolumeDown
                                else -> Icons.Filled.VolumeUp
                            },
                            contentDescription = "Volumen",
                            tint = OnDark
                        )
                    }
                    if (showVolumeSlider) {
                        Slider(
                            value = volume,
                            onValueChange = { volume = it },
                            modifier = Modifier.width(120.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = OnDark,
                                activeTrackColor = NetflixRed,
                                inactiveTrackColor = SurfaceElevated
                            )
                        )
                    }
                    Spacer(Modifier.weight(1f))

                    // Fullscreen toggle
                    IconButton(onClick = { isFullscreen = !isFullscreen }) {
                        Icon(
                            if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            "Pantalla completa", tint = OnDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NetflixRed)
            Spacer(Modifier.height(12.dp))
            Text("Cargando canal…", color = OnDarkMuted)
        }
    }
}

@Composable
private fun ErrorOverlay(message: String, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.ErrorOutline, null, tint = LiveRed, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(message, color = OnDarkMuted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)) {
                Text("Volver")
            }
        }
    }
}
