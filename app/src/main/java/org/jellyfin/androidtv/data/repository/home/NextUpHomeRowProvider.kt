package org.jellyfin.androidtv.data.repository.home

import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRowContext
import org.jellyfin.androidtv.data.model.home.HomeRowProvider
import org.jellyfin.androidtv.data.model.home.HomeRowTitle
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.model.api.request.GetNextUpRequest
import java.time.LocalDateTime

class NextUpHomeRowProvider(
	private val api: ApiClient,
	private val preferences: UserPreferences,
) : HomeRowProvider {
	override val sections = setOf(HomeSectionType.NEXT_UP)

	override fun createRows(context: HomeRowContext) = listOf(
		HomeRow.media(
			key = context.key,
			title = HomeRowTitle(R.string.lbl_next_up),
		) {
			val maxDays = preferences[UserPreferences.homeNextUpMaxDays]
			api.tvShowsApi.getNextUp(
				GetNextUpRequest(
					userId = context.user.id,
					limit = ITEM_LIMIT,
					fields = ItemRepository.browseFields,
					imageTypeLimit = 1,
					enableResumable = false,
					nextUpDateCutoff = maxDays.takeIf { it > 0 }?.let { LocalDateTime.now().minusDays(it.toLong()) },
				)
			).content.items
		})

	private companion object {
		const val ITEM_LIMIT = 50
	}
}
