package org.jellyfin.androidtv.data.model

import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.core.annotation.Single
import java.time.Instant
import java.util.UUID

@Single
class DataRefreshService {
	var lastDeletedItemId: UUID? = null
	var lastPlayback: Instant? = null
	var lastMoviePlayback: Instant? = null
	var lastTvPlayback: Instant? = null
	var lastLibraryChange: Instant? = null
	var lastFavoriteUpdate: Instant? = null
	var lastPlayedItem: BaseItemDto? = null
}
