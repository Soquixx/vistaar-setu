package com.vistaarsetu.app.ui.worksheets

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vistaarsetu.app.data.LessonEntity

@Composable
fun WorksheetScreen(
    lesson: LessonEntity,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedOptionQ1 by remember { mutableStateOf<Int?>(null) }
    var selectedTrueFalseQ2 by remember { mutableStateOf<Boolean?>(null) }
    var submitted by remember { mutableStateOf(false) }

    val isQ1Correct = selectedOptionQ1 == 0
    val isQ2Correct = selectedTrueFalseQ2 == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
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
                            "Bilingual Worksheet",
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
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEDE9FE)
                ) {
                    Text(
                        "FLN Practice",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // BILINGUAL INSTRUCTIONS
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "INSTRUCTIONS / ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "हिंदी: दिए गए पाठ को ध्यानपूर्वक पढ़ें और प्रश्नों के उत्तर दें।",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "ᱥᱟᱱᱛᱟᱲᱤ: ᱚᱞ ᱟᱠᱟᱱ ᱯᱟᱲᱦᱟᱣ ᱢᱮ ᱟᱨ ᱠᱩᱠᱞᱤ ᱨᱮᱱᱟᱜ ᱥᱟᱹᱨᱤ ᱛᱮᱞᱟ ᱮᱢ ᱢᱮ᱾",
                        fontSize = 13.sp,
                        color = Color(0xFF4C1D95),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // READING COMPREHENSION PASSAGE
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEDE9FE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "READING PASSAGE (पाठ पढ़ें)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5B21B6)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Hindi: ${lesson.hindiText}",
                        fontSize = 14.sp,
                        color = Color(0xFF1E1B4B),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Santali: ${lesson.translatedText}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C1D95),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // QUESTION 1: MULTIPLE CHOICE
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
                            "Question 1 (कुकᱞᱤ 1)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                        if (submitted) {
                            Text(
                                if (isQ1Correct) "✓ Correct (+1)" else "✗ Incorrect (0)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isQ1Correct) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "दिए गए पाठ का सही संताली (Ol Chiki) अनुवाद कौन सा है?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val options = listOf(
                        lesson.translatedText,
                        "ᱢᱤᱫᱴᱟᱝ ᱮᱴᱟᱜ ᱠᱟᱛᱷᱟ (Alternative Option A)",
                        "ᱡᱚᱛᱚ ᱦᱚᱲ ᱢᱮᱥᱟ ᱠᱟᱛᱮ (Alternative Option B)"
                    )

                    options.forEachIndexed { index, optionText ->
                        val isSelected = selectedOptionQ1 == index
                        val borderColor = when {
                            submitted && index == 0 -> Color(0xFF059669)
                            submitted && isSelected && !isQ1Correct -> Color(0xFFDC2626)
                            isSelected -> Color(0xFF7C3AED)
                            else -> Color(0xFFE5E7EB)
                        }
                        val bgColor = when {
                            submitted && index == 0 -> Color(0xFFD1FAE5)
                            submitted && isSelected && !isQ1Correct -> Color(0xFFFEE2E2)
                            isSelected -> Color(0xFFF3E8FF)
                            else -> Color(0xFFF9FAFB)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!submitted) {
                                        selectedOptionQ1 = index
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { if (!submitted) selectedOptionQ1 = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C3AED))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    optionText,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color(0xFF1E1B4B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // QUESTION 2: TRUE OR FALSE (सत्य / असत्य)
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
                            "Question 2 (कुकᱞᱤ 2)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                        if (submitted) {
                            Text(
                                if (isQ2Correct) "✓ Correct (+1)" else "✗ Incorrect (0)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isQ2Correct) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "क्या संताली भाषा 'ओल चिकी' (Ol Chiki ᱚᱞ ᱪᱤᱠᱤ) लिपि में लिखी जाती है?\n(Is Santali written in Ol Chiki script?)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // TRUE OPTION
                        OutlinedButton(
                            onClick = { if (!submitted) selectedTrueFalseQ2 = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTrueFalseQ2 == true) Color(0xFFEDE9FE) else Color.Transparent
                            )
                        ) {
                            Text("हाँ / ᱥᱟᱹᱨᱤ (True)", fontWeight = FontWeight.Bold)
                        }

                        // FALSE OPTION
                        OutlinedButton(
                            onClick = { if (!submitted) selectedTrueFalseQ2 = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTrueFalseQ2 == false) Color(0xFFFEE2E2) else Color.Transparent
                            )
                        ) {
                            Text("नहीं / ᱵᱟᱝ (False)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SUBMIT & SCORE ACTION
        item {
            if (submitted) {
                val totalScore = (if (isQ1Correct) 1 else 0) + (if (isQ2Correct) 1 else 0)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (totalScore == 2) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (totalScore == 2) Color(0xFF059669) else Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    if (totalScore == 2) "बहुत बढ़िया! (Excellent!)" else "पुनः प्रयास करें (Try Again)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    "Score: $totalScore / 2",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                submitted = false
                                selectedOptionQ1 = null
                                selectedTrueFalseQ2 = null
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (selectedOptionQ1 == null || selectedTrueFalseQ2 == null) {
                            Toast.makeText(context, "Please answer all questions", Toast.LENGTH_SHORT).show()
                        } else {
                            submitted = true
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Check Answers / ᱛᱮᱞᱟ ᱧᱮᱞ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
