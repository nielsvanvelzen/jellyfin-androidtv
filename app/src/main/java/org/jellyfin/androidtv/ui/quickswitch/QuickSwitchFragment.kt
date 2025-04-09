package org.jellyfin.androidtv.ui.quickswitch

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.navigation.ActivityDestinations
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActivebutton
import org.jellyfin.androidtv.ui.startup.StartupActivity
import org.jellyfin.androidtv.util.getActivity
import org.koin.compose.koinInject

@Composable
fun QuickSwitchMenu(
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val sessionRepository = koinInject<SessionRepository>()
	val mediaManager = koinInject<MediaManager>()

	Column(modifier) {
		Button(
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				context.startActivity(ActivityDestinations.userPreferences(context))
			}
		) {
			Text("Settings")
		}

		Button(
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				val activity = context.getActivity()

				mediaManager.clearAudioQueue()
				sessionRepository.destroyCurrentSession()

				// Open login activity
				val selectUserIntent = Intent(activity, StartupActivity::class.java)
				selectUserIntent.putExtra(StartupActivity.EXTRA_HIDE_SPLASH, true)
				// Remove history to prevent user going back to current activity
				selectUserIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

				activity?.startActivity(selectUserIntent)
				activity?.finishAfterTransition()
			}
		) {
			Text("Switch user")
		}
	}
}

class QuickSwitchFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	) = content {
		Column {
			MainToolbar(MainToolbarActivebutton.USER)

			// TODO: Only show if there is AT LEAST 1 different available user in the same server
			Text("Switch user")
			Row {
				// TODO Show recent users in a row, with profile picture & name below (similar to auth UI)
			}

			QuickSwitchMenu(
				modifier = Modifier.width(300.dp)
			)
		}
	}
}
