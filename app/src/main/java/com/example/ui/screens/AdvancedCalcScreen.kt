package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OcrState
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.animation.core.*
import com.example.viewmodel.CalculatorViewModel
import kotlin.math.sqrt

enum class AdvancedToolType {
    EQUATIONS, MATRIX, BASE_CONVERTER, UNIT_CONVERTER, AI_SOLVER
}

@Composable
fun AdvancedCalcScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTool by rememberSaveable { mutableStateOf(AdvancedToolType.EQUATIONS) }
    val verticalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(verticalScroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER MODE CHIPS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdvancedToolType.values().forEach { tool ->
                val isSelected = selectedTool == tool
                val label = when (tool) {
                    AdvancedToolType.EQUATIONS -> "Equations"
                    AdvancedToolType.MATRIX -> "Matrices"
                    AdvancedToolType.BASE_CONVERTER -> "Bases"
                    AdvancedToolType.UNIT_CONVERTER -> "Converter"
                    AdvancedToolType.AI_SOLVER -> "AI Solver"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTool = tool }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- SUB VIEWS ---
        when (selectedTool) {
            AdvancedToolType.EQUATIONS -> EquationSolverSubView()
            AdvancedToolType.MATRIX -> MatrixCalculatorSubView()
            AdvancedToolType.BASE_CONVERTER -> BaseConverterSubView()
            AdvancedToolType.UNIT_CONVERTER -> UnitConverterSubView()
            AdvancedToolType.AI_SOLVER -> AISolverSubView(viewModel)
        }
    }
}

// ==========================================
// 1. EQUATIONS SOLVER SUB VIEW
// ==========================================
@Composable
fun EquationSolverSubView() {
    var eqModeIsSystem by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector buttons for Quadratic vs System 2x2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { eqModeIsSystem = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (!eqModeIsSystem) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (!eqModeIsSystem) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Quadratic (ax²+bx+c=0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { eqModeIsSystem = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (eqModeIsSystem) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (eqModeIsSystem) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("2x2 Linear System", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (!eqModeIsSystem) {
            QuadraticSolverUI()
        } else {
            SystemSolverUI()
        }
    }
}

@Composable
fun QuadraticSolverUI() {
    var textA by remember { mutableStateOf("1") }
    var textB by remember { mutableStateOf("-5") }
    var textC by remember { mutableStateOf("6") }
    var resultText by remember { mutableStateOf("") }
    var stepText by remember { mutableStateOf<List<String>>(emptyList()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Solve a x² + b x + c = 0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = textA,
                    onValueChange = { textA = it },
                    label = { Text("a") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = textB,
                    onValueChange = { textB = it },
                    label = { Text("b") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = textC,
                    onValueChange = { textC = it },
                    label = { Text("c") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    val aVal = textA.toDoubleOrNull() ?: 1.0
                    val bVal = textB.toDoubleOrNull() ?: 0.0
                    val cVal = textC.toDoubleOrNull() ?: 0.0

                    if (aVal == 0.0) {
                        resultText = "Linear Equation"
                        if (bVal == 0.0) {
                            stepText = listOf("Since a = 0 and b = 0, there is no variables to solve.")
                        } else {
                            val sol = -cVal / bVal
                            stepText = listOf(
                                "Formula: b * x + c = 0",
                                "Steps:",
                                "1. Substituted: ${bVal}x + ${cVal} = 0",
                                "2. Transposed: ${bVal}x = ${-cVal}",
                                "3. x = ${-cVal} / ${bVal}",
                                "Final Answer: x = $sol"
                            )
                        }
                    } else {
                        val disc = bVal * bVal - 4 * aVal * cVal
                        val discSteps = listOf(
                            "Formula: Δ = b² - 4ac",
                            "Δ = (${bVal})² - 4 * (${aVal}) * (${cVal})",
                            "Δ = ${bVal * bVal} - ${4 * aVal * cVal}",
                            "Δ = $disc"
                        )

                        if (disc < 0) {
                            val realPart = -bVal / (2 * aVal)
                            val imagPart = sqrt(-disc) / (2 * aVal)
                            val x1 = "${String.format("%.4f", realPart)} + ${String.format("%.4f", imagPart)}i"
                            val x2 = "${String.format("%.4f", realPart)} - ${String.format("%.4f", imagPart)}i"
                            
                            resultText = "Complex Roots"
                            stepText = discSteps + listOf(
                                "Discriminant Δ < 0, roots are complex numbers.",
                                "Formula: x = [-b ± i√(-Δ)] / (2a)",
                                "x₁ = (${-bVal} + i√(${-disc})) / ${2 * aVal}",
                                "x₂ = (${-bVal} - i√(${-disc})) / ${2 * aVal}",
                                "Final Answer:",
                                "x₁ = $x1",
                                "x₂ = $x2"
                            )
                        } else if (disc == 0.0) {
                            val x = -bVal / (2 * aVal)
                            resultText = "One Single Root (Double Root)"
                            stepText = discSteps + listOf(
                                "Discriminant Δ = 0, meaning a single real root exists.",
                                "Formula: x = -b / 2a",
                                "x = ${-bVal} / (2 * ${aVal})",
                                "Final Answer: x = $x"
                            )
                        } else {
                            val r1 = (-bVal + sqrt(disc)) / (2 * aVal)
                            val r2 = (-bVal - sqrt(disc)) / (2 * aVal)
                            resultText = "Two Distinct Real Roots"
                            stepText = discSteps + listOf(
                                "Discriminant Δ > 0, roots are real numbers.",
                                "Formula: x = [-b ± √Δ] / 2a",
                                "x₁ = (${-bVal} + ${String.format("%.4f", sqrt(disc))}) / ${2 * aVal}",
                                "x₂ = (${-bVal} - ${String.format("%.4f", sqrt(disc))}) / ${2 * aVal}",
                                "Final Answer:",
                                "x₁ = ${String.format("%.4f", r1)}",
                                "x₂ = ${String.format("%.4f", r2)}"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.Check, contentDescription = "Solve")
                    Text("Solve Quadratic Formula")
                }
            }
        }
    }

    // Custom step-by-step visual ledger
    AnimatedVisibility(visible = stepText.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountTree,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                        contentDescription = null
                    )
                    Text(
                        "Algebraic Breakdown",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                stepText.forEach { line ->
                    if (line.startsWith("Final Answer:") || line.startsWith("x₁") || line.startsWith("x₂")) {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemSolverUI() {
    // Equation 1: a1 x + b1 y = c1
    // Equation 2: a2 x + b2 y = c2
    var textA1 by remember { mutableStateOf("2") }
    var textB1 by remember { mutableStateOf("3") }
    var textC1 by remember { mutableStateOf("8") }

    var textA2 by remember { mutableStateOf("1") }
    var textB2 by remember { mutableStateOf("-1") }
    var textC2 by remember { mutableStateOf("1") }

    var steps by remember { mutableStateOf<List<String>>(emptyList()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Solve system of 2 Linear Equations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text("Equation 1: a₁x + b₁y = c₁", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = textA1,
                    onValueChange = { textA1 = it },
                    label = { Text("a₁") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = textB1,
                    onValueChange = { textB1 = it },
                    label = { Text("b₁") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = textC1,
                    onValueChange = { textC1 = it },
                    label = { Text("c₁") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Text("Equation 2: a₂x + b₂y = c₂", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = textA2,
                    onValueChange = { textA2 = it },
                    label = { Text("a₂") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = textB2,
                    onValueChange = { textB2 = it },
                    label = { Text("b₂") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = textC2,
                    onValueChange = { textC2 = it },
                    label = { Text("c₂") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    val a1 = textA1.toDoubleOrNull() ?: 1.0
                    val b1 = textB1.toDoubleOrNull() ?: 1.0
                    val c1 = textC1.toDoubleOrNull() ?: 0.0

                    val a2 = textA2.toDoubleOrNull() ?: 1.0
                    val b2 = textB2.toDoubleOrNull() ?: 1.0
                    val c2 = textC2.toDoubleOrNull() ?: 0.0

                    // Cramer's rule determinants
                    val d = a1 * b2 - a2 * b1
                    val dx = c1 * b2 - c2 * b1
                    val dy = a1 * c2 - a2 * c1

                    val calcSteps = mutableListOf<String>()
                    calcSteps.add("System Equations:")
                    calcSteps.add("1) ${a1}x + (${b1})y = ${c1}")
                    calcSteps.add("2) ${a2}x + (${b2})y = ${c2}")
                    calcSteps.add("")
                    calcSteps.add("Matrix Determinants (Cramer's Rule):")
                    calcSteps.add("D  = a₁*b₂ - a₂*b₁ = $a1*$b2 - $a2*$b1 = $d")
                    calcSteps.add("D_x = c₁*b₂ - c₂*b₁ = $c1*$b2 - $c2*$b1 = $dx")
                    calcSteps.add("D_y = a₁*c₂ - a₂*c₁ = $a1*$c2 - $a2*$c1 = $dy")

                    if (d == 0.0) {
                        if (dx == 0.0 && dy == 0.0) {
                            calcSteps.add("D = 0, D_x = 0, D_y = 0: Infinitely many solutions.")
                        } else {
                            calcSteps.add("D = 0 and (D_x ≠ 0 or D_y ≠ 0): No unique solution (Parallel Lines).")
                        }
                    } else {
                        val solX = dx / d
                        val solY = dy / d
                        calcSteps.add("")
                        calcSteps.add("Solutions calculated:")
                        calcSteps.add("x = D_x / D = $dx / $d = $solX")
                        calcSteps.add("y = D_y / D = $dy / $d = $solY")
                        calcSteps.add("")
                        calcSteps.add("Final Answer: x = ${String.format("%.4f", solX)}, y = ${String.format("%.4f", solY)}")
                    }
                    steps = calcSteps
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.Check, contentDescription = "Solve")
                    Text("Solve System via Cramer's Rule")
                }
            }
        }
    }

    AnimatedVisibility(visible = steps.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountTree,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                        contentDescription = null
                    )
                    Text(
                        "Cramer's Rule Breakdown",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                steps.forEach { line ->
                    if (line.startsWith("Final Answer:")) {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. MATRICES CALCULATOR SUB VIEW
// ==========================================
@Composable
fun MatrixCalculatorSubView() {
    var matrixSize by remember { mutableStateOf(2) } // 2 for 2x2, 3 for 3x3

    // Matrix A values (Max size 3x3)
    val aGrid = remember {
        mutableStateListOf(
            mutableStateListOf("1", "2", "3"),
            mutableStateListOf("0", "1", "4"),
            mutableStateListOf("5", "6", "0")
        )
    }

    // Matrix B values (Max size 3x3)
    val bGrid = remember {
        mutableStateListOf(
            mutableStateListOf("2", "0", "-1"),
            mutableStateListOf("1", "3", "2"),
            mutableStateListOf("0", "1", "1")
        )
    }

    var outputMatrixSteps by remember { mutableStateOf("") }
    var output2dGrid by remember { mutableStateOf<List<List<Double>>?>(null) }
    var outputScalar by remember { mutableStateOf<Double?>(null) }
    var matrixOpName by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Size chooser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Matrix Operations Panel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SuggestionChip(
                            onClick = { matrixSize = 2 },
                            label = { Text("2 x 2") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (matrixSize == 2) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                        SuggestionChip(
                            onClick = { matrixSize = 3 },
                            label = { Text("3 x 3") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (matrixSize == 3) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // MATRIX A INPUT
                Text("Matrix A:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (r in 0 until matrixSize) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (c in 0 until matrixSize) {
                                OutlinedTextField(
                                    value = aGrid[r][c],
                                    onValueChange = { aGrid[r][c] = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }

                // MATRIX B INPUT
                Text("Matrix B (For Addition/Multiplication):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (r in 0 until matrixSize) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (c in 0 until matrixSize) {
                                OutlinedTextField(
                                    value = bGrid[r][c],
                                    onValueChange = { bGrid[r][c] = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // OPERATIONS CHIPS GRID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            matrixOpName = "Determinant of Matrix A"
                            output2dGrid = null
                            val matrix = mutableListOf<List<Double>>()
                            try {
                                for (r in 0 until matrixSize) {
                                    matrix.add((0 until matrixSize).map { c -> aGrid[r][c].toDoubleOrNull() ?: 0.0 })
                                }
                                if (matrixSize == 2) {
                                    val det = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]
                                    outputScalar = det
                                    outputMatrixSteps = "det(A) = ad - bc = ${matrix[0][0]}*${matrix[1][1]} - ${matrix[0][1]}*${matrix[1][0]} = $det"
                                } else {
                                    // 3x3 determinant expanding along row 0
                                    val a = matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
                                    val b = matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
                                    val c = matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0])
                                    val det = a - b + c
                                    outputScalar = det
                                    outputMatrixSteps = "Formula: det(A) = a(ei-fh) - b(di-fg) + c(dh-eg)\n" +
                                            "= ${matrix[0][0]}*(${matrix[1][1]}*${matrix[2][2]} - ${matrix[1][2]}*${matrix[2][1]})" +
                                            " - ${matrix[0][1]}*(${matrix[1][0]}*${matrix[2][2]} - ${matrix[1][2]}*${matrix[2][0]})" +
                                            " + ${matrix[0][2]}*(${matrix[1][0]}*${matrix[2][1]} - ${matrix[1][1]}*${matrix[2][0]})\n" +
                                            "= $a - $b + $c = $det"
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Please verify matrices are fully filled with real numbers", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Det(A)", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            matrixOpName = "Transpose of A"
                            outputScalar = null
                            val matrix = mutableListOf<List<Double>>()
                            try {
                                for (r in 0 until matrixSize) {
                                    matrix.add((0 until matrixSize).map { c -> aGrid[r][c].toDoubleOrNull() ?: 0.0 })
                                }
                                val transposed = (0 until matrixSize).map { c ->
                                    (0 until matrixSize).map { r -> matrix[r][c] }
                                }
                                output2dGrid = transposed
                                outputMatrixSteps = "Columns swapped with rows beautifully."
                            } catch (e: Exception) {
                                Toast.makeText(context, "Input numeric values first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Trsp(A)", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            matrixOpName = "A + B Matrix Addition"
                            outputScalar = null
                            try {
                                val a = (0 until matrixSize).map { r -> (0 until matrixSize).map { c -> aGrid[r][c].toDoubleOrNull() ?: 0.0 } }
                                val b = (0 until matrixSize).map { r -> (0 until matrixSize).map { c -> bGrid[r][c].toDoubleOrNull() ?: 0.0 } }
                                val res = (0 until matrixSize).map { r ->
                                    (0 until matrixSize).map { c -> a[r][c] + b[r][c] }
                                }
                                output2dGrid = res
                                outputMatrixSteps = "Element-by-element sums completed."
                            } catch (e: Exception) {
                                Toast.makeText(context, "Input numeric matrices first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("A + B", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            matrixOpName = "A * B Matrix Multiplication"
                            outputScalar = null
                            try {
                                val a = (0 until matrixSize).map { r -> (0 until matrixSize).map { c -> aGrid[r][c].toDoubleOrNull() ?: 0.0 } }
                                val b = (0 until matrixSize).map { r -> (0 until matrixSize).map { c -> bGrid[r][c].toDoubleOrNull() ?: 0.0 } }
                                val res = (0 until matrixSize).map { r ->
                                    (0 until matrixSize).map { c ->
                                        var cellSum = 0.0
                                        for (k in 0 until matrixSize) {
                                            cellSum += a[r][k] * b[k][c]
                                        }
                                        cellSum
                                    }
                                }
                                output2dGrid = res
                                outputMatrixSteps = "Formula: C_ij = Σ(A_ik * B_kj) row Dot column multiplications"
                            } catch (e: Exception) {
                                Toast.makeText(context, "Input correct matrices first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("A * B", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        // OUTPUT DISPLAY REGION
        if (outputScalar != null || output2dGrid != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Result: $matrixOpName",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                    // If Scalar Determinant result:
                    if (outputScalar != null) {
                        Text(
                            text = "= ${String.format("%.4f", outputScalar)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // If 2D Grid Matrix result:
                    if (output2dGrid != null) {
                        Column(
                            modifier = Modifier
                                .wrapContentSize()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            output2dGrid?.forEach { rList ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    rList.forEach { valCell ->
                                        Text(
                                            text = if (valCell % 1.0 == 0.0) valCell.toLong().toString() else String.format("%.3f", valCell),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(64.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = outputMatrixSteps,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


// ==========================================
// 3. BASE CONVERTER SUB VIEW
// ==========================================
@Composable
fun BaseConverterSubView() {
    var rawInput by remember { mutableStateOf("100") }
    var selectedInputBase by remember { mutableStateOf(10) } // 2, 8, 10, or 16

    val decValue = remember(rawInput, selectedInputBase) {
        val raw = rawInput.trim().replace(" ", "")
        if (raw.isEmpty()) return@remember 0L
        try {
            when (selectedInputBase) {
                2 -> raw.toLong(2)
                8 -> raw.toLong(8)
                10 -> raw.toLong(10)
                16 -> raw.toLong(16)
                else -> 0L
            }
        } catch (e: Exception) {
            -1L // Error parsing
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Numerical Base System Shifter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Input Base Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Select Input Base Source:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(2, 8, 10, 16).forEach { base ->
                            val label = when (base) {
                                2 -> "BIN"
                                8 -> "OCT"
                                10 -> "DEC"
                                16 -> "HEX"
                                else -> "DEC"
                            }
                            SuggestionChip(
                                onClick = { selectedInputBase = base },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedInputBase == base) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            )
                        }
                    }
                }

                val baseLabel = when (selectedInputBase) {
                    2 -> "Binary"
                    8 -> "Octal"
                    10 -> "Decimal"
                    16 -> "Hex"
                    else -> "Dec"
                }

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    label = { Text("Value to convert ($baseLabel)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
                )

                if (decValue == -1L) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Invalid format for radix-$selectedInputBase numerical set.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        if (decValue != -1L) {
            val basesList = listOf(
                Triple("DECIMAL (Radix-10)", decValue.toString(10).uppercase(), Icons.Outlined.Pin),
                Triple("BINARY (Radix-2)", decValue.toString(2).uppercase().chunked(4).joinToString(" "), Icons.Outlined.Dataset),
                Triple("OCTAL (Radix-8)", decValue.toString(8).uppercase(), Icons.Outlined.Tag),
                Triple("HEXADECIMAL (Radix-16)", decValue.toString(16).uppercase(), Icons.Outlined.Code)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                basesList.forEach { (title, outputVal, icon) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = outputVal.ifEmpty { "0" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. UNIT CONVERTER SUB VIEW
// ==========================================
enum class UnitCategory {
    LENGTH, WEIGHT, TEMPERATURE, AREA, SPEED
}

data class ConversionUnit(val name: String, val symbol: String, val factorToAnchor: Double)

@Composable
fun UnitConverterSubView() {
    var rawInputText by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }

    val lengthUnits = listOf(
        ConversionUnit("Kilometer", "km", 1000.0),
        ConversionUnit("Meter", "m", 1.0),
        ConversionUnit("Centimeter", "cm", 0.01),
        ConversionUnit("Millimeter", "mm", 0.001),
        ConversionUnit("Mile", "mi", 1609.344),
        ConversionUnit("Yard", "yd", 0.9144),
        ConversionUnit("Foot", "ft", 0.3048),
        ConversionUnit("Inch", "in", 0.0254)
    )

    val weightUnits = listOf(
        ConversionUnit("Kilogram", "kg", 1.0),
        ConversionUnit("Gram", "g", 0.001),
        ConversionUnit("Milligram", "mg", 0.000001),
        ConversionUnit("Pound", "lb", 0.45359237),
        ConversionUnit("Ounce", "oz", 0.028349523)
    )

    val areaUnits = listOf(
        ConversionUnit("Square Kilometer", "km²", 1000000.0),
        ConversionUnit("Square Meter", "m²", 1.0),
        ConversionUnit("Hectare", "ha", 10000.0),
        ConversionUnit("Acre", "ac", 4046.8564),
        ConversionUnit("Square Foot", "ft²", 0.09290304)
    )

    val speedUnits = listOf(
        ConversionUnit("Meter / Sec", "m/s", 1.0),
        ConversionUnit("Kilometer / Hr", "km/h", 0.27777778),
        ConversionUnit("Mile / Hour", "mph", 0.44704),
        ConversionUnit("Knot", "kt", 0.514444)
    )

    // Keep track of chosen source unit per category
    var sourceLengthUnit by remember { mutableStateOf(lengthUnits[1]) } // Meter
    var sourceWeightUnit by remember { mutableStateOf(weightUnits[0]) } // Kilogram
    var sourceAreaUnit by remember { mutableStateOf(areaUnits[1]) } // Square Meter
    var sourceSpeedUnit by remember { mutableStateOf(speedUnits[1]) } // km/h
    var sourceTempUnitName by remember { mutableStateOf("Celsius (°C)") } // Celsius

    val inputValue = rawInputText.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Universal Unit Converter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Category selector row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnitCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val label = when (cat) {
                            UnitCategory.LENGTH -> "Length"
                            UnitCategory.WEIGHT -> "Mass/Weight"
                            UnitCategory.TEMPERATURE -> "Temperature"
                            UnitCategory.AREA -> "Area"
                            UnitCategory.SPEED -> "Speed"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Input value text field
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    label = { Text("Enter Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    trailingIcon = {
                        if (rawInputText.isNotEmpty()) {
                            IconButton(onClick = { rawInputText = "" }) {
                                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )

                // Select Source Unit Header
                Text(
                    "Select Source Unit:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Active units selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (selectedCategory) {
                        UnitCategory.LENGTH -> {
                            lengthUnits.forEach { u ->
                                val isSelected = sourceLengthUnit == u
                                ElevatedSuggestionChip(
                                    onClick = { sourceLengthUnit = u },
                                    label = { Text("${u.name} (${u.symbol})", fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                        UnitCategory.WEIGHT -> {
                            weightUnits.forEach { u ->
                                val isSelected = sourceWeightUnit == u
                                ElevatedSuggestionChip(
                                    onClick = { sourceWeightUnit = u },
                                    label = { Text("${u.name} (${u.symbol})", fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                        UnitCategory.AREA -> {
                            areaUnits.forEach { u ->
                                val isSelected = sourceAreaUnit == u
                                ElevatedSuggestionChip(
                                    onClick = { sourceAreaUnit = u },
                                    label = { Text("${u.name} (${u.symbol})", fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                        UnitCategory.SPEED -> {
                            speedUnits.forEach { u ->
                                val isSelected = sourceSpeedUnit == u
                                ElevatedSuggestionChip(
                                    onClick = { sourceSpeedUnit = u },
                                    label = { Text("${u.name} (${u.symbol})", fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                        UnitCategory.TEMPERATURE -> {
                            listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)").forEach { uName ->
                                val isSelected = sourceTempUnitName == uName
                                ElevatedSuggestionChip(
                                    onClick = { sourceTempUnitName = uName },
                                    label = { Text(uName, fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // OUTPUT LIST OF EQUIVALENCES
        Text(
            text = "Converted Output Equivalence Map:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        val outputList = remember(selectedCategory, sourceLengthUnit, sourceWeightUnit, sourceAreaUnit, sourceSpeedUnit, sourceTempUnitName, inputValue) {
            val list = mutableListOf<Triple<String, String, String>>()

            when (selectedCategory) {
                UnitCategory.LENGTH -> {
                    val valueInAnchor = inputValue * sourceLengthUnit.factorToAnchor
                    lengthUnits.forEach { unit ->
                        val resultVal = valueInAnchor / unit.factorToAnchor
                        val formatted = if (resultVal % 1.0 == 0.0) resultVal.toLong().toString() else String.format("%.6f", resultVal).trimEnd('0').trimEnd('.')
                        list.add(Triple(unit.name, unit.symbol, formatted))
                    }
                }
                UnitCategory.WEIGHT -> {
                    val valueInAnchor = inputValue * sourceWeightUnit.factorToAnchor
                    weightUnits.forEach { unit ->
                        val resultVal = valueInAnchor / unit.factorToAnchor
                        val formatted = if (resultVal % 1.0 == 0.0) resultVal.toLong().toString() else String.format("%.6f", resultVal).trimEnd('0').trimEnd('.')
                        list.add(Triple(unit.name, unit.symbol, formatted))
                    }
                }
                UnitCategory.AREA -> {
                    val valueInAnchor = inputValue * sourceAreaUnit.factorToAnchor
                    areaUnits.forEach { unit ->
                        val resultVal = valueInAnchor / unit.factorToAnchor
                        val formatted = if (resultVal % 1.0 == 0.0) resultVal.toLong().toString() else String.format("%.6f", resultVal).trimEnd('0').trimEnd('.')
                        list.add(Triple(unit.name, unit.symbol, formatted))
                    }
                }
                UnitCategory.SPEED -> {
                    val valueInAnchor = inputValue * sourceSpeedUnit.factorToAnchor
                    speedUnits.forEach { unit ->
                        val resultVal = valueInAnchor / unit.factorToAnchor
                        val formatted = if (resultVal % 1.0 == 0.0) resultVal.toLong().toString() else String.format("%.6f", resultVal).trimEnd('0').trimEnd('.')
                        list.add(Triple(unit.name, unit.symbol, formatted))
                    }
                }
                UnitCategory.TEMPERATURE -> {
                    // Convert chosen to Celsius first
                    val celsius = when (sourceTempUnitName) {
                        "Celsius (°C)" -> inputValue
                        "Fahrenheit (°F)" -> (inputValue - 32.0) * 5.0 / 9.0
                        "Kelvin (K)" -> inputValue - 273.15
                        else -> inputValue
                    }
                    // Celsius Output
                    val cFormatted = if (celsius % 1.0 == 0.0) celsius.toLong().toString() else String.format("%.4f", celsius).trimEnd('0').trimEnd('.')
                    list.add(Triple("Celsius", "°C", cFormatted))

                    // Fahrenheit Output
                    val fVal = celsius * 9.0 / 5.0 + 32.0
                    val fFormatted = if (fVal % 1.0 == 0.0) fVal.toLong().toString() else String.format("%.4f", fVal).trimEnd('0').trimEnd('.')
                    list.add(Triple("Fahrenheit", "°F", fFormatted))

                    // Kelvin Output
                    val kVal = celsius + 273.15
                    val kFormatted = if (kVal % 1.0 == 0.0) kVal.toLong().toString() else String.format("%.4f", kVal).trimEnd('0').trimEnd('.')
                    list.add(Triple("Kelvin", "K", kFormatted))
                }
            }
            list
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            outputList.forEach { (name, symbol, converted) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = symbol,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Text(
                            text = converted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. AI SOLVER SUB VIEW & HELPER
// ==========================================
data class ParsedSolution(
    val expression: String = "",
    val concepts: String = "",
    val steps: List<String> = emptyList(),
    val finalAnswer: String = ""
)

fun parseGeminiSolution(raw: String): ParsedSolution {
    var expression = ""
    var concepts = ""
    val steps = mutableListOf<String>()
    var finalAnswer = ""

    val sections = raw.split("###")
    for (sec in sections) {
        val trimmed = sec.trim()
        if (trimmed.isEmpty()) continue

        val lines = trimmed.lines()
        val header = lines.firstOrNull()?.replace("*", "")?.trim() ?: ""
        val bodyLines = lines.drop(1)
        val bodyText = bodyLines.joinToString("\n").trim()

        when {
            header.contains("Expression", ignoreCase = true) -> {
                expression = bodyText.replace("*", "").trim()
            }
            header.contains("Concepts", ignoreCase = true) || header.contains("Theory", ignoreCase = true) || header.contains("Axioms", ignoreCase = true) -> {
                concepts = bodyText
            }
            header.contains("Step", ignoreCase = true) || header.contains("Solution", ignoreCase = true) || header.contains("Calculation", ignoreCase = true) -> {
                val stepLines = bodyText.lines()
                for (line in stepLines) {
                    val lineTrim = line.trim()
                    if (lineTrim.startsWith("-") || lineTrim.startsWith("*") || (lineTrim.isNotEmpty() && lineTrim.first().isDigit())) {
                        steps.add(lineTrim.replaceFirst(Regex("^[-*\\s\\d.]*"), "").trim())
                    } else if (lineTrim.isNotEmpty()) {
                        steps.add(lineTrim)
                    }
                }
            }
            header.contains("Answer", ignoreCase = true) || header.contains("Result", ignoreCase = true) -> {
                finalAnswer = bodyText.replace("*", "").trim()
            }
        }
    }

    if (expression.isEmpty() && concepts.isEmpty() && steps.isEmpty() && finalAnswer.isEmpty()) {
        return ParsedSolution(
            expression = "Input Equation",
            concepts = "Theoretical details included below",
            steps = raw.lines().filter { it.trim().isNotEmpty() },
            finalAnswer = "See calculation steps"
        )
    }

    return ParsedSolution(
        expression = expression,
        concepts = concepts,
        steps = steps,
        finalAnswer = finalAnswer
    )
}

@Composable
fun AISolverSubView(viewModel: CalculatorViewModel) {
    val solverInput by viewModel.solverInput.collectAsState()
    val ocrState by viewModel.ocrState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Solver Header / Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "AI Solver Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Step-by-Step Solver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Understand formulas, algebra, calculus, and more with deep educational steps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Equation input field
        OutlinedTextField(
            value = solverInput,
            onValueChange = { viewModel.updateSolverInput(it) },
            label = { Text("Equation or Expression") },
            placeholder = { Text("e.g. x^2 - 5x + 6 = 0 or integrate x * cos(x)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_solver_input_field"),
            shape = RoundedCornerShape(12.dp),
            singleLine = false,
            maxLines = 3,
            trailingIcon = {
                if (solverInput.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.updateSolverInput("") },
                        modifier = Modifier.testTag("clear_ai_solver_input")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Clear input"
                        )
                    }
                }
            }
        )

        // Suggestion chips row
        Text(
            text = "Tap a sample equation to try:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Bold
        )

        val sampleEquations = listOf(
            "3x + 5 = 20",
            "x^2 - 5x + 6 = 0",
            "integrate x * ln(x) dx",
            "sin(x) * cos(x) = 0.5",
            "limit (x^2 - 4)/(x - 2) as x->2"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleEquations.forEach { eq ->
                SuggestionChip(
                    onClick = { viewModel.updateSolverInput(eq) },
                    label = { Text(eq, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    modifier = Modifier.testTag("sample_chip_${eq.replace(" ", "_")}")
                )
            }
        }

        // Row of action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.clearSolver() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Clear State"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }

            Button(
                onClick = {
                    if (solverInput.trim().isEmpty()) {
                        Toast.makeText(context, "Please enter an equation first", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.solveWithAI(solverInput, null, null)
                    }
                },
                modifier = Modifier
                    .weight(2f)
                    .testTag("btn_solve_with_ai"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "Solve Equation"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solve with AI Tutor")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // State displays
        when (val state = ocrState) {
            is OcrState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = "School Icon",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No solution compiled yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Enter any equation and hit Solve to see beautiful explanations and theoretical breakdowns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            is OcrState.Loading -> {
                val infiniteTransition = rememberInfiniteTransition(label = "loading")
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )

                var loadingHint by remember { mutableStateOf("Formulating mathematical bounds...") }
                val hints = listOf(
                    "Formulating mathematical bounds...",
                    "Analyzing operands and axioms...",
                    "Generating step-by-step logic...",
                    "Double-checking algebraic accuracy..."
                )
                LaunchedEffect(Unit) {
                    var index = 0
                    while (true) {
                        kotlinx.coroutines.delay(2500)
                        index = (index + 1) % hints.size
                        loadingHint = hints[index]
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Spinning loader",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(angle)
                        )

                        Text(
                            text = "Consulting Gemini AI...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = loadingHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )

                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            is OcrState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "Error Icon",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Solver Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Button(
                            onClick = { viewModel.solveWithAI(solverInput, null, null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry Solving")
                        }
                    }
                }
            }

            is OcrState.Success -> {
                val parsed = remember(state.solution) { parseGeminiSolution(state.solution) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title with Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Solution Study Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(state.solution))
                                Toast.makeText(context, "Full explanation copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("btn_copy_solution")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy solution"
                            )
                        }
                    }

                    // 1. EXPRESSION CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Mathematical Expression",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = parsed.expression.ifEmpty { solverInput },
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 2. CONCEPTS CARD
                    if (parsed.concepts.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lightbulb,
                                        contentDescription = "Lightbulb Icon",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Theoretical Concepts & Axioms",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = parsed.concepts,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 3. STEPS TIMELINE CARD
                    if (parsed.steps.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Step-by-Step Explanation",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                parsed.steps.forEachIndexed { index, step ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (index + 1).toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Text(
                                            text = step,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. FINAL ANSWER CARD
                    if (parsed.finalAnswer.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Final Calculated Solution",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )

                                Text(
                                    text = parsed.finalAnswer,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
