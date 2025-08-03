package org.jellyfin.androidtv.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import org.jellyfin.androidtv.databinding.FragmentHomeBinding
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.koin.android.ext.android.inject

class HomeFragment : Fragment() {
	private val userPreferences by inject<UserPreferences>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? = if (userPreferences[UserPreferences.experimentalUiEnabled]) {
		content {
			JellyfinTheme {
				HomeScreen()
			}
		}
	} else {
		FragmentHomeBinding.inflate(inflater, container, false).apply {
			toolbar.setContent {
				MainToolbar(MainToolbarActiveButton.Home)
			}
		}.root
	}
}
