package com.vistaarsetu.app.ui.voice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vistaarsetu.app.data.models.SessionTurn
import java.util.UUID

enum class VoiceMode {
    SINGLE_LESSON,
    CLASSROOM_SESSION
}

@Composable
fun VoiceInputScreen(
    grade: String,
    onGradeChange: (String) -> Unit,
    area: String,
    onAreaChange: (String) -> Unit,
    lang: String,
    onLangChange: (String) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(VoiceMode.SINGLE_LESSON) }
    var isListening by remember { mutableStateOf(false) }

    // Multi-turn live session foundation
    var isSessionActive by remember { mutableStateOf(false) }
    var sessionTurns by remember { mutableStateOf<List<SessionTurn>>(emptyList()) }

    // Dropdown states
    var showGradeMenu by remember { mutableStateOf(false) }
    var showAreaMenu by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.getOrNull(0) ?: ""

            if (spokenText.isNotEmpty()) {
                if (selectedMode == VoiceMode.SINGLE_LESSON) {
                    val updated = if (text.isBlank()) spokenText else "$text\n$spokenText"
                    onTextChange(updated)
                    Toast.makeText(context, "Voice input captured!", Toast.LENGTH_SHORT).show()
                } else {
                    // Turn added to live classroom session
                    val newTurn = SessionTurn(
                        turnNumber = sessionTurns.size + 1,
                        speakerHindi = spokenText,
                        translationSantali = "Processing turn translation...",
                        audioPath = null
                    )
                    sessionTurns = sessionTurns + newTurn
                    Toast.makeText(context, "Turn #${newTurn.turnNumber} captured", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "शिक्षक: हिंदी में बोलें...")
            }
            speechLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Microphone permission is required for voice input", Toast.LENGTH_LONG).show()
        }
    }

    fun startListening() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            isListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "शिक्षक: हिंदी में बोलें...")
            }
            speechLauncher.launch(intent)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP BAR
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Voice & Lesson Input",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B)
                    )
                    Text(
                        "Vernacular Speech-to-Text Input",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEDE9FE)
                ) {
                    Text(
                        "hi-IN Recognizer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // MODE SELECTOR TAB (Single Lesson vs Live Classroom Session)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedMode == VoiceMode.SINGLE_LESSON) Color(0xFF7C3AED) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = VoiceMode.SINGLE_LESSON }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                "Single Lesson",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMode == VoiceMode.SINGLE_LESSON) Color.White else Color(0xFF6B7280)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedMode == VoiceMode.CLASSROOM_SESSION) Color(0xFF7C3AED) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = VoiceMode.CLASSROOM_SESSION }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                "Live Classroom Session",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMode == VoiceMode.CLASSROOM_SESSION) Color.White else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }

        // METADATA CONFIGURATION CHIPS (Grade, Subject, Target Language)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // GRADE DROPDOWN
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGradeMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("GRADE", fontSize = 9.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                                Text(grade, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF7C3AED))
                        }
                    }
                    DropdownMenu(expanded = showGradeMenu, onDismissRequest = { showGradeMenu = false }) {
                        listOf("Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5").forEach { g ->
                            DropdownMenuItem(text = { Text(g) }, onClick = {
                                onGradeChange(g)
                                showGradeMenu = false
                            })
                        }
                    }
                }

                // SUBJECT DROPDOWN
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAreaMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("SUBJECT", fontSize = 9.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                                Text(area, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF7C3AED))
                        }
                    }
                    DropdownMenu(expanded = showAreaMenu, onDismissRequest = { showAreaMenu = false }) {
                        listOf("Mathematics", "EVS", "Language", "General").forEach { s ->
                            DropdownMenuItem(text = { Text(s) }, onClick = {
                                onAreaChange(s)
                                showAreaMenu = false
                            })
                        }
                    }
                }

                // LANGUAGE DROPDOWN
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLangMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TARGET", fontSize = 9.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                                Text(lang, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF7C3AED))
                        }
                    }
                    DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
                        listOf("Santali", "Ho", "Mundari").forEach { l ->
                            DropdownMenuItem(text = { Text(l) }, onClick = {
                                onLangChange(l)
                                showLangMenu = false
                            })
                        }
                    }
                }
            }
        }

        if (selectedMode == VoiceMode.SINGLE_LESSON) {
            // VOICE RECORDING HERO BUTTON
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Tap to Speak in Hindi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            "Voice is captured using on-device Hindi speech recognition",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = CircleShape,
                            color = if (isListening) Color(0xFFEF4444) else Color(0xFF7C3AED),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(72.dp)
                                .clickable { startListening() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.Hearing else Icons.Default.Mic,
                                    contentDescription = "Microphone",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            if (isListening) "Listening in Hindi..." else "Tap mic to speak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isListening) Color(0xFFEF4444) else Color(0xFF7C3AED)
                        )
                    }
                }
            }

            // TEXT INPUT CARD
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
                                "HINDI LESSON INPUT (OR SPOKEN TEXT)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280)
                            )
                            if (text.isNotBlank()) {
                                TextButton(onClick = { onTextChange("") }) {
                                    Text("Clear", fontSize = 11.sp, color = Color(0xFFEF4444))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp),
                            shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("हिंदी में पाठ लिखें या बोलें...") }
                        )
                    }
                }
            }

            // GENERATE BUTTON
            item {
                Button(
                    onClick = onGenerate,
                    enabled = text.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Generate Lesson & Audio (Cache-First)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // CLASSROOM LIVE SESSION MODE (Multi-turn foundation)
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Classroom Interactive Session",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    if (isSessionActive) "Session in progress (${sessionTurns.size} turns)" else "Session idle",
                                    fontSize = 12.sp,
                                    color = if (isSessionActive) Color(0xFF059669) else Color(0xFF6B7280)
                                )
                            }

                            Button(
                                onClick = {
                                    isSessionActive = !isSessionActive
                                    if (!isSessionActive) {
                                        Toast.makeText(context, "Session completed with ${sessionTurns.size} turns", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSessionActive) Color(0xFFEF4444) else Color(0xFF7C3AED)
                                )
                            ) {
                                Text(if (isSessionActive) "End Session" else "Start Session")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isSessionActive) {
                            Button(
                                onClick = { startListening() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Speak Next Teacher Turn (Hindi)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // TURNS LIST
            item {
                Text(
                    "CONVERSATION TURNS (${sessionTurns.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED)
                )
            }

            if (sessionTurns.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(24.dp)) {
                            Text(
                                "No dialogue turns yet. Start the session and speak to record.",
                                fontSize = 13.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            } else {
                items(sessionTurns) { turn ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEDE9FE)) {
                                    Text(
                                        "Turn #${turn.turnNumber}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7C3AED),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Teacher (Hindi): ${turn.speakerHindi}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }
                }
            }
        }
    }
}
