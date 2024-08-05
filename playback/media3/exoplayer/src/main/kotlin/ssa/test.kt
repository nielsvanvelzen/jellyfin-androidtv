package org.jellyfin.playback.media3.exoplayer.ssa

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.text.Cue
import androidx.media3.common.util.Consumer
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.text.CuesWithTiming
import androidx.media3.extractor.text.DefaultSubtitleParserFactory
import androidx.media3.extractor.text.SubtitleParser

@UnstableApi
class ExtendedSubtitleParserFactory : SubtitleParser.Factory {
	private val parent = DefaultSubtitleParserFactory()

	override fun supportsFormat(format: Format): Boolean {
		if (format.sampleMimeType == MimeTypes.TEXT_SSA) return true

		return parent.supportsFormat(format)
	}

	override fun getCueReplacementBehavior(format: Format): Int {
		if (format.sampleMimeType == MimeTypes.TEXT_SSA) return Format.CUE_REPLACEMENT_BEHAVIOR_MERGE

		return parent.getCueReplacementBehavior(format)
	}

	override fun create(format: Format): SubtitleParser {
		if (format.sampleMimeType == MimeTypes.TEXT_SSA) return AssSubtitleParser(format)

		return parent.create(format)
	}
}

@UnstableApi
class AssSubtitleParser(
	private val format: Format,
) : SubtitleParser {
	override fun getCueReplacementBehavior(): Int = Format.CUE_REPLACEMENT_BEHAVIOR_MERGE

	private val header by lazy {
		format.initializationData
			.joinToString("\n") {
				it.decodeToString()
			}
	}

	private var a = false

	override fun parse(
		data: ByteArray,
		offset: Int,
		length: Int,
		outputOptions: SubtitleParser.OutputOptions,
		output: Consumer<CuesWithTiming>
	) {
		val cues = mutableListOf<Cue>()
		if (!a) {
			for (line in header.lines()) {
				cues.add(Cue.Builder().setText(line).build())
			}
			a = true
		}
		cues.add(Cue.Builder().setText(data.decodeToString()).build())

		output.accept(CuesWithTiming(cues,0, 10_000_000))
	}
}
