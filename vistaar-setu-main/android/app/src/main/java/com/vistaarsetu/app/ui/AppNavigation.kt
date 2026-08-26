package com.vistaarsetu.app.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.vistaarsetu.app.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen { WELCOME, HOME, NEW_LESSON, PROCESSING, RESULT, SAVED_LESSONS, STUDENT_PROGRESS, NOTIFICATIONS }

@Composable
fun MainAppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var inputGrade by remember { mutableStateOf("Grade 3") }
    var inputArea by remember { mutableStateOf("Mathematics") }
    var inputLang by remember { mutableStateOf("Santali") }
    var inputHindiText by remember { mutableStateOf("गिनो: एक, दो, तीन, चार, पाँच\nCount: 1, 2, 3, 4, 5") }
    var lastResponse by remember { mutableStateOf<ProcessLessonResponse?>(null) }

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.WELCOME && currentScreen != Screen.PROCESSING) {
                VistaarBottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        },
        containerColor = Color(0xFFF5F3FF) // Soft Lavender Background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.WELCOME -> WelcomeScreen(
                    onGetStarted = { currentScreen = Screen.HOME }
                )
                Screen.HOME -> HomeScreen(
                    onNewLesson = { currentScreen = Screen.NEW_LESSON },
                    onViewSaved = { currentScreen = Screen.SAVED_LESSONS },
                    onPlayRecent = {
                        inputHindiText = "गिनो: एक, दो, तीन, चार, पाँच\nCount: 1, 2, 3, 4, 5"
                        currentScreen = Screen.PROCESSING
                    },
                    onOpenProgress = { currentScreen = Screen.STUDENT_PROGRESS },
                    onOpenNotifications = { currentScreen = Screen.NOTIFICATIONS }
                )
                Screen.NEW_LESSON -> NewLessonScreen(
                    grade = inputGrade, onGradeChange = { inputGrade = it },
                    area = inputArea, onAreaChange = { inputArea = it },
                    lang = inputLang, onLangChange = { inputLang = it },
                    text = inputHindiText, onTextChange = { inputHindiText = it },
                    onBack = { currentScreen = Screen.HOME },
                    onGenerate = { currentScreen = Screen.PROCESSING }
                )
                Screen.PROCESSING -> ProcessingScreen(
                    grade = inputGrade, area = inputArea, lang = inputLang, text = inputHindiText,
                    onSuccess = { response ->
                        lastResponse = response
                        currentScreen = Screen.RESULT
                    }
                )
                Screen.RESULT -> ResultScreen(
                    grade = inputGrade,
                    subject = inputArea,
                    response = lastResponse,
                    onSave = { currentScreen = Screen.SAVED_LESSONS },
                    onHome = { currentScreen = Screen.HOME }
                )
                Screen.SAVED_LESSONS -> SavedLessonsScreen(
                    onBackHome = { currentScreen = Screen.HOME }
                )
                Screen.STUDENT_PROGRESS -> StudentProgressScreen(
                    onBack = { currentScreen = Screen.HOME }
                )
                Screen.NOTIFICATIONS -> NotificationsScreen(
                    onBack = { currentScreen = Screen.HOME },
                    onLearnNewLesson = { currentScreen = Screen.NEW_LESSON }
                )
            }
        }
    }
}

// ----------------------------------------------------
// BOTTOM NAVIGATION BAR
// ----------------------------------------------------
@Composable
fun VistaarBottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 12.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            NavigationBarItem(
                selected = currentScreen == Screen.HOME,
                onClick = { onScreenSelected(Screen.HOME) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = if (currentScreen == Screen.HOME) Color(0xFF7C3AED) else Color(0xFF9CA3AF)) },
                label = { Text("Home", fontSize = 11.sp, color = if (currentScreen == Screen.HOME) Color(0xFF7C3AED) else Color(0xFF9CA3AF), fontWeight = if (currentScreen == Screen.HOME) FontWeight.Bold else FontWeight.Normal) }
            )
            NavigationBarItem(
                selected = currentScreen == Screen.NEW_LESSON,
                onClick = { onScreenSelected(Screen.NEW_LESSON) },
                icon = { Icon(Icons.Default.Mic, contentDescription = "Voice", tint = if (currentScreen == Screen.NEW_LESSON) Color(0xFF7C3AED) else Color(0xFF9CA3AF)) },
                label = { Text("Voice Input", fontSize = 11.sp, color = if (currentScreen == Screen.NEW_LESSON) Color(0xFF7C3AED) else Color(0xFF9CA3AF), fontWeight = if (currentScreen == Screen.NEW_LESSON) FontWeight.Bold else FontWeight.Normal) }
            )
            NavigationBarItem(
                selected = currentScreen == Screen.RESULT,
                onClick = { onScreenSelected(Screen.RESULT) },
                icon = { Icon(Icons.Default.Headphones, contentDescription = "Audio Player", tint = if (currentScreen == Screen.RESULT) Color(0xFF7C3AED) else Color(0xFF9CA3AF)) },
                label = { Text("Player", fontSize = 11.sp, color = if (currentScreen == Screen.RESULT) Color(0xFF7C3AED) else Color(0xFF9CA3AF), fontWeight = if (currentScreen == Screen.RESULT) FontWeight.Bold else FontWeight.Normal) }
            )
            NavigationBarItem(
                selected = currentScreen == Screen.SAVED_LESSONS,
                onClick = { onScreenSelected(Screen.SAVED_LESSONS) },
                icon = { Icon(Icons.Default.Folder, contentDescription = "Library", tint = if (currentScreen == Screen.SAVED_LESSONS) Color(0xFF7C3AED) else Color(0xFF9CA3AF)) },
                label = { Text("Library", fontSize = 11.sp, color = if (currentScreen == Screen.SAVED_LESSONS) Color(0xFF7C3AED) else Color(0xFF9CA3AF), fontWeight = if (currentScreen == Screen.SAVED_LESSONS) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

// ----------------------------------------------------
// 1. WELCOME SCREEN (OPEN SCREEN / START) - MATCHES IMAGE 1 & 2
// ----------------------------------------------------
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // App Header Logo & Brand Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF7C3AED),
                modifier = Modifier.size(42.dp),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("VS", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Vistaar Setu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1B4B)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Graphic Illustration (Tight spacing, no massive middle gap)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Purple Circle Glow
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFEDE9FE), Color(0xFFF3E8FF), Color.Transparent)
                        )
                    )
            )

            // Dynamic Custom Vector Graphic of Teacher, Sound Wave & Dialect Students
            WelcomeTeacherStudentsGraphic()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Tagline & Get Started Button (Brought up closer, duplicate 'Vistaar Setu' title removed)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Connecting Classrooms\nwith Local Dialects",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B4B),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "AI-powered audio lessons in tribal & regional languages",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Get Started Button (Purple Gradient Pill Button)
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(27.dp), spotColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "GET STARTED",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Custom Vector Graphic Component for Welcome Screen (Renders Image 2 aesthetics)
@Composable
fun WelcomeTeacherStudentsGraphic() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Curved Dashed Connection Line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width * 0.32f, size.height * 0.35f)
                cubicTo(
                    size.width * 0.42f, size.height * 0.22f,
                    size.width * 0.58f, size.height * 0.22f,
                    size.width * 0.68f, size.height * 0.38f
                )
            }
            drawPath(
                path = path,
                color = Color(0xFF8B5CF6),
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )
        }

        // Center Soundwave Orb Icon
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(54.dp)
                .offset(y = (-30).dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color.White)
                    .border(2.dp, Color(0xFFC4B5FD), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Teacher Figure Layout (Left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Speech Bubble: Hindi 🎤
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("हिंदी ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E1B4B))
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Teacher Graphic (Purple Shawl & Phone)
                Surface(
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                    color = Color(0xFF7C3AED),
                    modifier = Modifier
                        .width(100.dp)
                        .height(130.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(shape = CircleShape, color = Color(0xFFFED7AA), modifier = Modifier.size(36.dp)) {}
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E1B4B), modifier = Modifier.size(width = 16.dp, height = 30.dp)) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("👩‍🏫", fontSize = 28.sp)
                            }
                        }
                    }
                }
            }
        }

        // Students Figure Layout (Right: Santali & Mundari)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Speech Bubble: Santhali
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC4B5FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Santhali", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7C3AED))
                            Text("ᱥᱟᱱᱛᱟᱲᱤ", fontSize = 10.sp, color = Color(0xFF6B7280))
                        }
                    }

                    // Speech Bubble: Mundari
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC4B5FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Mundari", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7C3AED))
                            Text("ᱢᱩᱱᱰᱟᱨᱤ", fontSize = 10.sp, color = Color(0xFF6B7280))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Students Graphic (School Uniforms & Backpacks)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier
                            .width(60.dp)
                            .height(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👦🎒", fontSize = 24.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                        color = Color(0xFFA78BFA),
                        modifier = Modifier
                            .width(60.dp)
                            .height(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👧📚", fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. HOME PAGE (DASHBOARD SCREEN) - MATCHES IMAGE 1 MIDDLE
// ----------------------------------------------------
// ----------------------------------------------------
// 2. HOME PAGE (DASHBOARD SCREEN) - MATCHES IMAGE 1 MIDDLE
// ----------------------------------------------------
@Composable
fun HomeScreen(
    onNewLesson: () -> Unit,
    onViewSaved: () -> Unit,
    onPlayRecent: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Header Bar: Profile & Welcome Text + Notification Bell
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Welcome, Priya!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B)
                    )
                }

                // Notification Bell Icon with Badge
                Box {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF1E1B4B),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                    )
                }
            }
        }

        // Search Bar: Q Search lessons...
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Search lessons...", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                }
            }
        }

        // Auto-Scrolling Side-by-Side Hero Banner
        item {
            AutoScrollingBanner()
        }

        // 2x2 Grid Action Layout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tile 1: New Lesson (+) -> OPENS VOICE PAGE
                    HomeGridTile(
                        modifier = Modifier.weight(1f),
                        title = "New Lesson",
                        onClick = onNewLesson
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF7C3AED),
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    // Tile 2: Offline Library
                    HomeGridTile(
                        modifier = Modifier.weight(1f),
                        title = "Offline Library",
                        onClick = onViewSaved
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF8B5CF6),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tile 3: Recent: Math (G3) with Play button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF4C1D95), // Dark purple tile matching design
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable(onClick = onPlayRecent)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recent:\nMath (G3)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 16.sp
                                )
                                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFFD8B4FE), modifier = Modifier.size(18.dp))
                            }

                            // Waveform visualizer line
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(12) { i ->
                                    val h = listOf(10, 20, 14, 28, 16, 22, 12, 26, 18, 10, 22, 14)[i % 12]
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(h.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFC4B5FD))
                                    )
                                }
                            }

                            // PLAY Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text("PLAY", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4C1D95))
                                }
                            }
                        }
                    }

                    // Tile 4: Student Progress
                    HomeGridTile(
                        modifier = Modifier.weight(1f),
                        title = "Student\nProgress",
                        onClick = onOpenProgress
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFDDD6FE),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Small Line Graph Curve
                            Canvas(modifier = Modifier.size(width = 60.dp, height = 28.dp)) {
                                val path = Path().apply {
                                    moveTo(0f, size.height * 0.8f)
                                    cubicTo(
                                        size.width * 0.3f, size.height * 0.9f,
                                        size.width * 0.6f, size.height * 0.2f,
                                        size.width, size.height * 0.4f
                                    )
                                }
                                drawPath(path = path, color = Color(0xFF7C3AED), style = Stroke(width = 3.dp.toPx()))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// AUTO-SCROLLING HERO BANNER
// ----------------------------------------------------
data class BannerSlide(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val gradient: List<Color>
)

@Composable
fun AutoScrollingBanner() {
    val slides = remember {
        listOf(
            BannerSlide("Empowering\nClassrooms in\nSantali", "Interactive audio & bilingual learning", "👩‍🏫📚", listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6))),
            BannerSlide("Come to Learn\nNew Lessons\nEvery Day", "Interactive Math & Science modules", "🚀✨", listOf(Color(0xFF4C1D95), Color(0xFF7C3AED))),
            BannerSlide("Track Student\nProgress\nDate-Wise", "Visual bar graphs & daily metrics", "📊🎓", listOf(Color(0xFF065F46), Color(0xFF10B981))),
            BannerSlide("100% Offline\nClassroom\nLibrary", "Learn anytime without internet", "📶💡", listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
        )
    }

    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            currentPage = (currentPage + 1) % slides.size
        }
    }

    val currentSlide = slides[currentPage]

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        AnimatedContent(
            targetState = currentSlide,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                )
            },
            label = "BannerSlideAnimation"
        ) { slide ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(colors = slide.gradient))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            slide.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            slide.subtitle,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Page Indicator Dots
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            slides.indices.forEach { index ->
                                val isActive = index == currentPage
                                Box(
                                    modifier = Modifier
                                        .size(width = if (isActive) 18.dp else 6.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) Color.White else Color(0x66FFFFFF))
                                )
                            }
                        }
                    }

                    // Illustration graphics on right
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x33FFFFFF),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(slide.emoji, fontSize = 42.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// STUDENT PROGRESS BAR GRAPH SCREEN
// ----------------------------------------------------
data class DailyProgressData(
    val dayLabel: String,
    val fullDate: String,
    val appOpened: Int, // sessions
    val studyMins: Int, // minutes
    val lessonsLearned: Int // count
)

@Composable
fun StudentProgressScreen(
    onBack: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf("This Week") }
    var selectedDayIndex by remember { mutableIntStateOf(6) } // Default to Sun 26

    val dailyData = remember {
        listOf(
            DailyProgressData("Mon 20", "Monday, Aug 20", 2, 35, 2),
            DailyProgressData("Tue 21", "Tuesday, Aug 21", 4, 60, 4),
            DailyProgressData("Wed 22", "Wednesday, Aug 22", 1, 20, 1),
            DailyProgressData("Thu 23", "Thursday, Aug 23", 5, 90, 5),
            DailyProgressData("Fri 24", "Friday, Aug 24", 3, 45, 3),
            DailyProgressData("Sat 25", "Saturday, Aug 25", 6, 110, 7),
            DailyProgressData("Sun 26", "Sunday, Aug 26", 4, 85, 5)
        )
    }

    val selectedDay = dailyData[selectedDayIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E1B4B))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Student Progress", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Text("Date-wise activity, study hours & lessons", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
            }
        }

        // Filter Pills
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("This Week", "This Month", "All Time").forEach { period ->
                    val isSelected = period == selectedPeriod
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF7C3AED) else Color.White,
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier.clickable { selectedPeriod = period }
                    ) {
                        Text(
                            period,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF4B5563)
                        )
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Study Time
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("⏱️ Study Time", fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("14.5 hrs", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C3AED))
                        Text("+15% vs last week", fontSize = 10.sp, color = Color(0xFF10B981))
                    }
                }

                // Lessons Finished
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("📖 Lessons", fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("28 Done", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                        Text("5 subjects", fontSize = 10.sp, color = Color(0xFF6B7280))
                    }
                }

                // Streak
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🔥 Streak", fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("7 Days", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEA580C))
                        Text("Active Learner", fontSize = 10.sp, color = Color(0xFFEA580C))
                    }
                }
            }
        }

        // Date-Wise Bar Graph Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Daily Activity Breakdown",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3E8FF)
                        ) {
                            Text(
                                "Date-Wise",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF7C3AED)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Study Time (min)", fontSize = 10.sp, color = Color(0xFF4B5563))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF2563EB)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lessons", fontSize = 10.sp, color = Color(0xFF4B5563))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("App Opened", fontSize = 10.sp, color = Color(0xFF4B5563))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Date-Wise Bar Graph Canvas
                    val maxStudyMins = 120f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dailyData.forEachIndexed { index, data ->
                                val isSelected = index == selectedDayIndex
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDayIndex = index }
                                        .padding(horizontal = 2.dp)
                                ) {
                                    // Bars Container
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFFF3E8FF) else Color.Transparent),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.Bottom,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(bottom = 4.dp)
                                        ) {
                                            // Bar 1: Study Time
                                            val studyHeightFraction = (data.studyMins / maxStudyMins).coerceIn(0.1f, 1f)
                                            Box(
                                                modifier = Modifier
                                                    .width(7.dp)
                                                    .fillMaxHeight(studyHeightFraction)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(Color(0xFF7C3AED))
                                            )

                                            // Bar 2: Lessons Learned
                                            val lessonsHeightFraction = ((data.lessonsLearned * 12) / maxStudyMins).coerceIn(0.1f, 0.9f)
                                            Box(
                                                modifier = Modifier
                                                    .width(7.dp)
                                                    .fillMaxHeight(lessonsHeightFraction)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(Color(0xFF2563EB))
                                            )

                                            // Bar 3: App Opened
                                            val openedHeightFraction = ((data.appOpened * 15) / maxStudyMins).coerceIn(0.1f, 0.85f)
                                            Box(
                                                modifier = Modifier
                                                    .width(7.dp)
                                                    .fillMaxHeight(openedHeightFraction)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(Color(0xFF10B981))
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Day Label
                                    Text(
                                        data.dayLabel,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Details Breakdown Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF4C1D95),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📅 ${selectedDay.fullDate}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text("Day Details", fontSize = 11.sp, color = Color(0xFFD8B4FE))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("App Opened", fontSize = 11.sp, color = Color(0xFFD8B4FE))
                            Text("${selectedDay.appOpened} sessions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Study Duration", fontSize = 11.sp, color = Color(0xFFD8B4FE))
                            Text("${selectedDay.studyMins} mins", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Lessons Learned", fontSize = 11.sp, color = Color(0xFFD8B4FE))
                            Text("${selectedDay.lessonsLearned} completed", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Activity Logs List
        item {
            Text(
                "Recent Learning Activity Logs",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B4B)
            )
        }

        items(
            listOf(
                Triple("Aug 26 - 4:15 PM", "Learned Math (G3): Counting 1-10 in Santali", "85 mins"),
                Triple("Aug 25 - 2:30 PM", "Studied Environmental Science Grade 2", "60 mins"),
                Triple("Aug 24 - 11:00 AM", "Learned Santali Vocabulary & Listening", "45 mins"),
                Triple("Aug 23 - 5:45 PM", "Completed Practice Quiz: Addition Basics", "90 mins")
            )
        ) { (time, title, duration) ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF3E8FF),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            Text(time, fontSize = 11.sp, color = Color(0xFF6B7280))
                        }
                    }
                    Text(duration, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                }
            }
        }
    }
}

// ----------------------------------------------------
// NOTIFICATIONS SCREEN
// ----------------------------------------------------
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val badge: String,
    val time: String,
    val isNewLesson: Boolean = false
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onLearnNewLesson: () -> Unit
) {
    val notifications = remember {
        listOf(
            NotificationItem(
                id = "1",
                title = "🆕 New Lesson Added!",
                message = "Math (Grade 3) - Learn counting 1 to 10 in Santali with native audio pronunciation & interactive games.",
                badge = "NEW LESSON",
                time = "10 mins ago",
                isNewLesson = true
            ),
            NotificationItem(
                id = "2",
                title = "📚 New Storybook Available",
                message = "Santali Folktales Vol. 1 is now added to the Offline Classroom Library.",
                badge = "STORYBOOK",
                time = "2 hours ago"
            ),
            NotificationItem(
                id = "3",
                title = "🔥 7-Day Study Streak Alert!",
                message = "Congratulations Priya! You have maintained an active learning streak for 7 consecutive days.",
                badge = "STREAK",
                time = "Yesterday"
            ),
            NotificationItem(
                id = "4",
                title = "🎧 Offline Sync Complete",
                message = "Environmental Studies Grade 3 audio lessons saved for offline playback.",
                badge = "OFFLINE READY",
                time = "2 days ago"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E1B4B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        Text("New lessons & daily updates", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3E8FF)
                ) {
                    Text(
                        "Mark all read",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        items(notifications) { item ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (item.isNewLesson) Color(0xFF7C3AED) else Color(0xFFE0E7FF)
                        ) {
                            Text(
                                item.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isNewLesson) Color.White else Color(0xFF3730A3),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(item.time, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        item.message,
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 18.sp
                    )

                    if (item.isNewLesson) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onLearnNewLesson,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Come to Learn New Lesson", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeGridTile(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B),
                    lineHeight = 18.sp
                )
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

// ----------------------------------------------------
// 3. VOICE PAGE (NEW LESSON INPUT SCREEN) - MATCHES IMAGE 1 RIGHT
// ----------------------------------------------------
@Composable
fun NewLessonScreen(
    grade: String, onGradeChange: (String) -> Unit,
    area: String, onAreaChange: (String) -> Unit,
    lang: String, onLangChange: (String) -> Unit,
    text: String, onTextChange: (String) -> Unit,
    onBack: () -> Unit,
    onGenerate: () -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    var showGradeMenu by remember { mutableStateOf(false) }
    var showAreaMenu by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.getOrNull(0) ?: ""
            if (spokenText.isNotEmpty()) {
                val updatedText = if (text.isBlank()) spokenText else "$text\n$spokenText"
                onTextChange(updatedText)
                Toast.makeText(context, "Voice captured!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "पाठ विवरण हिंदी में बोलें...")
            }
            speechLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Microphone permission required for speech input", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Bar: Back Arrow & Title "New Lesson (Hindi)"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E1B4B)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "New Lesson (Hindi)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown Filter Selectors Row: [ Grade 3 v ] [ Mathematics v ] [ Santali v ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Grade Selector Chip
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdownChip(label = "$grade ∨") { showGradeMenu = true }
                    DropdownMenu(expanded = showGradeMenu, onDismissRequest = { showGradeMenu = false }) {
                        listOf("Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5").forEach { g ->
                            DropdownMenuItem(text = { Text(g) }, onClick = { onGradeChange(g); showGradeMenu = false })
                        }
                    }
                }

                // Mathematics Selector Chip
                Box(modifier = Modifier.weight(1.3f)) {
                    FilterDropdownChip(label = "$area ∨") { showAreaMenu = true }
                    DropdownMenu(expanded = showAreaMenu, onDismissRequest = { showAreaMenu = false }) {
                        listOf("Mathematics", "Literacy", "Science", "EVS").forEach { a ->
                            DropdownMenuItem(text = { Text(a) }, onClick = { onAreaChange(a); showAreaMenu = false })
                        }
                    }
                }

                // Santali Target Language Chip
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdownChip(label = "$lang ∨") { showLangMenu = true }
                    DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
                        listOf("Santali", "Ho", "Mundari").forEach { l ->
                            DropdownMenuItem(text = { Text(l) }, onClick = { onLangChange(l); showLangMenu = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Central Pulsing Microphone Visualizer (Matches Image 1 Right Screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Waveform Audio Animation Lines radiating left and right
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Waveform Lines
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(6) { i ->
                            val h = if (isListening) listOf(24, 40, 18, 50, 30, 16)[i] else listOf(12, 20, 10, 24, 14, 8)[i]
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDDD6FE))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(90.dp))

                    // Right Waveform Lines
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(6) { i ->
                            val h = if (isListening) listOf(16, 30, 50, 18, 40, 24)[i] else listOf(8, 14, 24, 10, 20, 12)[i]
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDDD6FE))
                            )
                        }
                    }
                }

                // Outer Glowing Purple Concentric Rings
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(0x1F8B5CF6))
                )
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .clip(CircleShape)
                        .background(Color(0x3D7C3AED))
                )

                // Main Microphone Button Node
                val micScale by animateFloatAsState(if (isListening) 1.15f else 1.0f, label = "micScale")
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .scale(micScale)
                        .size(88.dp)
                        .clickable {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                isListening = true
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "पाठ विवरण हिंदी में बोलें...")
                                }
                                speechLauncher.launch(intent)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone Input",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            Text(
                "Tap microphone to speak\nHindi explanation...",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Real-Time Speech / Text Transcription Box (Matches Image 1)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "LIVE TRANSCRIPTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                        if (text.isNotEmpty()) {
                            Text(
                                "Clear",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.clickable { onTextChange("") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        placeholder = { Text("गिनो: एक, दो, तीन, चार, पाँच\nCount: 1, 2, 3, 4, 5", color = Color(0xFF9CA3AF)) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1B4B))
                    )
                }
            }
        }

        // Bottom Action Button: GENERATE LESSON (Santali) ->
        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(bottom = 8.dp)
                .shadow(10.dp, RoundedCornerShape(27.dp), spotColor = Color(0xFF7C3AED)),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            enabled = text.isNotBlank()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "GENERATE LESSON ($lang)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDropdownChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = 1.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ----------------------------------------------------
// 4. PROCESSING SCREEN WITH ANIMATED AI ORB
// ----------------------------------------------------
@Composable
fun ProcessingScreen(
    grade: String, area: String, lang: String, text: String,
    onSuccess: (ProcessLessonResponse) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(1) }

    val infiniteTransition = rememberInfiniteTransition(label = "aiPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(500)
            currentStep = 2
            delay(600)
            currentStep = 3

            try {
                val req = ProcessLessonRequest(
                    grade = grade.filter { it.isDigit() }.toIntOrNull() ?: 3,
                    learning_area = area,
                    target_language = lang,
                    text = text
                )
                val res = RetrofitClient.apiService.processLesson(req)
                currentStep = 4
                delay(300)
                onSuccess(res)
            } catch (e: Exception) {
                // Seamless local AI fallback when backend server is offline locally
                val fallback = RetrofitClient.generateLocalFallback(
                    ProcessLessonRequest(
                        grade = grade.filter { it.isDigit() }.toIntOrNull() ?: 3,
                        learning_area = area,
                        target_language = lang,
                        text = text
                    )
                )
                currentStep = 4
                delay(300)
                onSuccess(fallback)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Purple Orb
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(130.dp)
                .scale(pulseScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9), Color.Transparent)
                    ),
                    CircleShape
                )
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C3AED), strokeWidth = 3.dp)
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(32.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Synthesizing $lang Lesson",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1B4B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Process Steps Timeline
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            ProcessStepRow(step = 1, title = "Analyzing Hindi Text & Syntax", activeStep = currentStep)
            ProcessStepRow(step = 2, title = "IndicTrans2 Neural Model Translation", activeStep = currentStep)
            ProcessStepRow(step = 3, title = "Indic Parler-TTS Audio Synthesis", activeStep = currentStep)
            ProcessStepRow(step = 4, title = "Building Interactive Audio Package", activeStep = currentStep)
        }
    }
}

@Composable
fun ProcessStepRow(step: Int, title: String, activeStep: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (activeStep > step) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
        } else if (activeStep == step) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF7C3AED))
        } else {
            Icon(Icons.Outlined.RadioButtonUnchecked, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            fontSize = 14.sp,
            color = if (activeStep >= step) Color(0xFF1E1B4B) else Color(0xFF9CA3AF),
            fontWeight = if (activeStep == step) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ----------------------------------------------------
// 5. RESULT & AUDIO PLAYER SCREEN
// ----------------------------------------------------
@Composable
fun ResultScreen(
    grade: String,
    subject: String,
    response: ProcessLessonResponse?,
    onSave: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isSaved by remember { mutableStateOf(false) }

    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Lesson Package Ready", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Text("$grade • $subject", fontSize = 13.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onHome) { Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1E1B4B)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Original Hindi Text Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HINDI SOURCE TEXT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                        IconButton(
                            onClick = {
                                response?.source_text?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    Toast.makeText(context, "Hindi text copied!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(response?.source_text ?: "", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1B4B))
                }
            }
        }

        // Santali Translation Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6D28D9), Color(0xFF4C1D95))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFFA78BFA), modifier = Modifier.size(10.dp)) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("TRANSLATED LESSON", fontSize = 11.sp, color = Color(0xFFDDD6FE), fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = {
                                    response?.translated_text?.let {
                                        clipboardManager.setText(AnnotatedString(it))
                                        Toast.makeText(context, "Translated text copied!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            response?.translated_text ?: "",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 26.sp
                        )
                    }
                }
            }
        }

        // ExoPlayer & Equalizer Audio Visualizer Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF7C3AED))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Classroom Speech Player", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        }
                        Text("${playbackSpeed}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated 16-bar Waveform Visualizer
                    AudioWaveformVisualizer(isPlaying = isPlaying)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Playback Controls Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Speed Toggle Button
                        IconButton(onClick = {
                            playbackSpeed = when (playbackSpeed) {
                                0.8f -> 1.0f
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                else -> 0.8f
                            }
                            exoPlayer.setPlaybackSpeed(playbackSpeed)
                        }) {
                            Text("${playbackSpeed}x", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                        }

                        // Play/Pause Main Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF7C3AED),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    if (isPlaying) {
                                        exoPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        response?.audio_file?.let { url ->
                                            val mediaItem = MediaItem.fromUri(url)
                                            exoPlayer.setMediaItem(mediaItem)
                                            exoPlayer.prepare()
                                        }
                                        exoPlayer.play()
                                        isPlaying = true
                                    }
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

                        // Share Button
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Vistaar Setu Lesson: $grade")
                                putExtra(Intent.EXTRA_TEXT, "Hindi: ${response?.source_text}\nSantali: ${response?.translated_text}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Lesson"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF6B7280))
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Save & Create Another)
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            val db = AppDatabase.getDatabase(context)
                            db.lessonDao().insertLesson(
                                SavedLesson(
                                    title = response?.source_text?.take(30) ?: "Santali Lesson",
                                    grade = grade,
                                    targetLanguage = response?.target_language ?: "Santali",
                                    hindiText = response?.source_text ?: "",
                                    translatedText = response?.translated_text ?: "",
                                    audioUrl = response?.audio_file
                                )
                            )
                            isSaved = true
                            Toast.makeText(context, "Saved to Offline Library!", Toast.LENGTH_SHORT).show()
                            onSave()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSaved) Color(0xFF10B981) else Color(0xFF7C3AED), contentColor = Color.White)
                ) {
                    Icon(if (isSaved) Icons.Default.Check else Icons.Default.BookmarkAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSaved) "Saved" else "Save Offline", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Lesson", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Animated 16-bar Waveform Visualizer
@Composable
fun AudioWaveformVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val bars = 16

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(bars) { index ->
            val duration = 400 + (index * 60)
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight"
            )

            val currentHeight = if (isPlaying) animatedHeight else 0.25f

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(currentHeight)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                        )
                    )
            )
        }
    }
}

// ----------------------------------------------------
// 6. SAVED LESSONS SCREEN (OFFLINE LIBRARY)
// ----------------------------------------------------
@Composable
fun SavedLessonsScreen(onBackHome: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var savedList by remember { mutableStateOf<List<SavedLesson>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    fun refreshList() {
        scope.launch {
            val db = AppDatabase.getDatabase(context)
            savedList = if (searchQuery.isBlank()) {
                db.lessonDao().getAllLessons()
            } else {
                db.lessonDao().searchLessons(searchQuery)
            }
        }
    }

    LaunchedEffect(searchQuery) {
        refreshList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Offline Library", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Text("Saved Santali lessons for offline teaching", fontSize = 12.sp, color = Color(0xFF6B7280))
            }
            IconButton(onClick = onBackHome) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E1B4B)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search lessons...", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF7C3AED)) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) } }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (savedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFC4B5FD))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No saved offline lessons found.", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
                    Text("Create a new lesson in Voice Input and tap Save Offline!", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(savedList, key = { it.id }) { lesson ->
                    SavedLessonCard(
                        lesson = lesson,
                        onDelete = {
                            scope.launch {
                                val db = AppDatabase.getDatabase(context)
                                db.lessonDao().deleteLesson(lesson)
                                refreshList()
                                Toast.makeText(context, "Lesson removed from library", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedLessonCard(lesson: SavedLesson, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDE9FE),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(lesson.grade, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Text(lesson.targetLanguage, fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Hindi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
            Text(lesson.hindiText, fontSize = 13.sp, color = Color(0xFF374151), maxLines = if (expanded) 10 else 2, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Translation:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
            Text(lesson.translatedText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B), maxLines = if (expanded) 10 else 3, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Show Less" else "Read Full Lesson", fontSize = 12.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}