package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.CalculatorViewModel

enum class NormalButtonStyle {
    Numeric, Operator, ActionClear, ActionDelete, ActionEquals, Memory
}

data class NormalCalcButton(
    val label: String,
    val style: NormalButtonStyle,
    val action: String
)

@Composable
fun NormalCalcScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val calcResult by viewModel.calcResult.collectAsState()
    val memoryValue by viewModel.memoryValue.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- DISPLAY CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Mode Info & Memory Status Area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "NORMAL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (memoryValue != 0.0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "M = ${if (memoryValue % 1.0 == 0.0) memoryValue.toLong().toString() else memoryValue.toString()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "MEM REGISTER",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Values Area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(expression) {
                        scrollState.scrollTo(scrollState.maxValue)
                    }

                    // Raw current formula expression input
                    Text(
                        text = expression.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = if (expression.length > 20) 24.sp else 32.sp
                        ),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .testTag("calc_expression_display")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Evaluation real-time result preview
                    AnimatedVisibility(visible = calcResult.isNotEmpty()) {
                        Text(
                            text = if (calcResult == "Error") "Error" else "= $calcResult",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (calcResult == "Error") {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            ),
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calc_result_display")
                        )
                    }
                }
            }
        }

        // --- MEMORY CONTROLS ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val memoryButtons = listOf(
                NormalCalcButton("MC", NormalButtonStyle.Memory, "MC"),
                NormalCalcButton("MR", NormalButtonStyle.Memory, "MR"),
                NormalCalcButton("M+", NormalButtonStyle.Memory, "M+"),
                NormalCalcButton("M-", NormalButtonStyle.Memory, "M-")
            )

            memoryButtons.forEach { btn ->
                Card(
                    onClick = {
                        when (btn.action) {
                            "MC" -> viewModel.memoryClear()
                            "MR" -> viewModel.memoryRecall()
                            "M+" -> viewModel.memoryAdd()
                            "M-" -> viewModel.memorySubtract()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_mem_${btn.label.lowercase()}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = btn.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }

        // --- CALC BUTTONS GRID ---
        val buttonsList = listOf(
            NormalCalcButton("C", NormalButtonStyle.ActionClear, "CLEAR"),
            NormalCalcButton("⌫", NormalButtonStyle.ActionDelete, "DEL"),
            NormalCalcButton("%", NormalButtonStyle.Operator, "%"),
            NormalCalcButton("/", NormalButtonStyle.Operator, "/"),

            NormalCalcButton("7", NormalButtonStyle.Numeric, "7"),
            NormalCalcButton("8", NormalButtonStyle.Numeric, "8"),
            NormalCalcButton("9", NormalButtonStyle.Numeric, "9"),
            NormalCalcButton("*", NormalButtonStyle.Operator, "*"),

            NormalCalcButton("4", NormalButtonStyle.Numeric, "4"),
            NormalCalcButton("5", NormalButtonStyle.Numeric, "5"),
            NormalCalcButton("6", NormalButtonStyle.Numeric, "6"),
            NormalCalcButton("-", NormalButtonStyle.Operator, "-"),

            NormalCalcButton("1", NormalButtonStyle.Numeric, "1"),
            NormalCalcButton("2", NormalButtonStyle.Numeric, "2"),
            NormalCalcButton("3", NormalButtonStyle.Numeric, "3"),
            NormalCalcButton("+", NormalButtonStyle.Operator, "+"),

            NormalCalcButton("0", NormalButtonStyle.Numeric, "0"),
            NormalCalcButton(".", NormalButtonStyle.Numeric, "."),
            NormalCalcButton("ans", NormalButtonStyle.Numeric, if (calcResult.isEmpty() || calcResult == "Error") "0" else calcResult),
            NormalCalcButton("=", NormalButtonStyle.ActionEquals, "EVAL")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(buttonsList) { btn ->
                NormalCalcKey(
                    button = btn,
                    onClick = {
                        when (btn.action) {
                            "CLEAR" -> viewModel.clearExpression()
                            "DEL" -> viewModel.deleteLastChar()
                            "EVAL" -> viewModel.evaluateExpression()
                            else -> viewModel.appendToExpression(btn.action)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NormalCalcKey(
    button: NormalCalcButton,
    onClick: () -> Unit
) {
    val containerColor = when (button.style) {
        NormalButtonStyle.ActionClear -> MaterialTheme.colorScheme.errorContainer
        NormalButtonStyle.ActionDelete -> MaterialTheme.colorScheme.surfaceVariant
        NormalButtonStyle.Operator -> MaterialTheme.colorScheme.primaryContainer
        NormalButtonStyle.ActionEquals -> MaterialTheme.colorScheme.primary
        NormalButtonStyle.Numeric -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (button.style) {
        NormalButtonStyle.ActionClear -> MaterialTheme.colorScheme.onErrorContainer
        NormalButtonStyle.Operator -> MaterialTheme.colorScheme.onPrimaryContainer
        NormalButtonStyle.ActionEquals -> MaterialTheme.colorScheme.onPrimary
        NormalButtonStyle.Numeric -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1.25f)
            .fillMaxWidth()
            .testTag("btn_normal_${button.label}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (button.label == "⌫") {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = "Backspace",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = button.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (button.label.length > 2) 16.sp else 20.sp,
                    fontFamily = if (button.style == NormalButtonStyle.Numeric) FontFamily.SansSerif else FontFamily.SansSerif
                )
            }
        }
    }
}
