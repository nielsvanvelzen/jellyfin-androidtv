package org.jellyfin.androidtv.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class MarkdownBuilder : Appendable {
	private val stringBuilder = StringBuilder()

	override fun append(value: CharSequence?): MarkdownBuilder {
		stringBuilder.append(value)
		return this
	}

	override fun append(value: CharSequence?, p1: Int, p2: Int): MarkdownBuilder {
		stringBuilder.append(value)
		return this
	}

	override fun append(value: Char): MarkdownBuilder {
		stringBuilder.append(value)
		return this
	}

	override fun toString(): String {
		return stringBuilder.toString()
	}
}


@OptIn(ExperimentalContracts::class)
inline fun buildMarkdown(builderAction: MarkdownBuilder.() -> Unit): String {
	contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
	return MarkdownBuilder().apply(builderAction).toString()
}

fun MarkdownBuilder.appendSection(name: String, content: MarkdownBuilder.() -> Unit) {
	appendLine("### $name")
	appendLine()
	content()
	appendLine()
}

fun MarkdownBuilder.appendItem(name: String, value: MarkdownBuilder.() -> Unit) {
	append("***$name***: ")
	value()
	appendLine("  ")
}

fun MarkdownBuilder.appendCodeBlock(language: String, code: String?) {
	appendLine()
	appendLine("```$language")
	appendLine(code ?: "<null>")
	append("```")
}

fun MarkdownBuilder.appendValue(value: String?) {
	append("`", value ?: "<null>", "`")
}
