package org.jellyfin.androidtv.ui.preference.dsl

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.PreferenceCategory
import org.jellyfin.androidtv.auth.AuthenticationRepository
import org.jellyfin.androidtv.preference.constant.UserSelectBehavior
import org.jellyfin.androidtv.ui.preference.custom.UserSelectionPreference
import java.util.*

class OptionsItemUserSelector(
	private val context: Context,
	private val authenticationRepository: AuthenticationRepository,
) : OptionsItemMutable<OptionsItemUserSelector.UserSelection>() {
	var allowDisable: Boolean = true
	var allowLatest: Boolean = true

	fun setTitle(@StringRes resId: Int) {
		title = context.getString(resId)
	}

	private fun getServers() = authenticationRepository.getServers().map { server ->
		UserSelectionPreference.UserSelectorServer(
			id = server.id,
			name = server.name,
			users = authenticationRepository.getUsers(server.id)?.map { user ->
				UserSelectionPreference.UserSelectorUser(
					id = user.id,
					name = user.name
				)
			}.orEmpty()
		)
	}

	override fun build(category: PreferenceCategory, container: OptionsUpdateFunContainer) {
		val pref = UserSelectionPreference(context).also {
			it.isPersistent = false
			it.key = UUID.randomUUID().toString()
			category.addPreference(it)
			it.isEnabled = dependencyCheckFun() && enabled
			it.isVisible = visible
			it.title = title
			it.dialogTitle = title
//			it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
			it.allowDisable = allowDisable
			it.allowLatest = allowLatest
			it.servers = getServers()
			it.setOnPreferenceChangeListener { _, newValue ->
//				binder.set(newValue.toString())
//				it.value = binder.get()
//				container()

				// Always return false because we save it
				false
			}
		}

		container += {
			pref.isEnabled = dependencyCheckFun() && enabled
		}
	}

	data class UserSelection(
		val behavior: UserSelectBehavior,
		val userId: UUID?
	)
}

@OptionsDSL
fun OptionsCategory.userSelector(
	authenticationRepository: AuthenticationRepository,
	init: OptionsItemUserSelector.() -> Unit
) {
	this += OptionsItemUserSelector(context, authenticationRepository).apply { init() }
}
