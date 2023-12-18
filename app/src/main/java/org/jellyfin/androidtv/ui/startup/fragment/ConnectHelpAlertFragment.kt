package org.jellyfin.androidtv.ui.startup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.R

@Composable
fun ConnectHelpAlertScreen(
	onConfirm: () -> Unit,
) {
	Surface(
		color = colorResource(id = R.color.not_quite_black),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
			modifier = Modifier.fillMaxSize(),
		) {
			Text(
				text = stringResource(id = R.string.login_help_title),
				color = colorResource(id = R.color.white),
				fontSize = 44.sp,
			)

			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.width(500.dp)
			) {
				Image(
					painter = painterResource(R.drawable.qr_jellyfin_docs),
					contentDescription = null,
					modifier = Modifier.width(160.dp)
				)

				Text(
					text = stringResource(id = R.string.login_help_description),
					color = colorResource(id = R.color.white),
					modifier = Modifier.width(276.dp)
				)
			}

			Button(
				onClick = onConfirm,
			) {
				Text(
					text = stringResource(id = R.string.lbl_ok)
				)
			}
		}
	}
}

class ConnectHelpAlertFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	) = ComposeView(requireContext()).apply {
		setContent {
			ConnectHelpAlertScreen(
				onConfirm = {
					parentFragmentManager.popBackStack()
				}
			)
		}
	}
}
