package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

object JellyfinTheme {
	val colorScheme: ColorScheme
		@Composable @ReadOnlyComposable get() = LocalColorScheme.current

	val typography: Typography
		@Composable @ReadOnlyComposable get() = LocalTypography.current

	val shapes: Shapes
		@Composable @ReadOnlyComposable get() = LocalShapes.current
}
