/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.native.interop.gen

val kotlinKeywords = setOf(
        "as", "break", "class", "continue", "do", "dynamic", "else", "false", "for", "fun", "if", "in",
        "interface", "is", "null", "object", "package", "return", "super", "this", "throw",
        "true", "try", "typealias", "val", "var", "when", "while",
        // While not technically keywords, those shall be escaped as well.
        "_", "__", "___"
)

/**
 * The expression written in native language.
 */
typealias NativeExpression = String

/**
 * The expression written in Kotlin.
 */
typealias KotlinExpression = String

/**
 * For this identifier constructs the string to be parsed by Kotlin as `SimpleName`
 * defined [here](https://kotlinlang.org/docs/reference/grammar.html#SimpleName).
 */
fun String.asSimpleName(): String = if (this in kotlinKeywords) {
    "`$this`"
} else {
    this
}

/**
 * Returns the expression to be parsed by Kotlin as string literal with given contents,
 * i.e. transforms `foo$bar` to `"foo\$bar"`.
 */
fun String.quoteAsKotlinLiteral(): KotlinExpression {
    val sb = StringBuilder()
    sb.append('"')

    this.forEach { c ->
        val escaped = when (c) {
            in 'a' .. 'z', in 'A' .. 'Z', in '0' .. '9', '_', '@', ':', '{', '}', '=', '[', ']', '^', '#', '*' -> c.toString()
            '$' -> "\\$"
            else -> "\\u" + "%04X".format(c.toInt()) // TODO: improve result readability by preserving more characters.
        }
        sb.append(escaped)
    }

    sb.append('"')
    return sb.toString()
}

fun block(header: String, lines: Iterable<String>) = block(header, lines.asSequence())

fun block(header: String, lines: Sequence<String>) =
        sequenceOf("$header {") +
                lines.map { "    $it" } +
                sequenceOf("}")

val annotationForUnableToImport
    get() = "@Deprecated(${"Unable to import this declaration".quoteAsKotlinLiteral()}, level = DeprecationLevel.ERROR)"

fun String.applyToStrings(vararg arguments: String) =
        "${this}(${arguments.joinToString { it.quoteAsKotlinLiteral() }})"
