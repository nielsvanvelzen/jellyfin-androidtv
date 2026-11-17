package org.jellyfin.androidtv.data.model.home

import org.jellyfin.sdk.model.api.BaseItemDto

sealed interface HomeRowItem {
	val key: String

	data class Media(val item: BaseItemDto) : HomeRowItem {
		override val key get() = item.id.toString()
	}

	enum class LiveTvAction : HomeRowItem {
		GUIDE,
		RECORDINGS,
		SCHEDULE,
		SERIES;

		override val key get() = name
	}
}
