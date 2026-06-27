package com.example.data

import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String // Clean Base64 representation without linebreaks
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: ContentResponse? = null
)

@JsonClass(generateAdapter = true)
data class ContentResponse(
    val parts: List<PartResponse>? = null
)

@JsonClass(generateAdapter = true)
data class PartResponse(
    val text: String? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Solves a string expression or a base64 math problem image.
     */
    suspend fun solveMathProblem(
        expressionText: String?,
        base64Image: String?,
        formatMimeType: String? = null
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Gemini API Key is missing. Please add your GEMINI_API_KEY inside the Secrets Panel of Google AI Studio."
        }

        val promptText = """
            You are an expert step-by-step math tutor and solver for students.
            Analyze the mathematical expression below:
            
            ${expressionText?.let { "The expression provided is: $it" } ?: "The math problem is contained within the uploaded image."}
            
            Your goal is to provide a proper, highly accurate, and mathematically sound step-by-step breakdown and theoretical explanation in standard, natural English.
            
            Follow these CRITICAL instructions:
            1. **NO PROGRAMMING CODE OR CODE-WORDS**: Do NOT write any computer programming code, scripts, or syntax blocks (such as Python code, Kotlin code, or raw software code comments). Do NOT phrase explanations using programmer terminology (like "evaluating parsed values", "float types", "string indexes", etc.). The entire theory must be written from a pure mathematical perspective.
            2. **LANGUAGE**: All explanations and theoretical summaries must be written entirely in plain, grammatically correct, friendly English.
            3. **ACCURACY**: Ensure there are no arithmetic, notation, or logical errors. The theory behind the calculation (like order of operations, trigonometry concepts, logarithms, or calculus rules) must be described precisely.
            4. **FORMATTING**: Structure your visual layout so that the Android renderer displays it beautifully:
               - Start with: "### **Mathematical Expression**" followed by the equation on a single line.
               - Write a dedicated section: "### **Theoretical Concepts & Axioms**" presenting a friendly, rich English explanation of the mathematical theory, definitions, or rules being used to solve it.
               - Write: "### **Step-by-Step Solution & Calculation**" breaking down the actual steps clearly using bullet points (`-`).
               - Place steps or equations inside formula lines starting with standard characters or enclosed in ${"$$"} triggers for visual highlighting (e.g. ${"$$"} 2 + 2 = 4 ${"$$"}).
               - Conclude with a clear final result under "### **Final Answer**" in bold.
        """.trimIndent()

        val partsList = mutableListOf<Part>()
        partsList.add(Part(text = promptText))

        if (base64Image != null && formatMimeType != null) {
            partsList.add(
                Part(
                    inlineData = InlineData(
                        mimeType = formatMimeType,
                        data = base64Image
                    )
                )
            )
        }

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = partsList)
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No solution generated. Please try again with a clearer picture or expression."
        } catch (e: Exception) {
            "Network or API Error: ${e.localizedMessage ?: e.message ?: "Failed to reach Gemini servers. Please verify your internet connection and API credential."}"
        }
    }

    /**
     * Parses a handwritten mathematical expression from an image into a clean plain calculator text.
     */
    suspend fun parseHandwrittenMath(
        base64Image: String,
        mimeType: String = "image/jpeg"
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Gemini API Key is missing. Please add your GEMINI_API_KEY inside the Secrets Panel of Google AI Studio."
        }

        val promptText = """
            You are a precise mathematical OCR scanner.
            Analyze the handwritten math problem in the image and extract the numeric or algebraic expression.
            Return ONLY the raw mathematical formula. Do NOT solve it, and do NOT include any explanatory text, markdown formatting (no backticks or ``` markers), spaces, or words.
            You must only use characters compatible with standard equations, specifically:
            - Digits: 0-9
            - Basic Operators: +, -, *, /
            - Power: ^ (e.g. x^2)
            - Square root: sqrt(...)
            - Trig: sin(...), cos(...), tan(...)
            - Logarithm: log(...) for base 10, ln(...) for natural log
            - Constants: pi (for π), e
            - Parentheses: (, )
            - Decimals: .
            
            Example output format:
            Image: handwritten "2 + 3 * (5 - 1)"
            Output: 2+3*(5-1)
            
            Image: handwritten "sin(pi/2) + sqrt(16)"
            Output: sin(pi/2)+sqrt(16)
            
            If it's not a clear math problem, return "Error: Could not parse formula".
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = promptText),
                        Part(inlineData = InlineData(mimeType = mimeType, data = base64Image))
                    )
                )
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val parsedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""
            // Clean up backticks if any were generated accidentally
            parsedText.replace("`", "").trim()
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: e.message}"
        }
    }
}
