package com.example.util

import kotlin.math.*

class MathParser(private val modeRad: Boolean = true) {
    
    fun evaluate(expression: String, xValue: Double = 0.0): Double {
        // Preprocess string: remove spaces, convert to lowercase
        var expr = expression.replace(" ", "").lowercase()
        
        // Normalize constants
        expr = expr.replace("pi", "π")
        expr = expr.replace("π", "(${Math.PI})")
        expr = expr.replace("e", "(${Math.E})")
        
        // Insert implicit multiplication where needed
        expr = insertImplicitMultiplication(expr)
        
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expr.length) expr[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expr.length) throw RuntimeException("Unexpected character at position $pos: '${ch.toChar()}'")
                return x
            }

            // expression = term | expression `+` term | expression `-` term
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else break
                }
                return x
            }

            // term = factor | term `*` factor | term `/` factor
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) {
                            throw ArithmeticException("Division by zero")
                        }
                        x /= divisor // division
                    } else if (eat('%'.code)) {
                        x %= parseFactor()
                    } else break
                }
                return x
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = expr.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions or variables
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = expr.substring(startPos, pos)
                    if (func == "x") {
                        x = xValue
                    } else {
                        if (eat('('.code)) {
                            val arg = parseExpression()
                            eat(')'.code)
                            x = when (func) {
                                "sqrt" -> {
                                    val sq = sqrt(arg)
                                    if (sq.isNaN()) throw ArithmeticException("Square root of a negative number")
                                    sq
                                }
                                "sin" -> if (modeRad) sin(arg) else sin(Math.toRadians(arg))
                                "cos" -> if (modeRad) cos(arg) else cos(Math.toRadians(arg))
                                "tan" -> if (modeRad) tan(arg) else tan(Math.toRadians(arg))
                                "asin" -> if (modeRad) asin(arg) else Math.toDegrees(asin(arg))
                                "acos" -> if (modeRad) acos(arg) else Math.toDegrees(acos(arg))
                                "atan" -> if (modeRad) atan(arg) else Math.toDegrees(atan(arg))
                                "log" -> log10(arg)
                                "ln" -> ln(arg)
                                "abs" -> abs(arg)
                                "exp" -> exp(arg)
                                else -> throw RuntimeException("Unknown function: $func")
                            }
                        } else {
                            if (func == "x") {
                                x = xValue
                            } else {
                                throw RuntimeException("Function error near '$func'. Check parenthesis.")
                            }
                        }
                    }
                } else {
                    throw RuntimeException("Unexpected symbol: '${ch.toChar()}'")
                }

                if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation

                return x
            }
        }.parse()
    }

    private fun insertImplicitMultiplication(expr: String): String {
        val result = StringBuilder()
        for (i in expr.indices) {
            val curr = expr[i]
            result.append(curr)
            if (i < expr.length - 1) {
                val next = expr[i + 1]
                
                // Logic to insert '*' between:
                // - digit and letter (e.g., 2x, 2sin)
                // - digit and '(' (e.g., 2(x+1))
                // - variable 'x'/constant and letter/paren (e.g., x(x+1))
                // - right paren ')' and digit/letter/paren (e.g., (x+1)(x+2), (x+1)2)
                
                val currentNeedsMul = curr.isDigit() || curr == ')' || curr == 'e' || curr == 'π' || curr == 'x'
                val nextNeedsMul = next.isLetter() || next == '(' || next == 'π' || next == 'e'
                
                if (currentNeedsMul && nextNeedsMul) {
                    if (next != '.') {
                        // Special check: don't insert multiply if current is 'e' or 'p' or 's' or 'c' or 't' or 'l'
                        // when it's a multi-letter function name like "sin", "cos", "tan", "ln", "log", "sqrt"
                        // But since we extract single-character tokens:
                        // 'e' could be part of exp or constant. For safety, if user typed constants and standard numbers,
                        // insert multiplication.
                        result.append('*')
                    }
                }
            }
        }
        return result.toString()
    }
}
