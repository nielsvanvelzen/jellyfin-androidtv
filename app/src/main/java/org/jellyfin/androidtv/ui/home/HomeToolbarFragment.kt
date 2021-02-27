package org.jellyfin.androidtv.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.TvApp
import org.jellyfin.androidtv.databinding.FragmentToolbarHomeBinding
import org.jellyfin.androidtv.util.ImageUtils
import org.jellyfin.apiclient.interaction.ApiClient
import org.koin.android.ext.android.inject

class HomeToolbarFragment : Fragment() {
	private lateinit var binding: FragmentToolbarHomeBinding
	private val apiClient: ApiClient by inject()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = FragmentToolbarHomeBinding.inflate(inflater, container, false)

		val image = TvApp.getApplication().currentUser?.let { ImageUtils.getPrimaryImageUrl(it, apiClient) }
		Glide.with(requireContext())
			.load(image)
			.error(R.drawable.ic_user)
			.centerInside()
			.into(binding.picture)

		return binding.root
	}
}
