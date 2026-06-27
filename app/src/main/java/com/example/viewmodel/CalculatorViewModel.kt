package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationHistory
import com.example.data.GeminiClient
import com.example.data.HistoryRepository
import com.example.util.MathParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.*

enum class UnitCategory {
    LENGTH, MASS, TEMPERATURE, AREA, TIME
}

sealed interface OcrState {
    object Idle : OcrState
    object Loading : OcrState
    data class Success(val solution: String) : OcrState
    data class Error(val message: String) : OcrState
}

class CalculatorViewModel(private val repository: HistoryRepository) : ViewModel() {

    // --- History Stream (Room reactive DB) ---
    val historyState: StateFlow<List<CalculationHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Scientific Calc State ---
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _calcResult = MutableStateFlow("")
    val calcResult: StateFlow<String> = _calcResult.asStateFlow()

    private val _isRadMode = MutableStateFlow(true)
    val isRadMode: StateFlow<Boolean> = _isRadMode.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    private var justEvaluated = false

    // --- Graphing State ---
    private val _graphExpression = MutableStateFlow("sin(x)")
    val graphExpression: StateFlow<String> = _graphExpression.asStateFlow()

    private val _graphStatus = MutableStateFlow("")
    val graphStatus: StateFlow<String> = _graphStatus.asStateFlow()

    // --- OCR & Step Solver State ---
    private val _solverInput = MutableStateFlow("")
    val solverInput: StateFlow<String> = _solverInput.asStateFlow()

    private val _ocrState = MutableStateFlow<OcrState>(OcrState.Idle)
    val ocrState: StateFlow<OcrState> = _ocrState.asStateFlow()

    private val _isParsingImage = MutableStateFlow(false)
    val isParsingImage: StateFlow<Boolean> = _isParsingImage.asStateFlow()

    private val _parseError = MutableStateFlow<String?>(null)
    val parseError: StateFlow<String?> = _parseError.asStateFlow()

    private val _calcExplanationState = MutableStateFlow<OcrState>(OcrState.Idle)
    val calcExplanationState: StateFlow<OcrState> = _calcExplanationState.asStateFlow()

    // --- Unit Converter State ---
    private val _converterCategory = MutableStateFlow(UnitCategory.LENGTH)
    val converterCategory: StateFlow<UnitCategory> = _converterCategory.asStateFlow()

    private val _convFromValue = MutableStateFlow("1")
    val convFromValue: StateFlow<String> = _convFromValue.asStateFlow()

    private val _convFromUnit = MutableStateFlow("m")
    val convFromUnit: StateFlow<String> = _convFromUnit.asStateFlow()

    private val _convToUnit = MutableStateFlow("ft")
    val convToUnit: StateFlow<String> = _convToUnit.asStateFlow()

    private val _convResult = MutableStateFlow("3.28084")
    val convResult: StateFlow<String> = _convResult.asStateFlow()

    init {
        performUnitConversion()
    }

    // --- Scientific Calc Intent Actions ---
    fun appendToExpression(value: String) {
        val current = _expression.value
        
        if (justEvaluated) {
            justEvaluated = false
            val isOperator = value in listOf("+", "-", "*", "/", "%", "^") || 
                             (value.isNotEmpty() && value[0] in listOf('+', '-', '*', '/', '%', '^'))
            if (!isOperator) {
                _expression.value = value
                _calcResult.value = ""
                return
            }
        }
        _expression.value = current + value
    }

    fun clearExpression() {
        _expression.value = ""
        _calcResult.value = ""
        justEvaluated = false
    }

    fun deleteLastChar() {
        justEvaluated = false
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = current.substring(0, current.length - 1)
        }
    }

    fun toggleAngleMode() {
        _isRadMode.value = !_isRadMode.value
    }

    // --- Memory Operations ---
    fun memoryClear() {
        _memoryValue.value = 0.0
    }

    fun memoryRecall() {
        val mem = _memoryValue.value
        val formatted = if (mem % 1.0 == 0.0) mem.toLong().toString() else mem.toString()
        _expression.value = _expression.value + formatted
    }

    fun memoryAdd() {
        val currentRes = _calcResult.value.toDoubleOrNull()
        if (currentRes != null) {
            _memoryValue.value += currentRes
        } else {
            val currentExpr = _expression.value.toDoubleOrNull()
            if (currentExpr != null) {
                _memoryValue.value += currentExpr
            }
        }
    }

    fun memorySubtract() {
        val currentRes = _calcResult.value.toDoubleOrNull()
        if (currentRes != null) {
            _memoryValue.value -= currentRes
        } else {
            val currentExpr = _expression.value.toDoubleOrNull()
            if (currentExpr != null) {
                _memoryValue.value -= currentExpr
            }
        }
    }

    fun evaluateExpression() {
        val currentExpr = _expression.value
        if (currentExpr.isEmpty()) return

        try {
            val parser = MathParser(modeRad = _isRadMode.value)
            val resultValue = parser.evaluate(currentExpr)
            
            // Format result beautifully (remove trailing zeros for integers)
            val formattedResult = if (resultValue.isNaN() || resultValue.isInfinite()) {
                resultValue.toString()
            } else {
                val roundVal = (resultValue * 100000000.0).roundToLong() / 100000000.0
                if (roundVal % 1.0 == 0.0) {
                    roundVal.toLong().toString()
                } else {
                    roundVal.toString()
                }
            }

            // Save to room db
            viewModelScope.launch(Dispatchers.IO) {
                repository.insert(
                    CalculationHistory(
                        expression = currentExpr,
                        result = formattedResult,
                        isOcrOrStep = false
                    )
                )
            }

            _expression.value = formattedResult
            _calcResult.value = ""
            justEvaluated = true
        } catch (e: Exception) {
            _calcResult.value = "Error"
            justEvaluated = false
        }
    }

    // --- Graphing Actions ---
    fun updateGraphExpression(expr: String) {
        _graphExpression.value = expr
        // Validate expression
        try {
            val parser = MathParser(modeRad = true)
            parser.evaluate(expr, 1.0)
            _graphStatus.value = "" // Clear error
        } catch (e: Exception) {
            _graphStatus.value = "Syntax Error: ${e.localizedMessage ?: "Invalid function format"}"
        }
    }

    // --- Solver & OCR Actions ---
    fun updateSolverInput(input: String) {
        _solverInput.value = input
    }

    fun clearSolver() {
        _ocrState.value = OcrState.Idle
        _solverInput.value = ""
    }

    fun parseHandwrittenImage(base64Image: String, targetScreen: String = "calculator", onComplete: (String) -> Unit = {}) {
        _isParsingImage.value = true
        _parseError.value = null
        viewModelScope.launch {
            val parsedResult = GeminiClient.parseHandwrittenMath(base64Image)
            _isParsingImage.value = false
            if (parsedResult.startsWith("Error:")) {
                _parseError.value = parsedResult
            } else {
                if (targetScreen == "calculator") {
                    _expression.value = parsedResult
                } else if (targetScreen == "solver") {
                    _solverInput.value = parsedResult
                }
                onComplete(parsedResult)
            }
        }
    }

    fun clearParseError() {
        _parseError.value = null
    }

    fun explainCurrentExpression(expr: String) {
        if (expr.trim().isEmpty()) {
            _calcExplanationState.value = OcrState.Error("Please enter an equation in the calculator input field first.")
            return
        }
        _calcExplanationState.value = OcrState.Loading
        viewModelScope.launch {
            val solution = GeminiClient.solveMathProblem(expr, null)
            if (solution.startsWith("Error:") || solution.startsWith("Network or API Error:")) {
                _calcExplanationState.value = OcrState.Error(solution)
            } else {
                _calcExplanationState.value = OcrState.Success(solution)
                // Cache solution in Room history database for offline study access!
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insert(
                        CalculationHistory(
                            expression = expr,
                            result = "Explained via Gemini AI",
                            isOcrOrStep = true,
                            steps = solution
                        )
                    )
                }
            }
        }
    }

    fun clearCalcExplanation() {
        _calcExplanationState.value = OcrState.Idle
    }

    fun solveWithAI(userInput: String?, base64Image: String?, formatMimeType: String? = null) {
        _ocrState.value = OcrState.Loading
        viewModelScope.launch {
            val solution = GeminiClient.solveMathProblem(userInput, base64Image, formatMimeType)
            if (solution.startsWith("Error:") || solution.startsWith("Network or API Error:")) {
                _ocrState.value = OcrState.Error(solution)
            } else {
                _ocrState.value = OcrState.Success(solution)

                // Cache step-by-step solver solutions in Room history database for offline study access!
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insert(
                        CalculationHistory(
                            expression = userInput ?: "Scanned Math Problem Image",
                            result = "Solved via Gemini AI",
                            isOcrOrStep = true,
                            steps = solution
                        )
                    )
                }
            }
        }
    }

    // --- Unit Converter Actions ---
    fun updateConverterCategory(category: UnitCategory) {
        _converterCategory.value = category
        // Set standard unit defaults
        val (from, to) = when (category) {
            UnitCategory.LENGTH -> Pair("m", "ft")
            UnitCategory.MASS -> Pair("kg", "lb")
            UnitCategory.TEMPERATURE -> Pair("°C", "°F")
            UnitCategory.AREA -> Pair("sq m", "acre")
            UnitCategory.TIME -> Pair("hour", "min")
        }
        _convFromUnit.value = from
        _convToUnit.value = to
        performUnitConversion()
    }

    fun updateConvFromValue(value: String) {
        _convFromValue.value = value
        performUnitConversion()
    }

    fun updateConvUnits(from: String, to: String) {
        _convFromUnit.value = from
        _convToUnit.value = to
        performUnitConversion()
    }

    fun performUnitConversion() {
        val inputStr = _convFromValue.value
        val number = inputStr.toDoubleOrNull()
        if (number == null) {
            _convResult.value = ""
            return
        }

        val from = _convFromUnit.value
        val to = _convToUnit.value
        val category = _converterCategory.value

        var resultDouble = 0.0

        try {
            when (category) {
                UnitCategory.LENGTH -> {
                    // Normalize to meters reference
                    val meters = when (from) {
                        "m" -> number
                        "km" -> number * 1000.0
                        "ft" -> number / 3.28084
                        "inch" -> number / 39.3701
                        "mile" -> number / 0.000621371
                        else -> number
                    }
                    // Convert from meters
                    resultDouble = when (to) {
                        "m" -> meters
                        "km" -> meters / 1000.0
                        "ft" -> meters * 3.28084
                        "inch" -> meters * 39.3701
                        "mile" -> meters * 0.000621371
                        else -> meters
                    }
                }
                UnitCategory.MASS -> {
                    // Normalize to kg reference
                    val kgs = when (from) {
                        "kg" -> number
                        "g" -> number / 1000.0
                        "lb" -> number / 2.20462
                        "oz" -> number / 35.274
                        else -> number
                    }
                    // Convert from kg
                    resultDouble = when (to) {
                        "kg" -> kgs
                        "g" -> kgs * 1000.0
                        "lb" -> kgs * 2.20462
                        "oz" -> kgs * 35.274
                        else -> kgs
                    }
                }
                UnitCategory.TEMPERATURE -> {
                    resultDouble = when (from) {
                        "°C" -> {
                            when (to) {
                                "°C" -> number
                                "°F" -> (number * 9 / 5) + 32
                                "K" -> number + 273.15
                                else -> number
                            }
                        }
                        "°F" -> {
                            when (to) {
                                "°C" -> (number - 32) * 5 / 9
                                "°F" -> number
                                "K" -> (number - 32) * 5 / 9 + 273.15
                                else -> number
                            }
                        }
                        "K" -> {
                            when (to) {
                                "°C" -> number - 273.15
                                "°F" -> (number - 273.15) * 9 / 5 + 32
                                "K" -> number
                                else -> number
                            }
                        }
                        else -> number
                    }
                }
                UnitCategory.AREA -> {
                    // Normalize to square meters reference
                    val sqMeters = when (from) {
                        "sq m" -> number
                        "sq km" -> number * 1000000.0
                        "sq ft" -> number / 10.7639
                        "acre" -> number / 0.000247105
                        else -> number
                    }
                    // Convert from square meters
                    resultDouble = when (to) {
                        "sq m" -> sqMeters
                        "sq km" -> sqMeters / 1000000.0
                        "sq ft" -> sqMeters * 10.7639
                        "acre" -> sqMeters * 0.000247105
                        else -> sqMeters
                    }
                }
                UnitCategory.TIME -> {
                    // Normalize to seconds reference
                    val seconds = when (from) {
                        "sec" -> number
                        "min" -> number * 60.0
                        "hour" -> number * 3600.0
                        "day" -> number * 86400.0
                        else -> number
                    }
                    // Convert from seconds
                    resultDouble = when (to) {
                        "sec" -> seconds
                        "min" -> seconds / 60.0
                        "hour" -> seconds / 3600.0
                        "day" -> seconds / 86400.0
                        else -> seconds
                    }
                }
            }
            
            // Format converter result beautifully
            _convResult.value = if (resultDouble % 1.0 == 0.0) {
                resultDouble.toLong().toString()
            } else {
                ((resultDouble * 1000000.0).roundToLong() / 1000000.0).toString()
            }
        } catch (e: Exception) {
            _convResult.value = "Error"
        }
    }

    // --- History Actions ---
    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    fun loadHistoryItem(calc: CalculationHistory) {
        if (calc.isOcrOrStep) {
            _solverInput.value = calc.expression
            _ocrState.value = OcrState.Success(calc.steps ?: "")
        } else {
            _expression.value = calc.expression
            _calcResult.value = calc.result
        }
    }
}

class CalculatorViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
