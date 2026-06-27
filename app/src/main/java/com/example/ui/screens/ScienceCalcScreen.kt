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

enum class ButtonStyleType {
    Numeric, Operator, ActionClear, ActionDelete, ActionEquals, Scientific
}

data class CalcButtonInput(
    val label: String,
    val style: ButtonStyleType,
    val action: String
)

@Composable
fun ScienceCalcScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val calcResult by viewModel.calcResult.collectAsState()
    val isRadMode by viewModel.isRadMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- DISPLAY REGION ---
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
                // Header angle mode control row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = { viewModel.toggleAngleMode() },
                        label = {
                            Text(
                                text = if (isRadMode) "RAD MODE" else "DEG MODE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.testTag("angle_mode_chip")
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "SCIENTIFIC",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Expression & Result previews
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(expression) {
                        scrollState.scrollTo(scrollState.maxValue)
                    }

                    // Formula input text rendering
                    Text(
                        text = expression.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = if (expression.length > 20) 22.sp else 30.sp
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

                    // Calculation Result rendering
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

        // --- SCIENTIFIC OPERATORS PANEL ---
        val scientificButtons = listOf(
            CalcButtonInput("sin", ButtonStyleType.Scientific, "sin("),
            CalcButtonInput("cos", ButtonStyleType.Scientific, "cos("),
            CalcButtonInput("tan", ButtonStyleType.Scientific, "tan("),
            CalcButtonInput("sin⁻¹", ButtonStyleType.Scientific, "asin("),
            CalcButtonInput("cos⁻¹", ButtonStyleType.Scientific, "acos("),

            CalcButtonInput("tan⁻¹", ButtonStyleType.Scientific, "atan("),
            CalcButtonInput("(", ButtonStyleType.Scientific, "("),
            CalcButtonInput(")", ButtonStyleType.Scientific, ")"),
            CalcButtonInput("^", ButtonStyleType.Scientific, "^"),
            CalcButtonInput("√", ButtonStyleType.Scientific, "sqrt("),

            CalcButtonInput("π", ButtonStyleType.Scientific, "pi"),
            CalcButtonInput("e", ButtonStyleType.Scientific, "e"),
            CalcButtonInput("log", ButtonStyleType.Scientific, "log("),
            CalcButtonInput("ln", ButtonStyleType.Scientific, "ln("),
            CalcButtonInput("abs", ButtonStyleType.Scientific, "abs(")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(scientificButtons) { button ->
                KeypadButton(
                    input = button,
                    onClick = { viewModel.appendToExpression(button.action) }
                )
            }
        }

        // --- BASIC OPERATORS PANEL ---
        val basicButtons = listOf(
            CalcButtonInput("C", ButtonStyleType.ActionClear, "CLEAR"),
            CalcButtonInput("⌫", ButtonStyleType.ActionDelete, "DEL"),
            CalcButtonInput("%", ButtonStyleType.Operator, "%"),
            CalcButtonInput("/", ButtonStyleType.Operator, "/"),

            CalcButtonInput("7", ButtonStyleType.Numeric, "7"),
            CalcButtonInput("8", ButtonStyleType.Numeric, "8"),
            CalcButtonInput("9", ButtonStyleType.Numeric, "9"),
            CalcButtonInput("*", ButtonStyleType.Operator, "*"),

            CalcButtonInput("4", ButtonStyleType.Numeric, "4"),
            CalcButtonInput("5", ButtonStyleType.Numeric, "5"),
            CalcButtonInput("6", ButtonStyleType.Numeric, "6"),
            CalcButtonInput("-", ButtonStyleType.Operator, "-"),

            CalcButtonInput("1", ButtonStyleType.Numeric, "1"),
            CalcButtonInput("2", ButtonStyleType.Numeric, "2"),
            CalcButtonInput("3", ButtonStyleType.Numeric, "3"),
            CalcButtonInput("+", ButtonStyleType.Operator, "+"),

            CalcButtonInput("0", ButtonStyleType.Numeric, "0"),
            CalcButtonInput(".", ButtonStyleType.Numeric, "."),
            CalcButtonInput("ans", ButtonStyleType.Scientific, if (calcResult.isEmpty() || calcResult == "Error") "0" else calcResult),
            CalcButtonInput("=", ButtonStyleType.ActionEquals, "EVAL")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(basicButtons) { button ->
                KeypadButton(
                    input = button,
                    onClick = {
                        when (button.action) {
                            "CLEAR" -> viewModel.clearExpression()
                            "DEL" -> viewModel.deleteLastChar()
                            "EVAL" -> viewModel.evaluateExpression()
                            else -> viewModel.appendToExpression(button.action)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun KeypadButton(
    input: CalcButtonInput,
    onClick: () -> Unit
) {
    val containerColor = when (input.style) {
        ButtonStyleType.ActionClear -> MaterialTheme.colorScheme.errorContainer
        ButtonStyleType.ActionDelete -> MaterialTheme.colorScheme.surfaceVariant
        ButtonStyleType.Operator -> MaterialTheme.colorScheme.primaryContainer
        ButtonStyleType.ActionEquals -> MaterialTheme.colorScheme.primary
        ButtonStyleType.Scientific -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ButtonStyleType.Numeric -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val contentColor = when (input.style) {
        ButtonStyleType.ActionClear -> MaterialTheme.colorScheme.onErrorContainer
        ButtonStyleType.Operator -> MaterialTheme.colorScheme.onPrimaryContainer
        ButtonStyleType.ActionEquals -> MaterialTheme.colorScheme.onPrimary
        ButtonStyleType.Scientific -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonStyleType.Numeric -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(if (input.style == ButtonStyleType.Scientific) 1.5f else 1.25f)
            .fillMaxWidth()
            .testTag("btn_${input.label}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (input.label == "⌫") {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = "Backspace",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = input.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (input.label.length >= 4) 12.sp else if (input.label.length >= 3) 14.sp else 18.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
