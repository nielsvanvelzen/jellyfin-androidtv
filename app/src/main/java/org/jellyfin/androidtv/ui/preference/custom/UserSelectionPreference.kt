package org.jellyfin.androidtv.ui.preference.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import java.util.*

class UserSelectionPreference(
	context: Context,
	attrs: AttributeSet? = null
) : DialogPreference(context, attrs) {
	var allowDisable: Boolean = true
	var allowLatest: Boolean = true
	var servers: List<UserSelectorServer> = emptyList()

	data class UserSelectorServer(
		val id: UUID,
		val name: String,
		val users: List<UserSelectorUser>
	)

	data class UserSelectorUser(
		val id: UUID,
		val name: String
	)
}

