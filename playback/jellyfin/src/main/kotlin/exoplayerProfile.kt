package org.jellyfin.playback.jellyfin

import org.jellyfin.sdk.model.api.CodecType
import org.jellyfin.sdk.model.api.DlnaProfileType
import org.jellyfin.sdk.model.api.EncodingContext
import org.jellyfin.sdk.model.api.ProfileConditionValue
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import org.jellyfin.sdk.model.deviceprofile.buildCodecProfile
import org.jellyfin.sdk.model.deviceprofile.buildDeviceProfile
import org.jellyfin.sdk.model.deviceprofile.buildProfileConditions
import timber.log.Timber

// H264 codec levels https://en.wikipedia.org/wiki/Advanced_Video_Coding#Levels
private const val H264_LEVEL_4_1 = "41"
private const val H264_LEVEL_5_1 = "51"
private const val H264_LEVEL_5_2 = "52"

object Codec {
	object Container {
		@Suppress("ObjectPropertyName", "ObjectPropertyNaming")
		const val `3GP` = "3gp"
		const val ASF = "asf"
		const val AVI = "avi"
		const val DVR_MS = "dvr-ms"
		const val M2V = "m2v"
		const val M4V = "m4v"
		const val MKV = "mkv"
		const val MOV = "mov"
		const val MP3 = "mp3"
		const val MP4 = "mp4"
		const val MPEG = "mpeg"
		const val MPEGTS = "mpegts"
		const val MPG = "mpg"
		const val OGM = "ogm"
		const val OGV = "ogv"
		const val TS = "ts"
		const val VOB = "vob"
		const val WEBM = "webm"
		const val WMV = "wmv"
		const val WTV = "wtv"
		const val XVID = "xvid"
	}

	object Audio {
		const val AAC = "aac"
		const val AAC_LATM = "aac_latm"
		const val AC3 = "ac3"
		const val ALAC = "alac"
		const val APE = "ape"
		const val DCA = "dca"
		const val DTS = "dts"
		const val EAC3 = "eac3"
		const val FLAC = "flac"
		const val MLP = "mlp"
		const val MP2 = "mp2"
		const val MP3 = "mp3"
		const val MPA = "mpa"
		const val OGA = "oga"
		const val OGG = "ogg"
		const val OPUS = "opus"
		const val PCM = "pcm"
		const val PCM_ALAW = "pcm_alaw"
		const val PCM_MULAW = "pcm_mulaw"
		const val PCM_S16LE = "pcm_s16le"
		const val PCM_S24LE = "pcm_s24le"
		const val TRUEHD = "truehd"
		const val WAV = "wav"
		const val WEBMA = "webma"
		const val WMA = "wma"
		const val WMAV2 = "wmav2"
	}

	object Video {
		const val H264 = "h264"
		const val HEVC = "hevc"
		const val MPEG = "mpeg"
		const val MPEG2VIDEO = "mpeg2video"
		const val VP8 = "vp8"
		const val VP9 = "vp9"
		const val AV1 = "av1"
	}

	object Subtitle {
		const val ASS = "ass"
		const val DVBSUB = "dvbsub"
		const val DVDSUB = "dvdsub"
		const val IDX = "idx"
		const val PGS = "pgs"
		const val PGSSUB = "pgssub"
		const val SMI = "smi"
		const val SRT = "srt"
		const val SSA = "ssa"
		const val SUB = "sub"
		const val SUBRIP = "subrip"
		const val VTT = "vtt"
		const val SMIL = "smil"
		const val TTML = "ttml"
		const val WEBVTT = "webvtt"
	}
}

private val downmixSupportedAudioCodecs = arrayOf(
	Codec.Audio.AAC,
	Codec.Audio.MP3,
	Codec.Audio.MP2,
)

private val allSupportedAudioCodecs = arrayOf(
	*downmixSupportedAudioCodecs,
	Codec.Audio.AAC_LATM,
	Codec.Audio.ALAC,
	Codec.Audio.DCA,
	Codec.Audio.DTS,
	Codec.Audio.MLP,
	Codec.Audio.TRUEHD,
	Codec.Audio.PCM_ALAW,
	Codec.Audio.PCM_MULAW,
)

private val ac3AudioCodecs = arrayOf(
	Codec.Audio.AC3,
	Codec.Audio.EAC3,
)

private val allSupportedAudioCodecsWithoutFFmpegExperimental = allSupportedAudioCodecs
	.filterNot { it == Codec.Audio.DCA || it == Codec.Audio.TRUEHD }
	.toTypedArray()

val deviceAV1CodecProfile = buildCodecProfile {
	type = CodecType.VIDEO
	codec = Codec.Video.AV1

//	condition(ProfileConditionValue.VIDEO_PROFILE equals "none")

	conditions {
		when {
			!MediaTest.supportsAV1() -> {
				// The following condition is a method to exclude all AV1
				Timber.i("*** Does NOT support AV1")
				ProfileConditionValue.VIDEO_PROFILE equals "none"
			}

			!MediaTest.supportsAV1Main10() -> {
				Timber.i("*** Does NOT support AV1 10 bit")
				ProfileConditionValue.VIDEO_PROFILE notEquals  "Main 10"
			}

			else -> {
				// supports all AV1
				Timber.i("*** Supports AV1 10 bit")
				ProfileConditionValue.VIDEO_PROFILE notEquals "none"
			}
		}
	}
}

val deviceHevcCodecProfile = buildCodecProfile {
	type = CodecType.VIDEO
	codec = Codec.Video.HEVC

	conditions {
		when {
			!MediaTest.supportsHevc() -> {
				// The following condition is a method to exclude all HEVC
				Timber.i("*** Does NOT support HEVC")
				ProfileConditionValue.VIDEO_PROFILE equals "none"
			}

			!MediaTest.supportsHevcMain10() -> {
				Timber.i("*** Does NOT support HEVC 10 bit")
				ProfileConditionValue.VIDEO_PROFILE notEquals "Main 10"
			}

			else -> {
				// supports all HEVC
				Timber.i("*** Supports HEVC 10 bit")
				ProfileConditionValue.VIDEO_PROFILE notEquals "none"
			}
		}
	}
}

val h264VideoLevelProfileCondition = buildProfileConditions {
	// https://developer.amazon.com/docs/fire-tv/device-specifications.html
	ProfileConditionValue.VIDEO_LEVEL lowerThanOrEquals when {
		DeviceUtils.isFireTvStick4k -> H264_LEVEL_5_2
		DeviceUtils.isFireTv -> H264_LEVEL_4_1
		DeviceUtils.isShieldTv -> H264_LEVEL_5_2
		else -> H264_LEVEL_5_1
	}
}

val h264VideoProfileCondition = buildProfileConditions {
	ProfileConditionValue.VIDEO_PROFILE inCollection buildList<String> {
		add("high")
		add("main")
		add("baseline")
		add("constrained baseline")
//		if (MediaTest.supportsAVCHigh10()) add("high 10")
	}
}

val max1080pProfileConditions = buildProfileConditions {
	ProfileConditionValue.WIDTH lowerThanOrEquals 1920
	ProfileConditionValue.HEIGHT lowerThanOrEquals 1080
}

fun ExoPlayerProfile(
	disableVideoDirectPlay: Boolean = false,
	isAC3Enabled: Boolean = false,
) = buildDeviceProfile {
	name = "AndroidTV-ExoPlayer"

	maxStreamingBitrate = 20_000_000 // 20 mbps
	maxStaticBitrate = 10_000_0000 // 10 mbps

	// TS video profile
	transcodingProfile {
		type = DlnaProfileType.VIDEO
		context = EncodingContext.STREAMING
		protocol = "hls"
		container = Codec.Container.TS

		if (deviceHevcCodecProfile.ContainsCodec(Codec.Video.HEVC, Codec.Container.TS)) videoCodec(Codec.Video.HEVC)
		videoCodec(Codec.Video.H264)

		if (Utils.downMixAudio(context)) {
			audioCodec(*downmixSupportedAudioCodecs)
		} else {
			audioCodec(*allSupportedAudioCodecsWithoutFFmpegExperimental)
			audioCodec(*ac3AudioCodecs)
		}

		copyTimestamps = false
	}

	// MP3 audio profile
	transcodingProfile {
		type = DlnaProfileType.AUDIO
		context = EncodingContext.STREAMING
		container = Codec.Container.MP3
		audioCodec(Codec.Audio.MP3)
	}

	containerProfile {
		container("mkv", "webm")
		conditions {
			ProfileConditionValue.VIDEO_CODEC_TAG inCollection setOf("h265", "h264")
		}
	}

	// Video direct play
	if (!disableVideoDirectPlay) {
		directPlayProfile {
			type = DlnaProfileType.VIDEO

			container(
				Codec.Container.M4V,
				Codec.Container.MOV,
				Codec.Container.XVID,
				Codec.Container.VOB,
				Codec.Container.MKV,
				Codec.Container.WMV,
				Codec.Container.ASF,
				Codec.Container.OGM,
				Codec.Container.OGV,
				Codec.Container.MP4,
				Codec.Container.WEBM
			)

			videoCodec(
				Codec.Video.H264,
				Codec.Video.HEVC,
				Codec.Video.VP8,
				Codec.Video.VP9,
				Codec.Video.MPEG,
				Codec.Video.MPEG2VIDEO,
				Codec.Video.AV1
			)

			if (Utils.downMixAudio(context)) {
				audioCodec(*downmixSupportedAudioCodecs)
			} else {
				audioCodec(*allSupportedAudioCodecs)
				if (isAC3Enabled) audioCodec(*ac3AudioCodecs)
			}
		}
	}

	// Audio direct play
	directPlayProfile {
		type = DlnaProfileType.AUDIO

		container(*allSupportedAudioCodecs)
		if (isAC3Enabled) container(*ac3AudioCodecs)
		container(
			Codec.Audio.MPA,
			Codec.Audio.FLAC,
			Codec.Audio.WAV,
			Codec.Audio.WMA,
			Codec.Audio.OGG,
			Codec.Audio.OGA,
			Codec.Audio.WEBMA,
			Codec.Audio.APE,
			Codec.Audio.OPUS,
		)
	}

	// H264 profile
	codecProfile {
		type = CodecType.VIDEO
		codec = Codec.Video.H264

		conditions {
			addAll(h264VideoProfileCondition)
			addAll(h264VideoLevelProfileCondition)
			if (!DeviceUtils.has4kVideoSupport()) addAll(max1080pProfileConditions)
		}
	}

	// H264 ref frames profile
	codecProfile {
		type = CodecType.VIDEO
		codec = Codec.Video.H264

		conditions {
			ProfileConditionValue.REF_FRAMES lowerThanOrEquals 12
		}

		applyConditions {
			ProfileConditionValue.WIDTH greaterThanOrEquals 1200
		}
	}

	// H264 ref frames profile
	codecProfile {
		type = CodecType.VIDEO
		codec = Codec.Video.H264

		conditions {
			ProfileConditionValue.REF_FRAMES lowerThanOrEquals 4
		}

		applyConditions {
			ProfileConditionValue.WIDTH greaterThanOrEquals 1900
		}
	}

	// HEVC profile
	add(deviceHevcCodecProfile)
	// AV1 profile
	add(deviceAV1CodecProfile)
	// Limit video resolution support for older devices
	if (!DeviceUtils.has4kVideoSupport()) {
		codecProfile {
			type = CodecType.VIDEO
			conditions {
				addAll(max1080pProfileConditions)
			}
		}
	}

	// Audio channel profile
	codecProfile {
		type = CodecType.VIDEO_AUDIO
		conditions {
			ProfileConditionValue.AUDIO_CHANNELS lowerThanOrEquals 8
		}
	}

	subtitleProfile(Codec.Subtitle.SRT, SubtitleDeliveryMethod.EXTERNAL)
	subtitleProfile(Codec.Subtitle.SUBRIP, SubtitleDeliveryMethod.EXTERNAL)
	subtitleProfile(Codec.Subtitle.ASS, SubtitleDeliveryMethod.ENCODE)
	subtitleProfile(Codec.Subtitle.SSA, SubtitleDeliveryMethod.ENCODE)
	subtitleProfile(Codec.Subtitle.PGS, SubtitleDeliveryMethod.EMBED)
	subtitleProfile(Codec.Subtitle.PGSSUB, SubtitleDeliveryMethod.EMBED)
	subtitleProfile(Codec.Subtitle.DVBSUB, SubtitleDeliveryMethod.EMBED)
	subtitleProfile(Codec.Subtitle.DVDSUB, SubtitleDeliveryMethod.ENCODE)
	subtitleProfile(Codec.Subtitle.VTT, SubtitleDeliveryMethod.EMBED)
	subtitleProfile(Codec.Subtitle.SUB, SubtitleDeliveryMethod.EMBED)
	subtitleProfile(Codec.Subtitle.IDX, SubtitleDeliveryMethod.EMBED)
}
