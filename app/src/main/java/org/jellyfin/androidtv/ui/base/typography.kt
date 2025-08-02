package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object TypographyDefaults {
	val Default: TextStyle = TextStyle.Default
	val Badge: TextStyle = Default.copy(
		fontSize = 11.sp,
		fontWeight = FontWeight.W700,
		color = Color(0xFFE8EAED),
		textAlign = TextAlign.Center,
	)
}

@Immutable
data class Typography(
	val default: TextStyle = TypographyDefaults.Default,
	val badge: TextStyle = TypographyDefaults.Badge,
)

val LocalTypography = staticCompositionLocalOf { Typography() }
