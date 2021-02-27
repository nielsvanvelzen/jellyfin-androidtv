package org.jellyfin.androidtv.ui.browsing

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.home.HomeFragment
import org.jellyfin.androidtv.ui.home.HomeToolbarFragment
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity(R.layout.fragment_content_view) {
	private val backgroundService: BackgroundService by inject<BackgroundService>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		supportFragmentManager
			.beginTransaction()
			.replace(R.id.content_view, HomeToolbarFragment::class.java, bundleOf())
			.add(R.id.content_view, HomeFragment::class.java, bundleOf())
			.commit()

		backgroundService.attach(this)
	}
}
