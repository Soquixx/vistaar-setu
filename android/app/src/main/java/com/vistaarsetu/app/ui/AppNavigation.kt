package com.vistaarsetu.app.ui

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.vistaarsetu.app.data.AppDatabase
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.vistaarsetu.app.data.*
import kotlinx.coroutines.launch

enum class Screen { HOME, NEW_LESSON, PROCESSING, RESULT, SAVED_LESSONS }

@Composable
fun MainAppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var inputGrade by remember { mutableStateOf("Grade 1") }
    var inputArea by remember { mutableStateOf("Foundational Literacy") }
    var inputLang by remember { mutableStateOf("Santali") }
    var inputHindiText by remember { mutableStateOf("") }
    var lastResponse by remember { mutableStateOf<ProcessLessonResponse?>(null) }

    when (currentScreen) {
        Screen.HOME -> HomeScreen(
            onNewLesson = { currentScreen = Screen.NEW_LESSON },
            onViewSaved = { currentScreen = Screen.SAVED_LESSONS }
        )
        Screen.NEW_LESSON -> NewLessonScreen(
            grade = inputGrade, onGradeChange = { inputGrade = it },
            area = inputArea, onAreaChange = { inputArea = it },
            lang = inputLang, onLangChange = { inputLang = it },
            text = inputHindiText, onTextChange = { inputHindiText = it },
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
            response = lastResponse,
            onSave = { currentScreen = Screen.SAVED_LESSONS },
            onHome = { currentScreen = Screen.HOME }
        )
        Screen.SAVED_LESSONS -> SavedLessonsScreen(
            onBackHome = { currentScreen = Screen.HOME }
        )
    }
}

// 1. HOME SCREEN
@Composable
fun HomeScreen(onNewLesson: () -> Unit, onViewSaved: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Vistaar Setu", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNewLesson, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("[ New Lesson ]")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onViewSaved, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Saved Lessons")
        }
    }
}

// VOICE INPUT TEXT FIELD HELPER
@Composable
fun SpeechToTextField(
    textValue: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    val context = LocalContext.current

    // Speech Recognition Result Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.getOrNull(0) ?: ""
            if (spokenText.isNotEmpty()) {
                val updatedText = if (textValue.isBlank()) spokenText else "$textValue $spokenText"
                onValueChange(updatedText)
            }
        }
    }

    // Permission Launcher for Record Audio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "अब हिंदी में बोलें...")
            }
            speechLauncher.launch(intent)
        }
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("Speak or type Hindi content...") },
        modifier = Modifier.fillMaxWidth().height(140.dp),
        trailingIcon = {
            IconButton(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "अब हिंदी में बोलें...")
                        }
                        speechLauncher.launch(intent)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            ) {
                Text("🎙️", style = MaterialTheme.typography.titleMedium)
            }
        }
    )
}

// 2. NEW LESSON SCREEN
@Composable
fun NewLessonScreen(
    grade: String, onGradeChange: (String) -> Unit,
    area: String, onAreaChange: (String) -> Unit,
    lang: String, onLangChange: (String) -> Unit,
    text: String, onTextChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("New Lesson", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Grade")
        OutlinedTextField(value = grade, onValueChange = onGradeChange, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))

        Text("Learning Area")
        OutlinedTextField(value = area, onValueChange = onAreaChange, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))

        Text("Target Language")
        OutlinedTextField(value = lang, onValueChange = onLangChange, modifier = Modifier.fillMaxWidth(), enabled = false)
        Text("Prototype: Santali (Ho/Mundari available soon)", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Hindi Lesson (Tap 🎙️ to Speak)")
        SpeechToTextField(
            textValue = text,
            onValueChange = onTextChange,
            label = "Lesson Content"
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = text.isNotBlank()
        ) {
            Text("[ Generate Lesson ]")
        }
    }
}

// 3. PROCESSING SCREEN
@Composable
fun ProcessingScreen(
    grade: String, area: String, lang: String, text: String,
    onSuccess: (ProcessLessonResponse) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val req = ProcessLessonRequest(
                    grade = grade.filter { it.isDigit() }.toIntOrNull() ?: 1,
                    learning_area = area,
                    target_language = "sat",
                    text = text
                )
                val res = RetrofitClient.apiService.processLesson(req)
                onSuccess(res)
            } catch (e: Exception) {
                // Fallback mock payload to continue testing UI if backend is offline
                onSuccess(
                    ProcessLessonResponse(
                        source_text = text,
                        target_language = "Santali",
                        translated_text = "गिनो: एक, दो, तीन, चार, पाँच। (Santali Draft)",
                        audio_file = null,
                        status = "draft"
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(24.dp))
        Text("Preparing your lesson...", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text("• Processing voice/text input")
        Text("• Adapting to Santali")
        Text("• Preparing classroom material")
    }
}

// 4. RESULT SCREEN
@Composable
fun ResultScreen(
    grade: String,
    response: ProcessLessonResponse?,
    onSave: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Lesson Result", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Hindi", style = MaterialTheme.typography.titleMedium)
        Text(response?.source_text ?: "")
        Spacer(modifier = Modifier.height(16.dp))

        Text("Santali", style = MaterialTheme.typography.titleMedium)
        Text(response?.translated_text ?: "")
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                response?.audio_file?.let { url ->
                    val mediaItem = MediaItem.fromUri(url)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔊 Play")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    val db = AppDatabase.getDatabase(context)
                    db.lessonDao().insertLesson(
                        SavedLesson(
                            title = response?.source_text?.take(20) ?: "Lesson",
                            grade = grade,
                            targetLanguage = "Santali",
                            hindiText = response?.source_text ?: "",
                            translatedText = response?.translated_text ?: "",
                            audioUrl = response?.audio_file
                        )
                    )
                    onSave()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("[ Save Offline ]")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Home")
        }
    }
}

// 5. SAVED LESSONS SCREEN
@Composable
fun SavedLessonsScreen(onBackHome: () -> Unit) {
    val context = LocalContext.current
    var savedList by remember { mutableStateOf<List<SavedLesson>>(emptyList()) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getDatabase(context)
        savedList = db.lessonDao().getAllLessons()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("My Offline Lessons", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (savedList.isEmpty()) {
            Text("No saved offline lessons yet.")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(savedList) { lesson ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                            Text("${lesson.grade} • ${lesson.targetLanguage}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Santali: ${lesson.translatedText}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
            Text("Back Home")
        }
    }
}