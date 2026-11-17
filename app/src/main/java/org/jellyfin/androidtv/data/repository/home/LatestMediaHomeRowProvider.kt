package org.jellyfin.androidtv.data.repository.home

import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRowContext
import org.jellyfin.androidtv.data.model.home.HomeRowPresentation
import org.jellyfin.androidtv.data.model.home.HomeRowProvider
import org.jellyfin.androidtv.data.model.home.HomeRowTitle
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.request.GetLatestMediaRequest

class LatestMediaHomeRowProvider(private val api: ApiClient) : HomeRowProvider {
	override val sections = setOf(HomeSectionType.LATEST_MEDIA)

	override fun createRows(context: HomeRowContext) = context.libraries
		.filterNot { it.collectionType in excludedTypes || it.id in context.user.configuration?.latestItemsExcludes.orEmpty() }
		.map { library ->
			HomeRow.media(
				key = "${context.key}:${library.id}",
				title = HomeRowTitle(R.string.lbl_latest_in, listOf(library.name.orEmpty())),
				presentation = HomeRowPresentation(
					posters = library.collectionType in posterTypes,
					preferSeriesPoster = true,
				),
			) {
				api.userLibraryApi.getLatestMedia(
					GetLatestMediaRequest(
						userId = context.user.id,
						parentId = library.id,
						limit = ITEM_LIMIT,
						fields = ItemRepository.browseFields,
						imageTypeLimit = 1,
						groupItems = true,
					)
				).content
			}
		}

	private companion object {
		const val ITEM_LIMIT = 50
		val excludedTypes = setOf(CollectionType.PLAYLISTS, CollectionType.LIVETV, CollectionType.BOXSETS, CollectionType.BOOKS)
		val posterTypes = setOf(CollectionType.MOVIES, CollectionType.TVSHOWS)
	}
}
