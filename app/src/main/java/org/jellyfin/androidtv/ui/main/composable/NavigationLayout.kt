package org.jellyfin.androidtv.ui.main.composable

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import org.jellyfin.design.token.ColorTokens

@Composable
fun NavigationLayout(
	navigation: @Composable ColumnScope.() -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable BoxScope.() -> Unit,
) {
	val collapsedWidth = 64.dp
	val expandedWidth = 200.dp

	val (navigationRequester, contentRequester) = remember { FocusRequester.createRefs() }
	var navigationFocused by remember { mutableStateOf(false) }
	val navigationWidth by animateDpAsState(
		targetValue = if (navigationFocused) expandedWidth else collapsedWidth,
		label = "navigationWidth",
	)

	Layout(
		modifier = modifier
			.fillMaxSize()
			.focusRestorer(contentRequester)
			.focusGroup(),
		content = {
			// Navigation
			Column(
				modifier = Modifier
					.background(ColorTokens.colorBluegrey950)
					.fillMaxHeight()
					.width(navigationWidth)
					.onFocusChanged {
						navigationFocused = it.hasFocus
					}
					.focusRequester(navigationRequester)
					.focusRestorer()
					.focusGroup()
					.focusProperties {
						end = contentRequester
					},
				content = navigation,
			)

			// Content
			Box(
				modifier = Modifier
					.focusRequester(contentRequester)
					.focusRestorer()
					.focusGroup()
					.focusProperties {
						start = navigationRequester
					},
				content = content,
			)
		},
	) { measurables, constraints ->
		val (navigation, content) = measurables

		val navigationPlaceable = navigation.measure(
			constraints.copy(minWidth = 0, maxWidth = navigationWidth.roundToPx())
		)

		val contentWidth = constraints.maxWidth - collapsedWidth.roundToPx()
		val contentPlaceable = content.measure(
			constraints.copy(minWidth = contentWidth, maxWidth = contentWidth),
		)

		layout(
			width = constraints.maxWidth,
			height = constraints.maxHeight,
		) {
			navigationPlaceable.placeRelative(x = 0, y = 0)
			contentPlaceable.placeRelative(x = navigationWidth.roundToPx(), y = 0)
		}
	}
}
