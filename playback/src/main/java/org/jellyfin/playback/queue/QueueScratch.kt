package org.jellyfin.playback.queue

// This file is used to design the overall architecture of the playback module
// all code in this file should be moved to separate files when used

// Queue
interface Queue : Iterable<QueueItem>

/**
 * A queue that only contains a single item
 */
class SingleQueue(private val item: QueueItem) : Queue {
	override fun iterator() = iterator { yield(item) }
}

/*
	QUEUE
	  - INTRO Jellyfin logo
	  - TRAILER Sample trailer
	  - TRAILER Sample trailer
	  - INTRO Jellyfin logo
	  - USERMEDIA Movie
	  - INTRO Jellyfin logo
 */

// Queue Items
interface QueueItem {
	val skippable: Boolean
	val url: String
}

/**
 * Branded queue items are used for intros, trailers, advertisements and other related videos
 * It is not possible to seek or skip these items and are normally invisible when showing a queue's contents
 */
abstract class BrandedQueueItem : QueueItem {
	override val skippable = false
}

abstract class IntroQueueItem : BrandedQueueItem()
abstract class TrailerQueueItem : BrandedQueueItem()

data class UserQueueItem(
	override val url: String
) : QueueItem {
	override val skippable = true
}
