package org.jellyfin.playback.core

import android.media.MediaCodecList
import android.os.Build
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PlatformCodecSupport {
	fun createSupportedCodecReport(): SupportedCodecReport {
		val list = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.flatMap { codecInfo ->
			codecInfo.supportedTypes.map { supportedType ->
				// TODO: read/use $caps
				// val caps = codecInfo.getCapabilitiesForType(supportedType)

				SupportedCodecType(
					name = codecInfo.name,
					canonicalName = codecInfo.canonicalName,
					type = supportedType,
					isEncoder = codecInfo.isEncoder,
					isHardwareAccelerated = codecInfo.isHardwareAccelerated,
					isSoftwareOnly = codecInfo.isSoftwareOnly,
					isVendor = codecInfo.isVendor
				)
			}
		}

		return SupportedCodecReport(
			manufacturer = Build.MANUFACTURER,
			model = Build.MODEL,
			device = Build.DEVICE,
			display = Build.DISPLAY,
			sdk = Build.VERSION.SDK_INT,
			supportedTypes = list
		)
	}

	fun createSupportedCodecReportJson() = Json.encodeToString(createSupportedCodecReport())
}

@Serializable
data class SupportedCodecReport(
	val manufacturer: String,
	val model: String,
	val device: String,
	val sdk: Int,
	val display: String,
	val supportedTypes: List<SupportedCodecType>
)

@Serializable
data class SupportedCodecType(
	val name: String,
	val canonicalName: String,
	val type: String,
	val isEncoder: Boolean,
	val isHardwareAccelerated: Boolean,
	val isSoftwareOnly: Boolean,
	val isVendor: Boolean,
)
