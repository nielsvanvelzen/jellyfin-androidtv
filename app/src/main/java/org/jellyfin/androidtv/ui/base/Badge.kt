package org.jellyfin.androidtv.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Badge(
	content: @Composable BoxScope.() -> Unit,
) {
	ProvideTextStyle(JellyfinTheme.typography.badge) {
		Box(
			modifier = Modifier
				.sizeIn(minWidth = 24.dp, minHeight = 24.dp)
				.wrapContentSize()
				// TODO extract color, maybe shape too?
				.background(Color(0x66000000), CircleShape)
				.padding(horizontal = 6.dp, vertical = 3.dp),
			contentAlignment = Alignment.Center,
		) {
			content()
		}
	}
}
