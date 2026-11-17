package org.jellyfin.androidtv.data.repository.home

import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRowContext
import org.jellyfin.androidtv.data.model.home.HomeRowProvider
import org.jellyfin.androidtv.data.model.home.HomeRowTitle
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.liveTvApi
import org.jellyfin.sdk.model.api.request.GetRecordingsRequest

class RecordingsHomeRowProvider(private val api: ApiClient) : HomeRowProvider {
	override val sections = setOf(HomeSectionType.ACTIVE_RECORDINGS)

	override fun createRows(context: HomeRowContext) = listOf(
		HomeRow.media(
			key = context.key,
			title = HomeRowTitle(R.string.lbl_recordings),
		) {
			api.liveTvApi.getRecordings(
				GetRecordingsRequest(
					userId = context.user.id,
					limit = ITEM_LIMIT,
					fields = ItemRepository.browseFields,
					enableImages = true,
				)
			).content.items
		})

	private companion object {
		const val ITEM_LIMIT = 40
	}
}
