package com.example.soulvent.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * A simple markdown parser that converts *bold* and _italic_ text.
 *
 * @param text The raw text with markdown syntax.
 * @return An AnnotatedString with the appropriate styles applied.
 */
fun parseMarkdown(text: String): AnnotatedString {
    val boldRegex = """\*(.*?)\*""".toRegex()
    val italicRegex = """_(.*?)_""".toRegex()

    return buildAnnotatedString {
        append(text)

        boldRegex.findAll(text).forEach { matchResult ->
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = matchResult.range.first,
                end = matchResult.range.last + 1
            )
        }

        italicRegex.findAll(text).forEach { matchResult ->
            addStyle(
                style = SpanStyle(fontStyle = FontStyle.Italic),
                start = matchResult.range.first,
                end = matchResult.range.last + 1
            )
        }
    }
}