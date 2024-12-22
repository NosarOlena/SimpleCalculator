package com.example.simplecalculator

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.simplecalculator.databinding.ActivityMainBinding
import java.util.Locale
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnC.setOnClickListener {
            val currentText = binding.expression.text.toString()
            if (currentText.isNotEmpty()) {
                binding.expression.text = currentText.substring(0, currentText.length - 1)
            }
            binding.result.text = ""
            scrollToEnd()
        }

        binding.btnAC.setOnClickListener {
            binding.expression.text = ""
            binding.result.text = ""
            scrollToEnd()
        }

        val operationSymbols = setOf('+', '-', '×', '÷', '.')
        val brackets = setOf('(', ')')

        binding.btnAdd.setOnClickListener {
            appendOperator("+")
            scrollToEnd()
        }

        binding.btnSubtract.setOnClickListener {
            val currText = binding.expression.text.toString().replace(" ", "")
            if (currText.isNotEmpty()) {
                val lastChar = currText.last()
                if (lastChar !in operationSymbols) {
                    if (lastChar == '(') {
                        binding.expression.append("-")
                    } else {
                        binding.expression.append(" - ")
                    }
                }
            } else {
                binding.expression.append("-")
            }
            scrollToEnd()
        }

        binding.btnMultiply.setOnClickListener {
            appendOperator("×")
            scrollToEnd()
        }

        binding.btnDivide.setOnClickListener {
            appendOperator("÷")
            scrollToEnd()
        }

        binding.btnDot.setOnClickListener {
            val currText = binding.expression.text.toString().replace(" ", "")
            if (currText.isNotEmpty()) {
                val lastToken = binding.expression.text.split(" ").last()
                if ("." !in lastToken) {
                    val lastChar = currText.last()
                    if (lastChar !in operationSymbols && lastChar !in brackets) {
                        binding.expression.append(".")
                    }
                }
            }
            scrollToEnd()
        }

        binding.btnOpenBracket.setOnClickListener {
            val currText = binding.expression.text.toString().replace(" ", "")
            if (currText.isEmpty() || currText.last() in setOf('+', '-', '×', '÷', '(')) {
                binding.expression.append("(")
            }
            scrollToEnd()
        }

        binding.btnClosedBracket.setOnClickListener {
            val currText = binding.expression.text.toString().replace(" ", "")
            val openBracketCount = currText.count { it == '(' }
            val closeBracketCount = currText.count { it == ')' }
            if (currText.last() !in operationSymbols && openBracketCount > closeBracketCount) {
                binding.expression.append(")")
            }
            scrollToEnd()
        }

        binding.btnEquals.setOnClickListener {
            val currText = binding.expression.text.toString().replace(" ", "")
            val lastChar = currText.last()
            if (currText.isNotEmpty() && lastChar !in operationSymbols && currText.count {it == '('} == currText.count{it == ')'}) {
                val expressionInInfix = binding.expression.text.toString()
                try {
                    val expressionInPostfix = convertToPostfix(expressionInInfix)
                    val computationResult = calculateByPostfix(expressionInPostfix)
                    binding.result.text = "$computationResult"
                } catch (e: ArithmeticException) {
                    binding.result.text = "${e.message}"
                } catch (e: Exception) {
                    binding.result.text = "Invalid expression"
                }
            }
        }

        binding.btn0.setOnClickListener { appendDecimal("0") }
        binding.btn1.setOnClickListener { appendDecimal("1") }
        binding.btn2.setOnClickListener { appendDecimal("2") }
        binding.btn3.setOnClickListener { appendDecimal("3") }
        binding.btn4.setOnClickListener { appendDecimal("4") }
        binding.btn5.setOnClickListener { appendDecimal("5") }
        binding.btn6.setOnClickListener { appendDecimal("6") }
        binding.btn7.setOnClickListener { appendDecimal("7") }
        binding.btn8.setOnClickListener { appendDecimal("8") }
        binding.btn9.setOnClickListener { appendDecimal("9") }
    }

    private fun appendDecimal(decimalString: String) {
        val lastToken = binding.expression.text.split(" ").last()
        if (lastToken.length == 1 && lastToken == "0") {
            return
        }
        binding.expression.append(decimalString)
        scrollToEnd()
    }

    private fun appendOperator(operator: String) {
        val currText = binding.expression.text.toString().replace(" ", "")
        if (currText.isNotEmpty()) {
            val lastChar = currText.last()
            if (lastChar !in setOf('+', '-', '×', '÷', '.', '(')) {
                binding.expression.append(" $operator ")
            }
        }
    }

    private fun convertToPostfix(expression: String): List<String> {
        val stack = Stack<String>()
        val result = mutableListOf<String>()
        val modifiedExpression = expression.replace(" ", "")

        val operators = mapOf(
            "+" to 1,
            "-" to 1,
            "×" to 2,
            "÷" to 2
        )

        val tokens = tokenize(modifiedExpression)

        for (token in tokens) {
            when {
                token.isNumeric() -> result.add(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        result.add(stack.pop())
                    }
                    if (stack.isNotEmpty()) stack.pop()
                }
                token in operators -> {
                    while (
                        stack.isNotEmpty() &&
                        stack.peek() != "(" &&
                        operators[stack.peek()]!! >= operators[token]!!
                    ) {
                        result.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            result.add(stack.pop())
        }

        return result
    }


    private fun String.isNumeric(): Boolean = this.toDoubleOrNull() != null

    private fun operation(left: Double, operator: String, right: Double): Double {
        return when (operator) {
            "+" -> left + right
            "-" -> left - right
            "×" -> left * right
            "÷" -> {
                if (right == 0.0) {
                    throw ArithmeticException("Division by zero")
                } else {
                    left / right
                }
            }
            else -> throw IllegalArgumentException("Unknown operator: $operator")
        }
    }

    private fun calculateByPostfix(tokens: List<String>): Double {
        val stack = Stack<Double>()

        for (token in tokens) {
            if (token.isNumeric()) {
                stack.push(token.toDouble())
            } else {
                val rightOperand = stack.pop()
                val leftOperand = stack.pop()
                val result = operation(leftOperand, token, rightOperand)
                stack.push(result)
            }
        }

        return String.format(Locale.US, "%.8f", stack.pop()).toDouble()
    }

    private fun tokenize(expression: String): List<String> {
        val result = mutableListOf<String>()
        val number = StringBuilder()

        for ((index, char) in expression.withIndex()) {
            when {
                char.isDigit() || char == '.' -> number.append(char)

                char == '-' -> {
                    if (number.isNotEmpty()) {
                        result.add(number.toString())
                        number.clear()
                    }

                    if (index == 0 || expression[index - 1] in listOf('(', '+', '-', '×', '÷')) {
                        number.append(char)
                    } else {
                        result.add(char.toString())
                    }
                }

                char in listOf('(', ')', '+', '×', '÷') -> {
                    if (number.isNotEmpty()) {
                        result.add(number.toString())
                        number.clear()
                    }
                    result.add(char.toString())
                }
            }
        }

        if (number.isNotEmpty()) {
            result.add(number.toString())
        }

        return result
    }

    private fun scrollToEnd() {
        binding.horizontalScrollView.post {
            binding.horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
        }
    }

}
