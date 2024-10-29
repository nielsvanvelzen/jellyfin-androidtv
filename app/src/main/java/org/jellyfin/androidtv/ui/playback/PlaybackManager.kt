package org.jellyfin.androidtv.ui.playback

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.data.compat.PlaybackException
import org.jellyfin.androidtv.data.compat.StreamInfo
import org.jellyfin.androidtv.data.compat.VideoOptions
import org.jellyfin.apiclient.interaction.Response
import org.jellyfin.apiclient.model.session.PlayMethod
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.hlsSegmentApi
import org.jellyfin.sdk.api.client.extensions.mediaInfoApi
import org.jellyfin.sdk.model.api.PlaybackInfoDto
import org.jellyfin.sdk.model.api.PlaybackInfoResponse
import org.jellyfin.sdk.model.deviceprofile.buildDeviceProfile

private fun createStreamInfo(
	api: ApiClient,
	options: VideoOptions,
	response: PlaybackInfoResponse,
): StreamInfo = StreamInfo().apply {
	val source = response.mediaSources.firstOrNull {
		options.mediaSourceId != null && it.id == options.mediaSourceId
	} ?: response.mediaSources.firstOrNull()

	itemId = options.itemId
	mediaSource = source
	runTimeTicks = source?.runTimeTicks
	deviceProfile = options.profile
	playSessionId = response.playSessionId

	if (source == null) return@apply

	if (options.enableDirectPlay && source.supportsDirectPlay) {
		// TODO: Test this branch
		playMethod = PlayMethod.DirectPlay
		container = source.container
		mediaUrl = source.path
	} else if (options.enableDirectStream && source.supportsDirectStream) {
		// TODO: Test this branch
		playMethod = PlayMethod.DirectPlay
		container = source.container
		mediaUrl = source.path
	} else if (source.supportsTranscoding) {
		playMethod = PlayMethod.Transcode
		container = source.transcodingContainer
		mediaUrl = api.createUrl(requireNotNull(source.transcodingUrl), ignorePathParameters = true)
	}
}

class PlaybackManager(
	private val api: ApiClient
) {
	fun getVideoStreamInfo(
		lifecycleOwner: LifecycleOwner,
		options: VideoOptions,
		startTimeTicks: Long,
		callback: Response<StreamInfo>,
	) = lifecycleOwner.lifecycleScope.launch {
		getVideoStreamInfoInternal(options, startTimeTicks).fold(
			onSuccess = { callback.onResponse(it) },
			onFailure = { callback.onError(Exception(it)) },
		)
	}

	fun changeVideoStream(
		lifecycleOwner: LifecycleOwner,
		stream: StreamInfo,
		options: VideoOptions,
		startTimeTicks: Long,
		callback: Response<StreamInfo>
	) = lifecycleOwner.lifecycleScope.launch {
		if (stream.playSessionId != null && stream.playMethod != PlayMethod.DirectPlay) {
			api.hlsSegmentApi.stopEncodingProcess(api.deviceInfo.id, stream.playSessionId)
		}

		getVideoStreamInfoInternal(options, startTimeTicks).fold(
			onSuccess = { callback.onResponse(it) },
			onFailure = { callback.onError(Exception(it)) },
		)
	}

	private suspend fun getVideoStreamInfoInternal(
		options: VideoOptions,
		startTimeTicks: Long
	) = runCatching {
		val response by api.mediaInfoApi.getPostedPlaybackInfo(
			itemId = requireNotNull(options.itemId) { "Item id cannot be null" },
			data = PlaybackInfoDto(
				maxStreamingBitrate = options.maxBitrate,
				mediaSourceId = options.mediaSourceId,
				startTimeTicks = startTimeTicks,
				deviceProfile = buildDeviceProfile {
					// TODO Use options.profile instead
				},
				enableDirectStream = options.enableDirectStream,
				enableDirectPlay = options.enableDirectPlay,
				maxAudioChannels = options.maxAudioChannels,
				audioStreamIndex = options.audioStreamIndex.takeIf { it != null && it >= 0 },
				subtitleStreamIndex = options.subtitleStreamIndex,
				allowVideoStreamCopy = true,
				allowAudioStreamCopy = true,
				autoOpenLiveStream = true,
			)
		)

		if (response.errorCode != null) {
			throw PlaybackException().apply {
				errorCode = response.errorCode!!
			}
		}

		createStreamInfo(api, options, response)
	}
}
