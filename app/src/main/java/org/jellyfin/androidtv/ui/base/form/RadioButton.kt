package org.jellyfin.androidtv.ui.base.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon

@Composable
fun RadioButton(
	checked: Boolean,
	shape: Shape = CircleShape,
	color: Color = Color(0xFF05070A),
) {
	Box(
		modifier = Modifier
			.sizeIn(minWidth = 24.dp, minHeight = 24.dp)
			.background(if (checked) color else Color.Unspecified, shape)
			.border(if (checked) 0.dp else 2.dp, color, shape),
		contentAlignment = Alignment.Center,
	) {
		if (checked) {
			Icon(
				painterResource(R.drawable.ic_check),
				tint = Color.White,
				contentDescription = null,
				modifier = Modifier
					.matchParentSize()
					.padding(3.dp)
			)
		}
	}
}
