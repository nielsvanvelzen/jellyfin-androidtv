package org.jellyfin.androidtv.ui.base.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.LocalTextStyle
import org.jellyfin.androidtv.ui.base.ProvideTextStyle
import org.jellyfin.androidtv.ui.base.button.ButtonBase

@Composable
private fun ListItemContent(
	headingContent: @Composable () -> Unit,
	overlineContent: (@Composable () -> Unit)? = null,
	captionContent: (@Composable () -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable () -> Unit)? = null,
	headingStyle: TextStyle,
) {
	Row(
		modifier = Modifier
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		leadingContent?.let { content ->
			Box(
				modifier = Modifier
					.sizeIn(minWidth = 24.dp),
				contentAlignment = Alignment.Center,
				content = {
					ProvideTextStyle(LocalTextStyle.current.copy(color = JellyfinTheme.typography.listCaption.color)) {
						content()
					}
				}
			)
			Spacer(Modifier.width(16.dp))
		}

		Column(
			modifier = Modifier
				.weight(1f),
		) {
			overlineContent?.let { content ->
				ProvideTextStyle(JellyfinTheme.typography.listOverline) {
					content()
				}
				Spacer(Modifier.height(2.dp))
			}

			ProvideTextStyle(headingStyle) {
				headingContent()
			}

			captionContent?.let { content ->
				Spacer(Modifier.height(4.dp))
				ProvideTextStyle(JellyfinTheme.typography.listCaption) {
					content()
				}
			}
		}

		trailingContent?.let { content ->
			Spacer(Modifier.width(16.dp))

			Box(
				modifier = Modifier
					.sizeIn(minWidth = 24.dp),
				contentAlignment = Alignment.Center,
				content = { content() }
			)
		}
	}
}

@Composable
fun ListSection(
	headingContent: @Composable () -> Unit,
	overlineContent: (@Composable () -> Unit)? = null,
	captionContent: (@Composable () -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable () -> Unit)? = null,
) {
	ListItemContent(
		headingContent = headingContent,
		overlineContent = overlineContent,
		captionContent = captionContent,
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		headingStyle = JellyfinTheme.typography.listHeader,
	)
}

@Composable
fun ListButton(
	onClick: () -> Unit,
	headingContent: @Composable () -> Unit,
	overlineContent: (@Composable () -> Unit)? = null,
	captionContent: (@Composable () -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable () -> Unit)? = null,
) {
	ButtonBase(
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
	) {
		ListItemContent(
			headingContent = headingContent,
			overlineContent = overlineContent,
			captionContent = captionContent,
			leadingContent = leadingContent,
			trailingContent = trailingContent,
			headingStyle = JellyfinTheme.typography.listHeadline,
		)
	}
}
