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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

import com.vistaarsetu.app.R
import com.vistaarsetu.app.data.AppDatabase
import com.vistaarsetu.app.data.ProcessLessonRequest
import com.vistaarsetu.app.data.ProcessLessonResponse
import com.vistaarsetu.app.data.RetrofitClient
import com.vistaarsetu.app.data.SavedLesson

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// ============================================================
// SCREEN ROUTING
// ============================================================

enum class Screen {
    WELCOME,
    HOME,
    NEW_LESSON,
    PROCESSING,
    RESULT,
    SAVED_LESSONS,
    NOTIFICATIONS
}


// ============================================================
// MAIN NAVIGATION
// ============================================================

@Composable
fun MainAppNavigation() {

    var currentScreen by remember {
        mutableStateOf(Screen.WELCOME)
    }

    var inputGrade by remember {
        mutableStateOf("Grade 3")
    }

    var inputArea by remember {
        mutableStateOf("Mathematics")
    }

    var inputLang by remember {
        mutableStateOf("Santali")
    }

    var inputHindiText by remember {
        mutableStateOf(
            "गिनो: एक, दो, तीन, चार, पाँच\nCount: 1, 2, 3, 4, 5"
        )
    }

    var lastResponse by remember {
        mutableStateOf<ProcessLessonResponse?>(null)
    }


    Scaffold(
        bottomBar = {

            if (
                currentScreen != Screen.WELCOME &&
                currentScreen != Screen.PROCESSING
            ) {

                VistaarBottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = {
                        currentScreen = it
                    }
                )
            }
        },

        containerColor = Color(0xFFF5F3FF)

    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when (currentScreen) {

                // ------------------------------------------------
                // WELCOME
                // ------------------------------------------------

                Screen.WELCOME -> {

                    WelcomeScreen(
                        onGetStarted = {
                            currentScreen = Screen.HOME
                        }
                    )
                }


                // ------------------------------------------------
                // HOME
                // ------------------------------------------------

                Screen.HOME -> {

                    HomeScreen(
                        onNewLesson = {
                            currentScreen = Screen.NEW_LESSON
                        },

                        onViewSaved = {
                            currentScreen = Screen.SAVED_LESSONS
                        },

                        onPlayRecent = {

                            inputHindiText =
                                "गिनो: एक, दो, तीन, चार, पाँच\nCount: 1, 2, 3, 4, 5"

                            currentScreen = Screen.PROCESSING
                        },

                        onOpenNotifications = {
                            currentScreen = Screen.NOTIFICATIONS
                        }
                    )
                }


                // ------------------------------------------------
                // NEW LESSON
                // ------------------------------------------------

                Screen.NEW_LESSON -> {

                    NewLessonScreen(
                        grade = inputGrade,
                        onGradeChange = {
                            inputGrade = it
                        },

                        area = inputArea,
                        onAreaChange = {
                            inputArea = it
                        },

                        lang = inputLang,
                        onLangChange = {
                            inputLang = it
                        },

                        text = inputHindiText,
                        onTextChange = {
                            inputHindiText = it
                        },

                        onBack = {
                            currentScreen = Screen.HOME
                        },

                        onGenerate = {
                            currentScreen = Screen.PROCESSING
                        }
                    )
                }


                // ------------------------------------------------
                // PROCESSING
                // ------------------------------------------------

                Screen.PROCESSING -> {

                    ProcessingScreen(
                        grade = inputGrade,
                        area = inputArea,
                        lang = inputLang,
                        text = inputHindiText,

                        onSuccess = { response ->

                            lastResponse = response

                            currentScreen = Screen.RESULT
                        }
                    )
                }


                // ------------------------------------------------
                // RESULT
                // ------------------------------------------------

                Screen.RESULT -> {

                    ResultScreen(
                        grade = inputGrade,
                        subject = inputArea,
                        response = lastResponse,

                        onSave = {
                            currentScreen = Screen.SAVED_LESSONS
                        },

                        onHome = {
                            currentScreen = Screen.HOME
                        }
                    )
                }


                // ------------------------------------------------
                // OFFLINE LIBRARY
                // ------------------------------------------------

                Screen.SAVED_LESSONS -> {

                    SavedLessonsScreen(
                        onBackHome = {
                            currentScreen = Screen.HOME
                        }
                    )
                }


                // ------------------------------------------------
                // NOTIFICATIONS
                // ------------------------------------------------

                Screen.NOTIFICATIONS -> {

                    NotificationsScreen(
                        onBack = {
                            currentScreen = Screen.HOME
                        },

                        onLearnNewLesson = {
                            currentScreen = Screen.NEW_LESSON
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// BOTTOM NAVIGATION
// ============================================================

@Composable
fun VistaarBottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {

    Surface(
        color = Color.White,
        shadowElevation = 12.dp,
        modifier = Modifier.clip(
            RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            )
        )
    ) {

        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {

            NavigationBarItem(
                selected = currentScreen == Screen.HOME,
                onClick = {
                    onScreenSelected(Screen.HOME)
                },

                icon = {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home"
                    )
                },

                label = {
                    Text(
                        "Home",
                        fontSize = 11.sp
                    )
                }
            )


            NavigationBarItem(
                selected = currentScreen == Screen.NEW_LESSON,
                onClick = {
                    onScreenSelected(Screen.NEW_LESSON)
                },

                icon = {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice Input"
                    )
                },

                label = {
                    Text(
                        "Voice Input",
                        fontSize = 11.sp
                    )
                }
            )


            NavigationBarItem(
                selected = currentScreen == Screen.RESULT,
                onClick = {
                    onScreenSelected(Screen.RESULT)
                },

                icon = {
                    Icon(
                        Icons.Default.Headphones,
                        contentDescription = "Player"
                    )
                },

                label = {
                    Text(
                        "Player",
                        fontSize = 11.sp
                    )
                }
            )


            NavigationBarItem(
                selected = currentScreen == Screen.SAVED_LESSONS,
                onClick = {
                    onScreenSelected(Screen.SAVED_LESSONS)
                },

                icon = {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = "Library"
                    )
                },

                label = {
                    Text(
                        "Library",
                        fontSize = 11.sp
                    )
                }
            )
        }
    }
}


// ============================================================
// 1. WELCOME SCREEN
// ============================================================

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 24.dp,
                vertical = 20.dp
            ),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Spacer(
            modifier = Modifier.height(10.dp)
        )


        // ------------------------------------------------------
        // ACTUAL VISTAAR SETU LOGO
        // ------------------------------------------------------

        ImageCard(
            imageRes = R.drawable.logo_vistaar_setu,
            contentDescription = "Vistaar Setu Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ------------------------------------------------------
        // ACTUAL CLASSROOM ILLUSTRATION
        // ------------------------------------------------------

        AsyncImageCard(
            imageRes = R.drawable.illustration_classroom,
            contentDescription = "Teacher and students communicating across languages",
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        Text(
            "Connecting Classrooms\nwith Local Languages",

            fontSize = 21.sp,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF1E1B4B),

            textAlign = TextAlign.Center,

            lineHeight = 27.sp
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            "AI-powered audio lessons in tribal & regional languages",

            fontSize = 13.sp,

            fontWeight = FontWeight.Medium,

            color = Color(0xFF6B7280),

            textAlign = TextAlign.Center
        )


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ------------------------------------------------------
        // GET STARTED
        // ------------------------------------------------------

        Button(
            onClick = onGetStarted,

            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(54.dp)
                .shadow(
                    10.dp,
                    RoundedCornerShape(27.dp)
                ),

            shape = RoundedCornerShape(27.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C3AED)
            )
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "GET STARTED",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )
    }
}


// ============================================================
// IMAGE CARD
// ============================================================

@Composable
fun ImageCard(
    imageRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {

    Surface(
        shape = RoundedCornerShape(24.dp),

        color = Color.White,

        shadowElevation = 2.dp,

        modifier = modifier
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),

            contentAlignment = Alignment.Center
        ) {

            androidx.compose.foundation.Image(
                painter = painterResource(imageRes),

                contentDescription = contentDescription,

                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),

                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
    }
}


// ============================================================
// ASYNC IMAGE CARD  (decodes bitmap on IO thread to avoid
// main-thread jank for large PNGs like illustration_classroom)
// ============================================================

@Composable
fun AsyncImageCard(
    imageRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Decode the bitmap on the IO dispatcher so the main thread is never blocked.
    val painter by produceState<androidx.compose.ui.graphics.painter.Painter?>(
        initialValue = null,
        key1 = imageRes
    ) {
        value = withContext(Dispatchers.IO) {
            val bmp = BitmapFactory.decodeResource(
                context.resources,
                imageRes
            )
            if (bmp != null) BitmapPainter(bmp.asImageBitmap()) else null
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (painter != null) {
                androidx.compose.foundation.Image(
                    painter = painter!!,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            } else {
                // Lightweight placeholder shown while the bitmap decodes off-thread
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEDE9FE))
                )
            }
        }
    }
}


// ============================================================
// 2. HOME SCREEN
// ============================================================

@Composable
fun HomeScreen(
    onNewLesson: () -> Unit,
    onViewSaved: () -> Unit,
    onPlayRecent: () -> Unit,
    onOpenNotifications: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),

        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 24.dp
        ),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        // ------------------------------------------------------
        // HEADER
        // ------------------------------------------------------

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    ImageCard(
                        imageRes =
                            R.drawable.logo_vistaar_setu,

                        contentDescription =
                            "Vistaar Setu",

                        modifier = Modifier
                            .size(52.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Column {

                        Text(
                            "Vistaar Setu",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )

                        Text(
                            "Bridging Languages",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }


                Box {

                    IconButton(
                        onClick =
                            onOpenNotifications
                    ) {

                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription =
                                "Notifications",

                            tint =
                                Color(0xFF1E1B4B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0xFFEF4444)
                            )
                            .align(Alignment.TopEnd)
                            .offset(
                                x = (-6).dp,
                                y = 6.dp
                            )
                    )
                }
            }
        }


        // ------------------------------------------------------
        // SEARCH
        // ------------------------------------------------------

        item {

            Surface(
                shape =
                    RoundedCornerShape(16.dp),

                color = Color.White,

                shadowElevation = 2.dp,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF)
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        "Search lessons...",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }


        // ------------------------------------------------------
        // HERO
        // ------------------------------------------------------

        item {
            HomeHeroBanner()
        }


        // ------------------------------------------------------
        // ACTION GRID
        // ------------------------------------------------------

        item {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    HomeGridTile(
                        modifier =
                            Modifier.weight(1f),

                        title = "New Lesson",

                        onClick =
                            onNewLesson
                    ) {

                        Surface(
                            shape = CircleShape,

                            color =
                                Color(0xFF7C3AED),

                            modifier =
                                Modifier.size(54.dp)
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    Icons.Default.Add,
                                    contentDescription =
                                        "New Lesson",

                                    tint = Color.White,

                                    modifier =
                                        Modifier.size(32.dp)
                                )
                            }
                        }
                    }


                    HomeGridTile(
                        modifier =
                            Modifier.weight(1f),

                        title = "Offline Library",

                        onClick =
                            onViewSaved
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,

                            contentDescription =
                                "Offline Library",

                            tint =
                                Color(0xFF7C3AED),

                            modifier =
                                Modifier.size(42.dp)
                        )
                    }
                }


                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    // ------------------------------------------
                    // RECENT LESSON
                    // ------------------------------------------

                    Surface(
                        shape =
                            RoundedCornerShape(20.dp),

                        color =
                            Color(0xFF4C1D95),

                        shadowElevation = 4.dp,

                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable(
                                onClick =
                                    onPlayRecent
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxSize(),

                            verticalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                "Recent:\nMath (G3)",

                                fontSize = 13.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color = Color.White
                            )


                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(3.dp)
                            ) {

                                repeat(12) { index ->

                                    Box(
                                        modifier =
                                            Modifier
                                                .width(3.dp)
                                                .height(
                                                    listOf(
                                                        10, 20, 14,
                                                        28, 16, 22,
                                                        12, 26, 18,
                                                        10, 22, 14
                                                    )[index].dp
                                                )
                                                .clip(
                                                    CircleShape
                                                )
                                                .background(
                                                    Color(0xFFC4B5FD)
                                                )
                                    )
                                }
                            }


                            Surface(
                                shape =
                                    RoundedCornerShape(12.dp),

                                color =
                                    Color.White,

                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Box(
                                    contentAlignment =
                                        Alignment.Center,

                                    modifier =
                                        Modifier.padding(
                                            vertical = 6.dp
                                        )
                                ) {

                                    Text(
                                        "PLAY",

                                        fontSize = 12.sp,

                                        fontWeight =
                                            FontWeight.ExtraBold,

                                        color =
                                            Color(0xFF4C1D95)
                                    )
                                }
                            }
                        }
                    }


                    // ------------------------------------------
                    // REPLACEMENT FOR STUDENT PROGRESS
                    // ------------------------------------------

                    SupportedLanguagesTile(
                        modifier =
                            Modifier.weight(1f),

                        onClick =
                            onNewLesson
                    )
                }
            }
        }
    }
}


// ============================================================
// HERO BANNER
// ============================================================

@Composable
fun HomeHeroBanner() {

    val slides = remember {

        listOf(
            Pair(
                "Learn in Local Languages",
                "Hindi → Santali audio lessons"
            ),

            Pair(
                "Teach Through Voice",
                "Speak naturally and generate lessons"
            ),

            Pair(
                "Offline Classroom Ready",
                "Save lessons for later use"
            )
        )
    }

    var currentIndex by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit) {

        while (true) {

            delay(3500)

            currentIndex =
                (currentIndex + 1) % slides.size
        }
    }

    val slide =
        slides[currentIndex]


    Surface(
        shape =
            RoundedCornerShape(24.dp),

        shadowElevation = 5.dp,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        AnimatedContent(

            targetState = slide,

            transitionSpec = {

                (
                        slideInHorizontally {
                            it
                        } + fadeIn()
                        ).togetherWith(

                        slideOutHorizontally {
                            -it
                        } + fadeOut()
                    )
            },

            label = "hero"
        ) { current ->

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF6D28D9),
                                Color(0xFF8B5CF6)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            current.first,

                            fontSize = 20.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color = Color.White,

                            lineHeight = 26.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(
                            current.second,

                            fontSize = 12.sp,

                            color =
                                Color.White.copy(
                                    alpha = 0.88f
                                )
                        )
                    }


                    ImageCard(
                        imageRes =
                            R.drawable.illustration_classroom,

                        contentDescription =
                            null,

                        modifier =
                            Modifier
                                .size(105.dp)
                    )
                }
            }
        }
    }
}


// ============================================================
// SUPPORTED LANGUAGES TILE
// ============================================================

@Composable
fun SupportedLanguagesTile(
    modifier: Modifier,
    onClick: () -> Unit
) {

    Surface(
        shape =
            RoundedCornerShape(20.dp),

        color = Color.White,

        shadowElevation = 4.dp,

        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick)
    ) {

        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),

            verticalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                "Supported\nLanguages",

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF1E1B4B)
            )


            Column(
                verticalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {

                LanguageChip(
                    "Santali",
                    "ᱥᱟᱱᱛᱟᱲᱤ"
                )

                LanguageChip(
                    "Ho",
                    "Warang Citi"
                )

                LanguageChip(
                    "Mundari",
                    "Mundari Bani"
                )
            }
        }
    }
}


@Composable
fun LanguageChip(
    name: String,
    script: String
) {

    Surface(
        shape =
            RoundedCornerShape(10.dp),

        color =
            Color(0xFFF3E8FF)
    ) {

        Row(
            modifier =
                Modifier.padding(
                    horizontal = 7.dp,
                    vertical = 4.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                name,

                fontSize = 9.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF7C3AED)
            )

            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )

            Text(
                script,

                fontSize = 8.sp,

                color =
                    Color(0xFF6B7280)
            )
        }
    }
}


// ============================================================
// HOME GRID TILE
// ============================================================

@Composable
fun HomeGridTile(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {

    Surface(
        shape =
            RoundedCornerShape(20.dp),

        color = Color.White,

        shadowElevation = 4.dp,

        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick)
    ) {

        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),

            verticalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                title,

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF1E1B4B)
            )


            Box(
                modifier =
                    Modifier.fillMaxWidth(),

                contentAlignment =
                    Alignment.Center
            ) {
                content()
            }
        }
    }
}


// ============================================================
// 3. NEW LESSON SCREEN
// ============================================================

@Composable
fun NewLessonScreen(
    grade: String,
    onGradeChange: (String) -> Unit,

    area: String,
    onAreaChange: (String) -> Unit,

    lang: String,
    onLangChange: (String) -> Unit,

    text: String,
    onTextChange: (String) -> Unit,

    onBack: () -> Unit,
    onGenerate: () -> Unit
) {

    val context =
        LocalContext.current

    var isListening by remember {
        mutableStateOf(false)
    }

    var showGradeMenu by remember {
        mutableStateOf(false)
    }

    var showAreaMenu by remember {
        mutableStateOf(false)
    }

    var showLangMenu by remember {
        mutableStateOf(false)
    }


    val speechLauncher =
        rememberLauncherForActivityResult(

            ActivityResultContracts.StartActivityForResult()

        ) { result ->

            isListening = false

            if (
                result.resultCode ==
                Activity.RESULT_OK
            ) {

                val spokenText =
                    result.data
                        ?.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                        )
                        ?.getOrNull(0)
                        ?: ""

                if (spokenText.isNotEmpty()) {

                    val updatedText =
                        if (text.isBlank()) {

                            spokenText

                        } else {

                            "$text\n$spokenText"
                        }

                    onTextChange(updatedText)

                    Toast.makeText(
                        context,
                        "Voice captured!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    val permissionLauncher =
        rememberLauncherForActivityResult(

            ActivityResultContracts.RequestPermission()

        ) { granted ->

            if (granted) {

                isListening = true

                val intent =
                    Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                    ).apply {

                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )

                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE,
                            "hi-IN"
                        )

                        putExtra(
                            RecognizerIntent.EXTRA_PROMPT,
                            "पाठ विवरण हिंदी में बोलें..."
                        )
                    }

                speechLauncher.launch(intent)

            } else {

                Toast.makeText(
                    context,
                    "Microphone permission required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            // --------------------------------------------------
            // TOP BAR
            // --------------------------------------------------

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E1B4B)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    "New Lesson (Hindi)",

                    fontSize = 18.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color(0xFF1E1B4B)
                )
            }


            // --------------------------------------------------
            // FILTERS
            // --------------------------------------------------

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Box(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    FilterDropdownChip(
                        label = "$grade ▼"
                    ) {
                        showGradeMenu = true
                    }

                    DropdownMenu(
                        expanded =
                            showGradeMenu,

                        onDismissRequest = {
                            showGradeMenu = false
                        }
                    ) {

                        listOf(
                            "Grade 1",
                            "Grade 2",
                            "Grade 3",
                            "Grade 4",
                            "Grade 5"
                        ).forEach { value ->

                            DropdownMenuItem(
                                text = {
                                    Text(value)
                                },

                                onClick = {
                                    onGradeChange(value)
                                    showGradeMenu = false
                                }
                            )
                        }
                    }
                }


                Box(
                    modifier =
                        Modifier.weight(1.3f)
                ) {

                    FilterDropdownChip(
                        label = "$area ▼"
                    ) {
                        showAreaMenu = true
                    }

                    DropdownMenu(
                        expanded =
                            showAreaMenu,

                        onDismissRequest = {
                            showAreaMenu = false
                        }
                    ) {

                        listOf(
                            "Mathematics",
                            "Literacy",
                            "Science",
                            "EVS"
                        ).forEach { value ->

                            DropdownMenuItem(
                                text = {
                                    Text(value)
                                },

                                onClick = {
                                    onAreaChange(value)
                                    showAreaMenu = false
                                }
                            )
                        }
                    }
                }


                Box(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    FilterDropdownChip(
                        label = "$lang ▼"
                    ) {
                        showLangMenu = true
                    }

                    DropdownMenu(
                        expanded =
                            showLangMenu,

                        onDismissRequest = {
                            showLangMenu = false
                        }
                    ) {

                        listOf(
                            "Santali",
                            "Ho",
                            "Mundari"
                        ).forEach { value ->

                            DropdownMenuItem(
                                text = {
                                    Text(value)
                                },

                                onClick = {
                                    onLangChange(value)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )


            // --------------------------------------------------
            // MICROPHONE
            // --------------------------------------------------

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(190.dp),

                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0x1F8B5CF6)
                            )
                )


                Box(
                    modifier =
                        Modifier
                            .size(124.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0x3D7C3AED)
                            )
                )


                val micScale by
                animateFloatAsState(
                    if (isListening)
                        1.12f
                    else
                        1f,

                    label = "micScale"
                )


                Surface(
                    shape = CircleShape,

                    color =
                        Color(0xFF7C3AED),

                    shadowElevation = 12.dp,

                    modifier =
                        Modifier
                            .size(88.dp)
                            .scale(micScale)
                            .clickable {

                                val permission =
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    )

                                if (
                                    permission ==
                                    PackageManager.PERMISSION_GRANTED
                                ) {

                                    isListening = true

                                    val intent =
                                        Intent(
                                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                                        ).apply {

                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                            )

                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE,
                                                "hi-IN"
                                            )

                                            putExtra(
                                                RecognizerIntent.EXTRA_PROMPT,
                                                "पाठ विवरण हिंदी में बोलें..."
                                            )
                                        }

                                    speechLauncher.launch(intent)

                                } else {

                                    permissionLauncher.launch(
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                }
                            }
                ) {

                    Box(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.Mic,

                            contentDescription =
                                "Speak Hindi",

                            tint = Color.White,

                            modifier =
                                Modifier.size(42.dp)
                        )
                    }
                }
            }


            Text(
                "Tap microphone to speak\nHindi explanation...",

                fontSize = 13.sp,

                fontWeight =
                    FontWeight.Medium,

                color =
                    Color(0xFF6B7280),

                textAlign =
                    TextAlign.Center,

                modifier =
                    Modifier.fillMaxWidth()
            )


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            // --------------------------------------------------
            // TRANSCRIPTION
            // --------------------------------------------------

            Surface(
                shape =
                    RoundedCornerShape(16.dp),

                color = Color.White,

                border =
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFFDDD6FE)
                    ),

                shadowElevation = 2.dp,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp)
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            "LIVE TRANSCRIPTION",

                            fontSize = 11.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color(0xFF7C3AED)
                        )


                        if (text.isNotEmpty()) {

                            Text(
                                "Clear",

                                fontSize = 11.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    Color(0xFFEF4444),

                                modifier =
                                    Modifier.clickable {
                                        onTextChange("")
                                    }
                            )
                        }
                    }


                    OutlinedTextField(
                        value = text,

                        onValueChange =
                            onTextChange,

                        modifier =
                            Modifier.fillMaxWidth(),

                        placeholder = {

                            Text(
                                "गिनो: एक, दो, तीन, चार, पाँच\nCount: 1, 2, 3, 4, 5",

                                color =
                                    Color(0xFF9CA3AF)
                            )
                        },

                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor =
                                    Color.Transparent,

                                focusedBorderColor =
                                    Color.Transparent,

                                unfocusedContainerColor =
                                    Color.Transparent,

                                focusedContainerColor =
                                    Color.Transparent
                            )
                    )
                }
            }
        }


        // ------------------------------------------------------
        // GENERATE
        // ------------------------------------------------------

        Button(
            onClick =
                onGenerate,

            enabled =
                text.isNotBlank(),

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(54.dp),

            shape =
                RoundedCornerShape(27.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF7C3AED)
                )
        ) {

            Text(
                "GENERATE LESSON ($lang)",

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold,

                color = Color.White
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null
            )
        }
    }
}


// ============================================================
// FILTER CHIP
// ============================================================

@Composable
fun FilterDropdownChip(
    label: String,
    onClick: () -> Unit
) {

    Surface(
        shape =
            RoundedCornerShape(12.dp),

        color = Color.White,

        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFFE5E7EB)
            ),

        modifier =
            Modifier.clickable(onClick = onClick)
    ) {

        Box(
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 8.dp
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                label,

                fontSize = 12.sp,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    Color(0xFF374151),

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}


// ============================================================
// 4. PROCESSING SCREEN
// ============================================================

@Composable
fun ProcessingScreen(
    grade: String,
    area: String,
    lang: String,
    text: String,
    onSuccess: (ProcessLessonResponse) -> Unit
) {

    var currentStep by remember {
        mutableStateOf(1)
    }


    val infiniteTransition =
        rememberInfiniteTransition(
            label = "processing"
        )


    val pulseScale by
    infiniteTransition.animateFloat(
        initialValue = 0.9f,

        targetValue = 1.1f,

        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(1000),

                repeatMode =
                    RepeatMode.Reverse
            ),

        label = "pulse"
    )


    LaunchedEffect(Unit) {

        currentStep = 1

        delay(700)

        currentStep = 2

        delay(700)

        currentStep = 3


        try {

            val request =
                ProcessLessonRequest(

                    grade =
                        grade
                            .filter {
                                it.isDigit()
                            }
                            .toIntOrNull()
                            ?: 3,

                    learning_area =
                        area,

                    target_language =
                        lang,

                    text =
                        text
                )


            val response =
                RetrofitClient
                    .apiService
                    .processLesson(request)


            currentStep = 4

            delay(300)

            onSuccess(response)

        } catch (e: Exception) {

            // -----------------------------------------------
            // OFFLINE FALLBACK
            // -----------------------------------------------

            val fallback =
                RetrofitClient.generateLocalFallback(

                    ProcessLessonRequest(

                        grade =
                            grade
                                .filter {
                                    it.isDigit()
                                }
                                .toIntOrNull()
                                ?: 3,

                        learning_area =
                            area,

                        target_language =
                            lang,

                        text =
                            text
                    )
                )


            currentStep = 4

            delay(300)

            onSuccess(fallback)
        }
    }


    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFFF5F3FF)
                )
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Box(
            modifier =
                Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFF6D28D9)
                            )
                        )
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Surface(
                shape = CircleShape,

                color = Color.White,

                modifier =
                    Modifier.size(80.dp)
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color =
                            Color(0xFF7C3AED),

                        strokeWidth =
                            3.dp
                    )

                    Icon(
                        Icons.Default.AutoAwesome,

                        contentDescription = null,

                        tint =
                            Color(0xFF7C3AED),

                        modifier =
                            Modifier.size(30.dp)
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(28.dp)
        )


        Text(
            "Creating $lang Lesson",

            fontSize = 22.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                Color(0xFF1E1B4B)
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        Column(
            modifier =
                Modifier.fillMaxWidth(0.88f),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            ProcessStepRow(
                1,
                "Analyzing Hindi Text",
                currentStep
            )

            ProcessStepRow(
                2,
                "IndicTrans2 Translation",
                currentStep
            )

            ProcessStepRow(
                3,
                "Indic Parler-TTS Audio",
                currentStep
            )

            ProcessStepRow(
                4,
                "Building Lesson Package",
                currentStep
            )
        }
    }
}


// ============================================================
// PROCESS STEP
// ============================================================

@Composable
fun ProcessStepRow(
    step: Int,
    title: String,
    activeStep: Int
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        when {

            activeStep > step -> {

                Icon(
                    Icons.Default.CheckCircle,

                    contentDescription = null,

                    tint =
                        Color(0xFF10B981),

                    modifier =
                        Modifier.size(20.dp)
                )
            }

            activeStep == step -> {

                CircularProgressIndicator(
                    modifier =
                        Modifier.size(18.dp),

                    strokeWidth =
                        2.dp,

                    color =
                        Color(0xFF7C3AED)
                )
            }

            else -> {

                Icon(
                    Icons.Default.RadioButtonUnchecked,

                    contentDescription = null,

                    tint =
                        Color(0xFF9CA3AF),

                    modifier =
                        Modifier.size(20.dp)
                )
            }
        }


        Spacer(
            modifier =
                Modifier.width(12.dp)
        )


        Text(
            title,

            fontSize = 14.sp,

            color =
                if (activeStep >= step)
                    Color(0xFF1E1B4B)
                else
                    Color(0xFF9CA3AF),

            fontWeight =
                if (activeStep == step)
                    FontWeight.Bold
                else
                    FontWeight.Normal
        )
    }
}


// ============================================================
// 5. RESULT SCREEN
// ============================================================

@Composable
fun ResultScreen(
    grade: String,
    subject: String,
    response: ProcessLessonResponse?,
    onSave: () -> Unit,
    onHome: () -> Unit
) {

    val context =
        LocalContext.current

    val clipboard =
        LocalClipboardManager.current

    val scope =
        rememberCoroutineScope()

    var isPlaying by remember {
        mutableStateOf(false)
    }

    var playbackSpeed by remember {
        mutableFloatStateOf(1f)
    }

    var isSaved by remember {
        mutableStateOf(false)
    }


    val exoPlayer =
        remember {
            ExoPlayer.Builder(context)
                .build()
        }


    DisposableEffect(Unit) {

        onDispose {
            exoPlayer.release()
        }
    }


    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFFF5F3FF)
                )
                .padding(horizontal = 20.dp),

        contentPadding =
            PaddingValues(
                top = 16.dp,
                bottom = 24.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {


        // ------------------------------------------------------
        // HEADER
        // ------------------------------------------------------

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        "Lesson Package Ready",

                        fontSize = 20.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color(0xFF1E1B4B)
                    )

                    Text(
                        "$grade • $subject",

                        fontSize = 13.sp,

                        color =
                            Color(0xFF7C3AED)
                    )
                }


                IconButton(
                    onClick = onHome
                ) {

                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        }


        // ------------------------------------------------------
        // HINDI
        // ------------------------------------------------------

        item {

            LessonTextCard(
                title = "HINDI SOURCE TEXT",

                text =
                    response?.source_text ?: "",

                onCopy = {

                    response?.source_text?.let {

                        clipboard.setText(
                            AnnotatedString(it)
                        )

                        Toast.makeText(
                            context,
                            "Hindi text copied!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }


        // ------------------------------------------------------
        // TRANSLATION
        // ------------------------------------------------------

        item {

            Surface(
                shape =
                    RoundedCornerShape(20.dp),

                shadowElevation = 4.dp,

                color =
                    Color(0xFF4C1D95),

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        "TRANSLATED LESSON",

                        fontSize = 11.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color(0xFFDDD6FE)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    Text(
                        response?.translated_text
                            ?: "",

                        fontSize = 16.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color = Color.White,

                        lineHeight =
                            26.sp
                    )
                }
            }
        }


        // ------------------------------------------------------
        // AUDIO PLAYER
        // ------------------------------------------------------

        item {

            Surface(
                shape =
                    RoundedCornerShape(24.dp),

                color = Color.White,

                shadowElevation = 4.dp,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.GraphicEq,

                                contentDescription =
                                    null,

                                tint =
                                    Color(0xFF7C3AED)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Text(
                                "Santali Audio",

                                fontSize = 14.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }


                        Text(
                            "${playbackSpeed}x",

                            fontSize = 12.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color(0xFF7C3AED)
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )


                    AudioWaveformVisualizer(
                        isPlaying =
                            isPlaying
                    )


                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )


                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceEvenly,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {

                                playbackSpeed =
                                    when (
                                        playbackSpeed
                                    ) {

                                        0.8f -> 1f
                                        1f -> 1.25f
                                        1.25f -> 1.5f

                                        else -> 0.8f
                                    }

                                exoPlayer.setPlaybackSpeed(
                                    playbackSpeed
                                )
                            }
                        ) {

                            Text(
                                "${playbackSpeed}x",

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    Color(0xFF7C3AED)
                            )
                        }


                        Surface(
                            shape = CircleShape,

                            color =
                                Color(0xFF7C3AED),

                            shadowElevation =
                                6.dp,

                            modifier =
                                Modifier
                                    .size(64.dp)
                                    .clickable {

                                        val rawPath =
                                            response?.audio_file


                                        if (
                                            rawPath.isNullOrBlank()
                                        ) {

                                            Toast.makeText(
                                                context,
                                                "Audio is not available.",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            return@clickable
                                        }


                                        val audioUrl =
                                            if (
                                                rawPath.startsWith(
                                                    "http://"
                                                ) ||
                                                rawPath.startsWith(
                                                    "https://"
                                                )
                                            ) {

                                                rawPath

                                            } else {

                                                RetrofitClient
                                                    .getFullAudioUrl(
                                                        rawPath
                                                    )
                                            }


                                        if (
                                            audioUrl.isNullOrBlank()
                                        ) {

                                            Toast.makeText(
                                                context,
                                                "Audio URL unavailable.",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            return@clickable
                                        }


                                        if (isPlaying) {

                                            exoPlayer.pause()

                                            isPlaying = false

                                        } else {

                                            exoPlayer.setMediaItem(
                                                MediaItem.fromUri(
                                                    audioUrl
                                                )
                                            )

                                            exoPlayer.prepare()

                                            exoPlayer.setPlaybackSpeed(
                                                playbackSpeed
                                            )

                                            exoPlayer.play()

                                            isPlaying = true
                                        }
                                    }
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    if (isPlaying)
                                        Icons.Default.Pause
                                    else
                                        Icons.Default.PlayArrow,

                                    contentDescription =
                                        if (isPlaying)
                                            "Pause"
                                        else
                                            "Play",

                                    tint =
                                        Color.White,

                                    modifier =
                                        Modifier.size(36.dp)
                                )
                            }
                        }


                        IconButton(
                            onClick = {

                                val shareIntent =
                                    Intent(
                                        Intent.ACTION_SEND
                                    ).apply {

                                        type =
                                            "text/plain"

                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Vistaar Setu Lesson"
                                        )

                                        putExtra(
                                            Intent.EXTRA_TEXT,

                                            "Hindi: ${response?.source_text}\n\n" +
                                                    "Translated: ${response?.translated_text}"
                                        )
                                    }


                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Share Lesson"
                                    )
                                )
                            }
                        ) {

                            Icon(
                                Icons.Default.Share,

                                contentDescription =
                                    "Share",

                                tint =
                                    Color(0xFF6B7280)
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    Text(
                        if (
                            response?.audio_file
                                .isNullOrBlank()
                        )
                            "Audio unavailable"
                        else
                            "Santali audio ready",

                        fontSize = 11.sp,

                        color =
                            if (
                                response?.audio_file
                                    .isNullOrBlank()
                            )
                                Color(0xFFEF4444)
                            else
                                Color(0xFF10B981)
                    )
                }
            }
        }


        // ------------------------------------------------------
        // SAVE
        // ------------------------------------------------------

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = {

                        scope.launch {

                            val db =
                                AppDatabase.getDatabase(
                                    context
                                )


                            // IMPORTANT:
                            // Store a usable URL instead of
                            // backend's local file path.

                            val savedAudioUrl =
                                response?.audio_file?.let {

                                    if (
                                        it.startsWith(
                                            "http://"
                                        ) ||
                                        it.startsWith(
                                            "https://"
                                        )
                                    ) {

                                        it

                                    } else {

                                        RetrofitClient
                                            .getFullAudioUrl(it)
                                    }
                                }


                            db.lessonDao()
                                .insertLesson(

                                    SavedLesson(

                                        title =
                                            response
                                                ?.source_text
                                                ?.take(30)
                                                ?: "Santali Lesson",

                                        grade =
                                            grade,
                                        subject =
                                            "General",

                                        targetLanguage =
                                            response
                                                ?.target_language
                                                ?: "Santali",

                                        hindiText =
                                            response
                                                ?.source_text
                                                ?: "",

                                        translatedText =
                                            response
                                                ?.translated_text
                                                ?: "",

                                        remoteAudioUrl =
                                            savedAudioUrl
                                    )
                                )


                            isSaved = true


                            Toast.makeText(
                                context,
                                "Saved to Offline Library!",
                                Toast.LENGTH_SHORT
                            ).show()


                            onSave()
                        }
                    },

                    modifier =
                        Modifier
                            .weight(1f)
                            .height(50.dp),

                    shape =
                        RoundedCornerShape(25.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (isSaved)
                                    Color(0xFF10B981)
                                else
                                    Color(0xFF7C3AED)
                        )
                ) {

                    Icon(
                        if (isSaved)
                            Icons.Default.Check
                        else
                            Icons.Default.BookmarkAdd,

                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Text(
                        if (isSaved)
                            "Saved"
                        else
                            "Save Offline",

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                OutlinedButton(
                    onClick =
                        onHome,

                    modifier =
                        Modifier
                            .weight(1f)
                            .height(50.dp),

                    shape =
                        RoundedCornerShape(25.dp)
                ) {

                    Text(
                        "New Lesson",

                        color =
                            Color(0xFF7C3AED),

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ============================================================
// LESSON TEXT CARD
// ============================================================

@Composable
fun LessonTextCard(
    title: String,
    text: String,
    onCopy: () -> Unit
) {

    Surface(
        shape =
            RoundedCornerShape(20.dp),

        color = Color.White,

        shadowElevation = 2.dp,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    title,

                    fontSize = 11.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color(0xFF6B7280)
                )


                IconButton(
                    onClick = onCopy
                ) {

                    Icon(
                        Icons.Default.ContentCopy,

                        contentDescription =
                            "Copy",

                        tint =
                            Color(0xFF7C3AED),

                        modifier =
                            Modifier.size(18.dp)
                    )
                }
            }


            Text(
                text,

                fontSize = 15.sp,

                fontWeight =
                    FontWeight.Medium,

                color =
                    Color(0xFF1E1B4B)
            )
        }
    }
}


// ============================================================
// AUDIO WAVEFORM
// ============================================================

@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean
) {

    val transition =
        rememberInfiniteTransition(
            label = "waveform"
        )


    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(50.dp),

        horizontalArrangement =
            Arrangement.SpaceEvenly,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        repeat(16) { index ->

            val height by
            transition.animateFloat(
                initialValue =
                    0.25f,

                targetValue =
                    1f,

                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(
                                400 +
                                        index * 50
                            ),

                        repeatMode =
                            RepeatMode.Reverse
                    ),

                label =
                    "wave_$index"
            )


            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight(
                            if (isPlaying)
                                height
                            else
                                0.25f
                        )
                        .clip(CircleShape)
                        .background(
                            Color(0xFF7C3AED)
                        )
            )
        }
    }
}


// ============================================================
// 6. OFFLINE LIBRARY
// ============================================================

@Composable
fun SavedLessonsScreen(
    onBackHome: () -> Unit
) {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    var savedList by remember {
        mutableStateOf<List<SavedLesson>>(
            emptyList()
        )
    }

    var searchQuery by remember {
        mutableStateOf("")
    }


    fun refreshList() {

        scope.launch {

            val db =
                AppDatabase.getDatabase(
                    context
                )


            savedList =
                if (searchQuery.isBlank()) {

                    db.lessonDao()
                        .getAllLessons()

                } else {

                    db.lessonDao()
                        .searchLessons(
                            searchQuery
                        )
                }
        }
    }


    LaunchedEffect(searchQuery) {

        refreshList()
    }


    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(0xFFF5F3FF)
                )
                .padding(
                    horizontal = 20.dp
                )
    ) {

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column {

                Text(
                    "Offline Library",

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color(0xFF1E1B4B)
                )

                Text(
                    "Saved lessons for offline teaching",

                    fontSize = 12.sp,

                    color =
                        Color(0xFF6B7280)
                )
            }


            IconButton(
                onClick =
                    onBackHome
            ) {

                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,

                    contentDescription =
                        "Back"
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        // ------------------------------------------------------
        // SEARCH
        // ------------------------------------------------------

        OutlinedTextField(

            value =
                searchQuery,

            onValueChange = {
                searchQuery = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            placeholder = {
                Text(
                    "Search saved lessons..."
                )
            },

            leadingIcon = {

                Icon(
                    Icons.Default.Search,

                    contentDescription =
                        null,

                    tint =
                        Color(0xFF7C3AED)
                )
            },

            trailingIcon = {

                if (
                    searchQuery.isNotEmpty()
                ) {

                    IconButton(
                        onClick = {
                            searchQuery = ""
                        }
                    ) {

                        Icon(
                            Icons.Default.Clear,

                            contentDescription =
                                "Clear"
                        )
                    }
                }
            },

            shape =
                RoundedCornerShape(16.dp)
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        if (savedList.isEmpty()) {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

                contentAlignment =
                    Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Icon(
                        Icons.Default.FolderOpen,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(64.dp),

                        tint =
                            Color(0xFFC4B5FD)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Text(
                        "No saved lessons found.",

                        fontSize = 15.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            Color(0xFF6B7280)
                    )


                    Text(
                        "Create a lesson and tap Save Offline.",

                        fontSize = 12.sp,

                        color =
                            Color(0xFF9CA3AF)
                    )
                }
            }

        } else {

            LazyColumn(
                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp),

                contentPadding =
                    PaddingValues(
                        bottom = 16.dp
                    )
            ) {

                items(
                    savedList,
                    key = {
                        it.id
                    }
                ) { lesson ->

                    SavedLessonCard(
                        lesson = lesson,

                        onDelete = {

                            scope.launch {

                                val db =
                                    AppDatabase
                                        .getDatabase(
                                            context
                                        )

                                db.lessonDao()
                                    .deleteLesson(
                                        lesson
                                    )

                                refreshList()

                                Toast.makeText(
                                    context,
                                    "Lesson removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// SAVED LESSON CARD
// ============================================================

@Composable
fun SavedLessonCard(
    lesson: SavedLesson,
    onDelete: () -> Unit
) {

    val context =
        LocalContext.current

    var expanded by remember {
        mutableStateOf(false)
    }

    var isPlaying by remember {
        mutableStateOf(false)
    }


    val player =
        remember {
            ExoPlayer.Builder(
                context
            ).build()
        }


    DisposableEffect(Unit) {

        onDispose {

            player.release()
        }
    }


    Surface(
        shape =
            RoundedCornerShape(20.dp),

        color =
            Color.White,

        shadowElevation =
            3.dp,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            // --------------------------------------------------
            // HEADER
            // --------------------------------------------------

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        shape =
                            RoundedCornerShape(8.dp),

                        color =
                            Color(0xFFEDE9FE)
                    ) {

                        Text(
                            lesson.grade,

                            fontSize = 11.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color(0xFF7C3AED),

                            modifier =
                                Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Text(
                        lesson.targetLanguage,

                        fontSize = 11.sp,

                        color =
                            Color(0xFF6B7280),

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }


                IconButton(
                    onClick =
                        onDelete
                ) {

                    Icon(
                        Icons.Default.DeleteOutline,

                        contentDescription =
                            "Delete",

                        tint =
                            Color(0xFFEF4444)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(
                "Hindi",

                fontSize = 11.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF6B7280)
            )


            Text(
                lesson.hindiText,

                fontSize = 13.sp,

                color =
                    Color(0xFF374151),

                maxLines =
                    if (expanded)
                        10
                    else
                        2,

                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(
                "Translation",

                fontSize = 11.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF7C3AED)
            )


            Text(
                lesson.translatedText,

                fontSize = 14.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF1E1B4B),

                maxLines =
                    if (expanded)
                        10
                    else
                        3,

                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            // --------------------------------------------------
            // AUDIO
            // --------------------------------------------------

            if (
                !lesson.remoteAudioUrl.isNullOrBlank()
            ) {

                Surface(
                    shape =
                        RoundedCornerShape(14.dp),

                    color =
                        Color(0xFFF3E8FF),

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 8.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.Headphones,

                                contentDescription =
                                    null,

                                tint =
                                    Color(0xFF7C3AED)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Column {

                                Text(
                                    "Audio Lesson",

                                    fontSize = 12.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        Color(0xFF1E1B4B)
                                )

                                Text(
                                    "Santali pronunciation",

                                    fontSize = 10.sp,

                                    color =
                                        Color(0xFF6B7280)
                                )
                            }
                        }


                        IconButton(

                            onClick = {

                                val url =
                                    lesson.remoteAudioUrl


                                if (
                                    url.isNullOrBlank()
                                ) {

                                    Toast.makeText(
                                        context,
                                        "Audio unavailable",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    return@IconButton
                                }


                                if (isPlaying) {

                                    player.pause()

                                    isPlaying =
                                        false

                                } else {

                                    player.setMediaItem(
                                        MediaItem.fromUri(
                                            url.toString()
                                        )
                                    )

                                    player.prepare()

                                    player.play()

                                    isPlaying =
                                        true
                                }
                            }
                        ) {

                            Surface(
                                shape =
                                    CircleShape,

                                color =
                                    Color(0xFF7C3AED),

                                modifier =
                                    Modifier.size(42.dp)
                            ) {

                                Box(
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Icon(
                                        if (isPlaying)
                                            Icons.Default.Pause
                                        else
                                            Icons.Default.PlayArrow,

                                        contentDescription =
                                            if (isPlaying)
                                                "Pause Audio"
                                            else
                                                "Play Audio",

                                        tint =
                                            Color.White
                                    )
                                }
                            }
                        }
                    }
                }

            } else {

                Text(
                    "Audio not available for this lesson",

                    fontSize = 11.sp,

                    color =
                        Color(0xFFEF4444)
                )
            }


            // --------------------------------------------------
            // EXPAND
            // --------------------------------------------------

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.End
            ) {

                TextButton(
                    onClick = {
                        expanded = !expanded
                    }
                ) {

                    Text(
                        if (expanded)
                            "Show Less"
                        else
                            "Read Full Lesson",

                        fontSize = 12.sp,

                        color =
                            Color(0xFF7C3AED),

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ============================================================
// 7. NOTIFICATIONS
// ============================================================

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

    val notifications =
        remember {

            listOf(

                NotificationItem(
                    id = "1",

                    title =
                        "New Lesson Added",

                    message =
                        "Math Grade 3 is ready with Santali translation and audio.",

                    badge =
                        "NEW LESSON",

                    time =
                        "Recently",

                    isNewLesson =
                        true
                ),

                NotificationItem(
                    id = "2",

                    title =
                        "Offline Library",

                    message =
                        "Your saved lessons are available from the Offline Library.",

                    badge =
                        "OFFLINE",

                    time =
                        "Today"
                ),

                NotificationItem(
                    id = "3",

                    title =
                        "Audio Ready",

                    message =
                        "Generated Santali pronunciation is ready for playback.",

                    badge =
                        "AUDIO",

                    time =
                        "Today"
                )
            )
        }


    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),

        contentPadding =
            PaddingValues(
                top = 16.dp,
                bottom = 24.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,

                        contentDescription =
                            "Back"
                    )
                }


                Column {

                    Text(
                        "Notifications",

                        fontSize = 20.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color(0xFF1E1B4B)
                    )

                    Text(
                        "Lessons & classroom updates",

                        fontSize = 12.sp,

                        color =
                            Color(0xFF6B7280)
                    )
                }
            }
        }


        items(
            notifications
        ) { item ->

            Surface(
                shape =
                    RoundedCornerShape(20.dp),

                color =
                    Color.White,

                shadowElevation =
                    3.dp,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp)
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Surface(
                            shape =
                                RoundedCornerShape(10.dp),

                            color =
                                Color(0xFFF3E8FF)
                        ) {

                            Text(
                                item.badge,

                                fontSize = 10.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    Color(0xFF7C3AED),

                                modifier =
                                    Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                            )
                        }


                        Text(
                            item.time,

                            fontSize = 11.sp,

                            color =
                                Color(0xFF9CA3AF)
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )


                    Text(
                        item.title,

                        fontSize = 15.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color(0xFF1E1B4B)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )


                    Text(
                        item.message,

                        fontSize = 13.sp,

                        color =
                            Color(0xFF4B5563),

                        lineHeight =
                            18.sp
                    )


                    if (
                        item.isNewLesson
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )


                        Button(
                            onClick =
                                onLearnNewLesson,

                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(12.dp),

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFF7C3AED)
                                )
                        ) {

                            Icon(
                                Icons.Default.PlayArrow,

                                contentDescription =
                                    null
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )

                            Text(
                                "Create New Lesson",

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}