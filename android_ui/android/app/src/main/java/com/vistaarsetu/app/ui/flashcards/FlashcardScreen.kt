package com.vistaarsetu.app.ui.flashcards

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.vistaarsetu.app.data.AudioCacheManager
import com.vistaarsetu.app.data.LessonEntity
import java.io.File

data class FlashcardItem(
    val frontTitle: String,
    val frontPrompt: String,
    val backTranslation: String,
    val note: String,
    val icon: ImageVector? = null
)

@Composable
fun FlashcardScreen(
    lesson: LessonEntity,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Deterministically generate flashcards from lesson content
    val flashcards = remember(lesson.id) {
        generateFlashcardsForLesson(lesson)
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    // Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    // Optional ExoPlayer for audio playback on cards
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    val currentCard = flashcards.getOrNull(currentIndex) ?: FlashcardItem(
        frontTitle = lesson.hindiText,
        frontPrompt = "Translate into Santali",
        backTranslation = lesson.translatedText,
        note = "Full Lesson Concept"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1E1B4B)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Lesson Flashcards",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
                Text(
                    "${lesson.grade} • ${lesson.subject}",
                    fontSize = 11.sp,
                    color = Color(0xFF7C3AED),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEDE9FE)
            ) {
                Text(
                    "${currentIndex + 1}/${flashcards.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PROGRESS BAR
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / flashcards.size.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF7C3AED),
            trackColor = Color(0xFFEDE9FE)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // FLASHCARD CONTAINER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable {
                    isFlipped = !isFlipped
                },
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // FRONT SIDE (Hindi)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                                    "HINDI QUESTION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                "Tap card to flip",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            currentCard.icon?.let { icon  ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(56.dp)

                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )
                            }
                            Text(
                                currentCard.frontTitle,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B),
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                currentCard.frontPrompt,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF3F0FF),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Flip",
                                    tint = Color(0xFF7C3AED)
                                )
                            }
                        }
                    }
                }
            } else {
                // BACK SIDE (Santali Ol Chiki) - rotated 180 so it's upright
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF4C1D95),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6D28D9)
                            ) {
                                Text(
                                    "SANTALI (OL CHIKI)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDDD6FE),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Audio button if local audio available
                            val hasLocalAudio = AudioCacheManager.isAvailable(lesson.localAudioPath)
                            if (hasLocalAudio) {
                                IconButton(
                                    onClick = {
                                        try {
                                            exoPlayer.stop()
                                            exoPlayer.clearMediaItems()
                                            val item = MediaItem.fromUri(Uri.fromFile(File(lesson.localAudioPath!!)))
                                            exoPlayer.setMediaItem(item)
                                            exoPlayer.prepare()
                                            exoPlayer.play()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Audio error", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Play Audio",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                currentCard.backTranslation,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 34.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                currentCard.note,
                                fontSize = 13.sp,
                                color = Color(0xFFC4B5FD),
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            "Tap to flip back",
                            fontSize = 11.sp,
                            color = Color(0xFFDDD6FE)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // NAVIGATION CONTROLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    if (currentIndex > 0) {
                        isFlipped = false
                        currentIndex--
                    }
                },
                enabled = currentIndex > 0,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Previous")
            }

            Button(
                onClick = {
                    if (currentIndex < flashcards.size - 1) {
                        isFlipped = false
                        currentIndex++
                    } else {
                        // Reset to first
                        isFlipped = false
                        currentIndex = 0
                        Toast.makeText(context, "Flashcard set completed!", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                Text(if (currentIndex == flashcards.size - 1) "Restart" else "Next")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

private fun generateFlashcardsForLesson(lesson: LessonEntity): List<FlashcardItem> {
    val cards = mutableListOf<FlashcardItem>()

    // Card 1: Full Lesson Concept
    cards.add(
        FlashcardItem(
            frontTitle = lesson.hindiText,
            frontPrompt = "पूरा वाक्य संताली (Ol Chiki) में क्या होगा?",
            backTranslation = lesson.translatedText,
            note = "${lesson.grade} • ${lesson.subject}"
        )
    )

    val vocabularyMap = listOf(
        Triple("एक", "ᱢᱤᱫ (1)", Icons.Filled.LooksOne),
        Triple("दो", "ᱵᱟᱨ (2)", Icons.Filled.LooksTwo),
        Triple("तीन", "ᱯᱮ (3)", Icons.Filled.Looks3),
        Triple("पानी", "ᱫᱟᱜ (Dag)", Icons.Filled.WaterDrop),
        Triple("पेड़", "ᱫᱟᱨᱮ (Dare)", Icons.Filled.Park),
        Triple("किताब", "ᱯᱩᱛᱷᱤ (Puthi)", Icons.Filled.MenuBook),
        Triple("बच्चे", "ᱜᱤᱫᱽᱨᱟᱹ (Gidra)", Icons.Filled.ChildCare),
        Triple("शिक्षक", "ᱢᱟᱪᱮᱛ (Machet)", Icons.Filled.School),
        Triple("गाय", "ᱜᱟᱹᱭ (Gai)", Icons.Filled.Pets),
        Triple("कुत्ता", "ᱥᱮᱛᱟ (Seta)", Icons.Filled.Pets)
    )

    vocabularyMap.forEach { (hindiWord, santaliWord, iconRef) ->

        if (lesson.hindiText.contains(hindiWord)) {

            cards.add(
                FlashcardItem(
                    frontTitle = hindiWord,
                    frontPrompt = "इस शब्द को संताली (ᱚᱞ ᱪᱤᱠᱤ) में पहचानें",
                    backTranslation = santaliWord,
                    note = "शब्दावली (Vocabulary)",
                    icon = iconRef
                )
            )
        }
    }
    // Split sentences if lesson contains multiple clauses
    val sentences = lesson.hindiText.split("।", "\n").map { it.trim() }.filter { it.length > 4 }
    val translatedSentences = lesson.translatedText.split("᱾", "\n").map { it.trim() }.filter { it.length > 4 }

    if (sentences.size > 1 && sentences.size == translatedSentences.size) {
        sentences.indices.forEach { i ->
            cards.add(
                FlashcardItem(
                    frontTitle = sentences[i] + "।",
                    frontPrompt = "वाक्य खंड का संताली अनुवाद",
                    backTranslation = translatedSentences[i] + "᱾",
                    note = "Sentence #${i + 1}"
                )
            )
        }
    }
    return cards
}
