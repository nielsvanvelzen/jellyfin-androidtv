package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R

private val figreeFontFamily = FontFamily(
	Font(R.font.figtree_regular, weight = FontWeight.Normal),
	Font(R.font.figtree_bold, weight = FontWeight.Bold),
	Font(R.font.figtree_extrabold, weight = FontWeight.ExtraBold),
	Font(R.font.figtree_black, weight = FontWeight.Black),
	Font(R.font.figtree_light, weight = FontWeight.Light),
	Font(R.font.figtree_medium, weight = FontWeight.Medium),
	Font(R.font.figtree_semibold, weight = FontWeight.SemiBold),

	Font(R.font.figtree_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
	Font(R.font.figtree_bolditalic, weight = FontWeight.Bold, style = FontStyle.Italic),
	Font(R.font.figtree_extrabolditalic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
	Font(R.font.figtree_blackitalic, weight = FontWeight.Black, style = FontStyle.Italic),
	Font(R.font.figtree_lightitalic, weight = FontWeight.Light, style = FontStyle.Italic),
	Font(R.font.figtree_mediumitalic, weight = FontWeight.Medium, style = FontStyle.Italic),
	Font(R.font.figtree_semibolditalic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
)

object TypographyDefaults {
	val Default: TextStyle = TextStyle(
		fontFamily = figreeFontFamily,
		platformStyle = PlatformTextStyle(
			includeFontPadding = false,
		)
	)

	val ListHeader: TextStyle = Default.copy(
		fontSize = 22.sp,
		lineHeight = 27.sp,
		fontWeight = FontWeight.W700,
		color = Color(0xFF05070A),
	)
	val ListOverline: TextStyle = Default.copy(
		fontSize = 12.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.W600,
		color = Color(0x99000000),
		letterSpacing = 0.88.sp,
	)
	val ListHeadline: TextStyle = Default.copy(
		fontSize = 18.sp,
		lineHeight = 24.sp,
		fontWeight = FontWeight.W600,
		color = Color(0xFF05070A),
	)
	val ListCaption: TextStyle = Default.copy(
		fontSize = 14.sp,
		lineHeight = 18.sp,
		fontWeight = FontWeight.W500,
		color = Color(0x99000000),
		letterSpacing = 0.13.sp,
	)

	val Badge: TextStyle = Default.copy(
		fontSize = 14.29.sp,
		fontWeight = FontWeight.W700,
		color = Color(0xFFE8EAED),
		textAlign = TextAlign.Center,
	)
}

@Immutable
data class Typography(
	val default: TextStyle = TypographyDefaults.Default,
	val listHeader: TextStyle = TypographyDefaults.ListHeader,
	val listOverline: TextStyle = TypographyDefaults.ListOverline,
	val listHeadline: TextStyle = TypographyDefaults.ListHeadline,
	val listCaption: TextStyle = TypographyDefaults.ListCaption,
	val badge: TextStyle = TypographyDefaults.Badge
)

val LocalTypography = staticCompositionLocalOf { Typography() }
