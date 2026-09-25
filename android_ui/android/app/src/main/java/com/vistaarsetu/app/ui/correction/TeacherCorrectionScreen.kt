package com.vistaarsetu.app.ui.correction

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vistaarsetu.app.data.AppDatabase
import com.vistaarsetu.app.data.LessonEntity
import kotlinx.coroutines.launch

@Composable
fun TeacherCorrectionScreen(
    lesson: LessonEntity,
    onBack: () -> Unit,
    onCorrectionSaved: (LessonEntity) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var correctedText by remember { mutableStateOf(lesson.translatedText) }
    var teacherNotes by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("Vocabulary / Dialect") }
    var isSaving by remember { mutableStateOf(false) }

    val reasons = listOf(
        "Vocabulary / Dialect",
        "Spelling / Script (Ol Chiki)",
        "Grammar / Syntax",
        "Grade Appropriateness"
    )

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
                            "Teacher Review Loop",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            "Vernacular Pedagogy Human-in-the-Loop",
                            fontSize = 11.sp,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        "Local Review",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // INFORMATIONAL BANNER
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEDE9FE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Teacher corrections are saved locally on this device. The original lesson is preserved with an updated 'Teacher Corrected' status for classroom reliability.",
                        fontSize = 12.sp,
                        color = Color(0xFF5B21B6),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // HINDI SOURCE TEXT (READ-ONLY)
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ORIGINAL HINDI SOURCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        lesson.hindiText,
                        fontSize = 14.sp,
                        color = Color(0xFF1F2937),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // REASON SELECTOR
        item {
            Column {
                Text(
                    "CORRECTION REASON",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reasons) { reason ->
                        val isSelected = selectedReason == reason
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF7C3AED) else Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier.clickable { selectedReason = reason }
                        ) {
                            Text(
                                reason,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF374151),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // EDITABLE SANTALI TRANSLATION
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "EDIT SANTALI TRANSLATION (OL CHIKI)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = correctedText,
                        onValueChange = { correctedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("Enter corrected Ol Chiki translation...") }
                    )
                }
            }
        }

        // OPTIONAL TEACHER NOTES
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "TEACHER PEDAGOGICAL NOTES (OPTIONAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = teacherNotes,
                        onValueChange = { teacherNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("e.g., Regional dialect adaptation for local district") }
                    )
                }
            }
        }

        // SAVE BUTTON
        item {
            Button(
                onClick = {
                    if (correctedText.isBlank()) {
                        Toast.makeText(context, "Translation cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    scope.launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val updatedLesson = lesson.copy(
                                translatedText = correctedText.trim(),
                                verificationStatus = "teacher_corrected",
                                timestamp = System.currentTimeMillis()
                            )
                            db.lessonDao().updateLesson(updatedLesson)

                            Toast.makeText(context, "Correction saved to local lesson bank!", Toast.LENGTH_LONG).show()
                            onCorrectionSaved(updatedLesson)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isSaving) "Saving..." else "Save Correction Locally",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
