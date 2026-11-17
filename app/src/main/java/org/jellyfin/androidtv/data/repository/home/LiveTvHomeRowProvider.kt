package org.jellyfin.androidtv.data.repository.home

import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRowContext
import org.jellyfin.androidtv.data.model.home.HomeRowItem
import org.jellyfin.androidtv.data.model.home.HomeRowProvider
import org.jellyfin.androidtv.data.model.home.HomeRowTitle
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.liveTvApi
import org.jellyfin.sdk.model.api.request.GetRecommendedProgramsRequest

class LiveTvHomeRowProvider(private val api: ApiClient) : HomeRowProvider {
	override val sections = setOf(HomeSectionType.LIVE_TV)

	override fun createRows(context: HomeRowContext): List<HomeRow> {
		if (context.user.policy?.enableLiveTvAccess != true) return emptyList()
		return listOf(
			HomeRow(
				key = "${context.key}:actions",
				title = HomeRowTitle(R.string.home_section_livetv),
			) {
				buildList {
					add(HomeRowItem.LiveTvAction.GUIDE)
					add(HomeRowItem.LiveTvAction.RECORDINGS)
					if (context.user.policy?.enableLiveTvManagement == true) {
						add(HomeRowItem.LiveTvAction.SCHEDULE)
						add(HomeRowItem.LiveTvAction.SERIES)
					}
				}
			},
			HomeRow.media(
				key = "${context.key}:programs",
				title = HomeRowTitle(R.string.lbl_on_now),
			) {
				api.liveTvApi.getRecommendedPrograms(
					GetRecommendedProgramsRequest(
						userId = context.user.id,
						isAiring = true,
						limit = ITEM_LIMIT,
						fields = ItemRepository.browseFields,
						imageTypeLimit = 1,
						enableTotalRecordCount = false,
					)
				).content.items
			},
		)
	}

	private companion object {
		const val ITEM_LIMIT = 20
	}
}
