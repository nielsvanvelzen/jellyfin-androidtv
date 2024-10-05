package org.jellyfin.androidtv.ui.playback.segment

import org.jellyfin.androidtv.R
import org.jellyfin.preference.PreferenceEnum

enum class MediaSegmentAction(
	override val nameRes: Int,
) : PreferenceEnum {
	/**
	 * Don't take any action for this segment.
	 */
	NOTHING(R.string.segment_action_nothing),

	/**
	 * Seek to the end of this segment (endTicks). If the duration of this segment is shorter than 1 second it should do nothing to avoid
	 * lagg. The skip action will only execute when playing over the segment start, not when seeking into the segment block.
	 */
	SKIP(R.string.segment_action_skip),

	/**
	 * Ask the user if they want to skip this segment. When the user agrees this behaves like [SKIP]. Confirmation should only be asked for
	 * segments with a duration of at least 3 seconds to avoid UI flickering.
	 */
	ASK_TO_SKIP(R.string.segment_action_ask_to_skip),

	/**
	 * Mute the audio volume until the end of this segment. The volume level should be restored to the state when this segment started. If
	 * the volume is manually changed by the user during this segment it should no longer restore volume itself. The mute action will always
	 * execute when the segment block is active.
	 *
	 * @todo Not fully implemented.
	 */
	MUTE(R.string.segment_action_mute),
}
