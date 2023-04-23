package org.jellyfin.androidtv.ui.composable

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R

@Composable
fun ButtonBase(
	modifier: Modifier = Modifier,
	onClick: () -> Unit,
	content: @Composable () -> Unit,
) {
	var focused by remember { mutableStateOf(false) }

	val backgroundColor = when {
		focused -> colorResource(id = R.color.button_default_highlight_background)
		else -> colorResource(id = R.color.button_default_normal_background)
	}

	val textColor = when {
		focused -> colorResource(id = R.color.button_default_highlight_text)
		else -> colorResource(id = R.color.button_default_normal_text)
	}

	CompositionLocalProvider(
		LocalIndication provides rememberRipple(color = Color.White)
	) {
		Button(
			modifier = modifier
				.onFocusEvent { focused = it.isFocused },
			onClick = onClick,
			colors = ButtonDefaults.buttonColors(
				backgroundColor = backgroundColor,
			),
			contentPadding = PaddingValues(),
		) {
			ProvideTextStyle(
				value = TextStyle.Default.copy(
					color = textColor,
					fontSize = 12.sp,
				),
				content = content,
			)
		}
	}
}
