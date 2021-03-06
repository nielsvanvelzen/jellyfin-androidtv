package org.jellyfin.androidtv.ui.preference.custom

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.preference.LeanbackPreferenceDialogFragmentCompat
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.databinding.PreferenceRichListBinding

class RichListPreferenceDialogFragment : LeanbackPreferenceDialogFragmentCompat() {
	private lateinit var binding: PreferenceRichListBinding

	public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		val styledContext = ContextThemeWrapper(activity, R.style.PreferenceThemeOverlayLeanback)
		val styledInflater = inflater.cloneInContext(styledContext)
		binding = PreferenceRichListBinding.inflate(styledInflater, container, false)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.decorTitle.text = preference.title
	}

	companion object {
		fun newInstance(key: String) = RichListPreferenceDialogFragment().apply {
			arguments = Bundle().apply {
				putString(ARG_KEY, key)
			}
		}
	}
}
