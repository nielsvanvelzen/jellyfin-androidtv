package org.jellyfin.playback.core.element

import org.jellyfin.playback.core.queue.QueueEntry

private val removedKey = ElementKey<Boolean>("Removed")

/**
 * Get or set the removed state for this [QueueEntry]. This can be used to hide (remove) entries while still preserving them in the queue.
 * Removing is normally done using the [org.jellyfin.playback.core.queue.QueueService].
 */
var QueueEntry.removed by requiredElement(removedKey) { false }

/**
 * Get the flow of [removed].
 * @see removed
 */
val QueueEntry.removedFlow by elementFlow(removedKey)
