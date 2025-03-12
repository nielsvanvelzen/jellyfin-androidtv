package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorScheme(
	val background: Color,
	val onBackground: Color,

	val badge: Color,
)

val darkColorScheme = ColorScheme(
	background = Color(0xFF05070A),
	onBackground = Color(0xFFFCFCFD),

	badge = Color(0xFF62676F),
)

val lightColorScheme = ColorScheme(
	background = Color(0xFFFCFCFD),
	onBackground = Color(0xFF05070A),

	badge = Color(0x66000000),
)

val LocalColorScheme = staticCompositionLocalOf { lightColorScheme }
