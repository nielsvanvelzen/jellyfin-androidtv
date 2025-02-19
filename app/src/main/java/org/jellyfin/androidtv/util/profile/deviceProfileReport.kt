package org.jellyfin.androidtv.util.profile

import android.content.Context
import android.media.MediaCodecList
import android.os.Build
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.BuildConfig
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.util.appendCodeBlock
import org.jellyfin.androidtv.util.appendItem
import org.jellyfin.androidtv.util.appendSection
import org.jellyfin.androidtv.util.appendTable
import org.jellyfin.androidtv.util.appendValue
import org.jellyfin.androidtv.util.buildMarkdown
import org.jellyfin.sdk.api.client.util.ApiSerializer

private val prettyPrintJson = Json { prettyPrint = true }
private fun formatJson(json: String) = prettyPrintJson.encodeToString(prettyPrintJson.parseToJsonElement(json))

fun createDeviceProfileReport(
	context: Context,
	userPreferences: UserPreferences,
) = buildMarkdown {
	// Header
	appendLine("---")
	appendLine("client: Jellyfin for Android TV")
	appendLine("client_version: ${BuildConfig.VERSION_NAME}")
	appendLine("client_repository: https://github.com/jellyfin/jellyfin-androidtv")
	appendLine("type: device_profile")
	appendLine("format: markdown")
	appendLine("---")

	// Content
	appendSection("Device profile") {
		appendCodeBlock(
			language = "json",
			code = createDeviceProfile(userPreferences, disableDirectPlay = false)
				.let(ApiSerializer::encodeRequestBody)
				?.let(::formatJson)
		)
	}

	appendSection("Device codecs") {
		val codecs = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
			.filter { !it.isEncoder }
			.sortedBy { it.name }

		appendTable(buildList {
			add(listOf("Name", "Codec(s)", "Vendor", "HWA"))
			add(listOf("---", "---", "---", "---"))

			for (codecInfo in codecs) {
				val isVendor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) codecInfo.isVendor.toString() else "??"
				val isHardwareAccelerated =
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) codecInfo.isHardwareAccelerated.toString() else "??"

				add(
					listOf(
						codecInfo.name,
						codecInfo.supportedTypes.joinToString(", "),
						isVendor,
						isHardwareAccelerated,
					)
				)
			}
		})

		appendSection("App information") {
			appendItem("App version") {
				appendValue(BuildConfig.VERSION_NAME)
				append(" (")
				appendValue(BuildConfig.VERSION_CODE.toString())
				append(")")
			}
			appendItem("Package name") { appendValue(context.packageName) }
		}

		appendSection("Device information") {
			appendItem("Android version") { appendValue(Build.VERSION.RELEASE) }
			appendItem("Device brand") { appendValue(Build.BRAND) }
			appendItem("Device product") { appendValue(Build.PRODUCT) }
			appendItem("Device model") { appendValue(Build.MODEL) }
		}
	}
}
