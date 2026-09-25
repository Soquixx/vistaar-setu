package com.vistaarsetu.app.ui.player

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.vistaarsetu.app.data.AppDatabase
import com.vistaarsetu.app.data.AudioCacheManager
import com.vistaarsetu.app.data.LessonEntity
import com.vistaarsetu.app.data.RetrofitClient
import java.io.File

@Composable
fun PlayerScreen(
    initialLessonId: Long? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var lessons by remember { mutableStateOf<List<LessonEntity>>(emptyList()) }
    var selectedLesson by remember { mutableStateOf<LessonEntity?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

    // Load lessons from Room
    LaunchedEffect(Unit) {
        val db = AppDatabase.getDatabase(context)
        val loaded = db.lessonDao().getAllLessons()
        lessons = loaded
        if (loaded.isNotEmpty()) {
            selectedLesson = if (initialLessonId != null) {
                loaded.firstOrNull { it.id == initialLessonId } ?: loaded.first()
            } else {
                loaded.first()
            }
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    isPlaying = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("PLAYER_SCREEN", "Audio playback error", error)
                isPlaying = false
                Toast.makeText(context, "Playback error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Stop playback if selected lesson changes
    LaunchedEffect(selectedLesson?.id) {
        if (isPlaying) {
            exoPlayer.stop()
            isPlaying = false
        }
    }

    val currentLesson = selectedLesson

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E1B4B)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        "Audio Player",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B)
                    )
                    Text(
                        "Vernacular Pronunciation & Listening",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEDE9FE),
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Headphones,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Offline Ready",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (lessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MusicOff,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No lessons saved in library yet.",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // LESSON SELECTOR CAROUSEL
            Text(
                "SELECT LESSON",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7C3AED)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(lessons) { lesson ->
                    val isSelected = lesson.id == currentLesson?.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF7C3AED) else Color.White,
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier
                            .clickable {
                                selectedLesson = lesson
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                lesson.grade,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFFEDE9FE) else Color(0xFF7C3AED)
                            )
                            Text(
                                lesson.title.ifBlank { "Lesson #${lesson.id}" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF1E1B4B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MAIN PLAYER CARD
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    if (currentLesson != null) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // METADATA ROW
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEDE9FE)
                                    ) {
                                        Text(
                                            "${currentLesson.grade} • ${currentLesson.subject}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7C3AED),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    val isLocalReady = AudioCacheManager.isAvailable(currentLesson.localAudioPath)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isLocalReady) Color(0xFFD1FAE5) else Color(0xFFF3F4F6)
                                    ) {
                                        Text(
                                            if (isLocalReady) "Available Offline" else "Remote Audio",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLocalReady) Color(0xFF065F46) else Color(0xFF6B7280),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // LESSON TITLE
                                Text(
                                    currentLesson.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // HINDI SOURCE
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF9FAFB),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            "HINDI (SOURCE)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6B7280)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            currentLesson.hindiText,
                                            fontSize = 13.sp,
                                            color = Color(0xFF374151)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // SANTALI TRANSLATION
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF4C1D95),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            "SANTALI (OL CHIKI)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDDD6FE)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            currentLesson.translatedText,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            lineHeight = 24.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // WAVEFORM VISUALIZER
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    val waveHeights = listOf(12, 24, 18, 32, 14, 28, 20, 36, 16, 26, 14, 30, 22, 12, 28, 16)
                                    waveHeights.forEachIndexed { index, baseHeight ->
                                        val height = if (isPlaying) {
                                            (baseHeight * (if (index % 2 == 0) 1.1f else 0.85f)).dp
                                        } else {
                                            (baseHeight * 0.4f).coerceAtLeast(6f).dp
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(height)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isPlaying) Color(0xFF7C3AED) else Color(0xFFC4B5FD)
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // AUDIO PLAYBACK CONTROLS
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // SPEED TOGGLE
                                    IconButton(
                                        onClick = {
                                            playbackSpeed = when (playbackSpeed) {
                                                0.8f -> 1f
                                                1f -> 1.25f
                                                1.25f -> 1.5f
                                                else -> 0.8f
                                            }
                                            exoPlayer.setPlaybackSpeed(playbackSpeed)
                                        }
                                    ) {
                                        Text(
                                            "${playbackSpeed}x",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7C3AED),
                                            fontSize = 13.sp
                                        )
                                    }

                                    // PLAY / PAUSE BUTTON
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF7C3AED),
                                        shadowElevation = 6.dp,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clickable {
                                                // Priority:
                                                // 1. If currently playing -> pause
                                                if (isPlaying) {
                                                    exoPlayer.pause()
                                                    return@clickable
                                                }

                                                // 2. If paused -> resume
                                                if (exoPlayer.playbackState == Player.STATE_READY && !isPlaying) {
                                                    exoPlayer.play()
                                                    return@clickable
                                                }

                                                // 3. Audio source priority:
                                                // Priority 1: localAudioPath
                                                val localPath = currentLesson.localAudioPath
                                                if (AudioCacheManager.isAvailable(localPath)) {
                                                    try {
                                                        Log.d("PLAYER_SCREEN", "Playing LOCAL audio: $localPath")
                                                        exoPlayer.stop()
                                                        exoPlayer.clearMediaItems()
                                                        val item = MediaItem.fromUri(Uri.fromFile(File(localPath!!)))
                                                        exoPlayer.setMediaItem(item)
                                                        exoPlayer.setPlaybackSpeed(playbackSpeed)
                                                        exoPlayer.prepare()
                                                        exoPlayer.play()
                                                        return@clickable
                                                    } catch (e: Exception) {
                                                        Log.e("PLAYER_SCREEN", "Local audio failed", e)
                                                    }
                                                }

                                                // Priority 2: remoteAudioUrl
                                                val remote = currentLesson.remoteAudioUrl
                                                val resolvedRemote = if (!remote.isNullOrBlank()) {
                                                    if (remote.startsWith("http://") || remote.startsWith("https://")) {
                                                        remote
                                                    } else {
                                                        RetrofitClient.getFullAudioUrl(remote)
                                                    }
                                                } else null

                                                if (!resolvedRemote.isNullOrBlank()) {
                                                    try {
                                                        Log.d("PLAYER_SCREEN", "Playing REMOTE audio: $resolvedRemote")
                                                        exoPlayer.stop()
                                                        exoPlayer.clearMediaItems()
                                                        val item = MediaItem.fromUri(Uri.parse(resolvedRemote))
                                                        exoPlayer.setMediaItem(item)
                                                        exoPlayer.setPlaybackSpeed(playbackSpeed)
                                                        exoPlayer.prepare()
                                                        exoPlayer.play()
                                                        return@clickable
                                                    } catch (e: Exception) {
                                                        Log.e("PLAYER_SCREEN", "Remote audio failed", e)
                                                    }
                                                }

                                                // Priority 3: Audio unavailable
                                                Toast.makeText(context, "Audio is unavailable for this lesson.", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Pause" else "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }

                                    // RESET TO START
                                    IconButton(
                                        onClick = {
                                            exoPlayer.seekTo(0)
                                            if (!isPlaying) exoPlayer.play()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Replay,
                                            contentDescription = "Restart",
                                            tint = Color(0xFF6B7280)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // STATUS LABEL
                                val localAvailable = AudioCacheManager.isAvailable(currentLesson.localAudioPath)
                                val hasAudio = localAvailable || !currentLesson.remoteAudioUrl.isNullOrBlank()

                                Text(
                                    text = when {
                                        isPlaying && localAvailable -> "Playing offline local audio"
                                        isPlaying -> "Playing streaming audio"
                                        localAvailable -> "Audio available offline (Downloaded)"
                                        hasAudio -> "Remote audio ready"
                                        else -> "Audio not recorded for this lesson"
                                    },
                                    fontSize = 11.sp,
                                    color = if (hasAudio) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
