package org.jellyfin.androidtv.ui.settings.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.LocalShapes

@Composable
fun SettingsLayout(
	navigation: @Composable BoxScope.() -> Unit,
	content: @Composable BoxScope.() -> Unit
) {
	Row(
		modifier = Modifier
			.padding(start = 6.dp, top = 0.dp, end = 6.dp, bottom = 6.dp)
			.clip(LocalShapes.current.medium)
			.background(JellyfinTheme.colorScheme.card)
	) {
		Box(
			modifier = Modifier
				.width(350.dp)
				.fillMaxHeight()
				.focusRestorer(),
			content = navigation
		)

		Box(
			modifier = Modifier
				.weight(1f)
				.fillMaxHeight()
				.background(JellyfinTheme.colorScheme.cardVariant)
				.focusRestorer(),
			content = content,
		)
	}
}
