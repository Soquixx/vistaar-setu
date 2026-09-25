package com.vistaarsetu.app.ui.lessons

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.vistaarsetu.app.data.AudioCacheManager
import com.vistaarsetu.app.data.LessonEntity
import com.vistaarsetu.app.data.RetrofitClient
import java.io.File

@Composable
fun LessonDetailScreen(
    lesson: LessonEntity,
    onBack: () -> Unit,
    onOpenFlashcards: (LessonEntity) -> Unit,
    onOpenWorksheet: (LessonEntity) -> Unit,
    onOpenCorrection: (LessonEntity) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

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
                Log.e("LESSON_DETAIL", "Player error", error)
                isPlaying = false
                Toast.makeText(context, "Audio error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    val isOfflineReady = AudioCacheManager.isAvailable(lesson.localAudioPath)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP APP BAR
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E1B4B)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            "Lesson Details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            "${lesson.grade} • ${lesson.subject}",
                            fontSize = 12.sp,
                            color = Color(0xFF7C3AED),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isOfflineReady) Color(0xFFD1FAE5) else Color(0xFFEDE9FE)
                ) {
                    Text(
                        if (isOfflineReady) "Offline Ready" else lesson.verificationStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOfflineReady) Color(0xFF065F46) else Color(0xFF7C3AED),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // LESSON TITLE CARD
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEDE9FE)
                        ) {
                            Text(
                                lesson.targetLanguage,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (lesson.verificationStatus == "teacher_corrected") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    "Teacher Corrected",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        lesson.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B)
                    )
                }
            }
        }

        // HINDI SOURCE TEXT
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "HINDI SOURCE TEXT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280)
                        )
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(lesson.hindiText))
                                Toast.makeText(context, "Hindi copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Hindi",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        lesson.hindiText,
                        fontSize = 15.sp,
                        color = Color(0xFF1F2937),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // SANTALI TRANSLATION
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF4C1D95),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SANTALI (OL CHIKI)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDDD6FE)
                        )
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(lesson.translatedText))
                                Toast.makeText(context, "Santali translation copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Translation",
                                tint = Color(0xFFDDD6FE),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        lesson.translatedText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 28.sp
                    )
                }
            }
        }

        // INTEGRATED AUDIO CONTROLS
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Vernacular Audio",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                        }

                        // Speed
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
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Play Button
                    Button(
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                                return@Button
                            }

                            if (exoPlayer.playbackState == Player.STATE_READY && !isPlaying) {
                                exoPlayer.play()
                                return@Button
                            }

                            // Priority 1: Local audio
                            val local = lesson.localAudioPath
                            if (AudioCacheManager.isAvailable(local)) {
                                try {
                                    exoPlayer.stop()
                                    exoPlayer.clearMediaItems()
                                    val item = MediaItem.fromUri(Uri.fromFile(File(local!!)))
                                    exoPlayer.setMediaItem(item)
                                    exoPlayer.setPlaybackSpeed(playbackSpeed)
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                    return@Button
                                } catch (e: Exception) {
                                    Log.e("LESSON_DETAIL", "Local audio failed", e)
                                }
                            }

                            // Priority 2: Remote audio
                            val remote = lesson.remoteAudioUrl
                            val fullRemote = if (!remote.isNullOrBlank()) {
                                if (remote.startsWith("http://") || remote.startsWith("https://")) remote
                                else RetrofitClient.getFullAudioUrl(remote)
                            } else null

                            if (!fullRemote.isNullOrBlank()) {
                                try {
                                    exoPlayer.stop()
                                    exoPlayer.clearMediaItems()
                                    val item = MediaItem.fromUri(Uri.parse(fullRemote))
                                    exoPlayer.setMediaItem(item)
                                    exoPlayer.setPlaybackSpeed(playbackSpeed)
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                    return@Button
                                } catch (e: Exception) {
                                    Log.e("LESSON_DETAIL", "Remote audio failed", e)
                                }
                            }

                            Toast.makeText(context, "Audio unavailable", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isPlaying) "Pause Audio" else if (isOfflineReady) "Play Offline Audio" else "Play Audio",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // PEDAGOGICAL TOOL ENTRY POINTS
        item {
            Text(
                "LEARNING TOOLS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7C3AED)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // FLASHCARDS BUTTON
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEDE9FE),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Style,
                                        contentDescription = "Flashcards",
                                        tint = Color(0xFF7C3AED)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Bilingual Flashcards",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    "Interactive flip cards for Hindi ↔ Santali",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        Button(
                            onClick = { onOpenFlashcards(lesson) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open", fontSize = 12.sp)
                        }
                    }
                }

                // WORKSHEET BUTTON
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEDE9FE),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Assignment,
                                        contentDescription = "Worksheets",
                                        tint = Color(0xFF7C3AED)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Bilingual Worksheet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    "Practice exercises & comprehension",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        Button(
                            onClick = { onOpenWorksheet(lesson) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open", fontSize = 12.sp)
                        }
                    }
                }

                // TEACHER CORRECTION BUTTON
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.EditNote,
                                        contentDescription = "Teacher Correction",
                                        tint = Color(0xFFD97706)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Teacher Review & Correction",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    "Refine translation or dialect phrasing",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { onOpenCorrection(lesson) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Review", fontSize = 12.sp, color = Color(0xFF7C3AED))
                        }
                    }
                }
            }
        }
    }
}
