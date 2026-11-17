package org.jellyfin.androidtv.data.repository.home

import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRowContext
import org.jellyfin.androidtv.data.model.home.HomeRowProvider
import org.jellyfin.androidtv.data.model.home.HomeRowTitle
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaType
import org.jellyfin.sdk.model.api.request.GetResumeItemsRequest

class ResumeHomeRowProvider(private val api: ApiClient) : HomeRowProvider {
	override val sections = setOf(HomeSectionType.RESUME, HomeSectionType.RESUME_AUDIO)

	override fun createRows(context: HomeRowContext): List<HomeRow> {
		val audio = context.section == HomeSectionType.RESUME_AUDIO
		return listOf(
			HomeRow.media(
				key = context.key,
				title = HomeRowTitle(if (audio) R.string.continue_listening else R.string.lbl_continue_watching),
			) {
				api.itemsApi.getResumeItems(
					GetResumeItemsRequest(
						userId = context.user.id,
						limit = ITEM_LIMIT,
						fields = ItemRepository.browseFields,
						imageTypeLimit = 1,
						enableTotalRecordCount = false,
						mediaTypes = listOf(if (audio) MediaType.AUDIO else MediaType.VIDEO),
						excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
					)
				).content.items
			})
	}

	private companion object {
		const val ITEM_LIMIT = 50
	}
}
