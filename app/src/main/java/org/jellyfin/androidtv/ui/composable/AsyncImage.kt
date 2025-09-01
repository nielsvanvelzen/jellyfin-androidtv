package org.jellyfin.androidtv.ui.composable

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.util.BlurHashDecoder
import kotlin.math.round

@Composable
fun AsyncImage(
	modifier: Modifier = Modifier,
	url: String? = null,
	blurHash: String? = null,
	placeholder: Painter? = null,
	aspectRatio: Float = 1f,
	blurHashResolution: Int = 32,
	contentScale: ContentScale = ContentScale.Fit,
) {
	// TODO: Skip blurhash if not loading from network (disk cache)
	val blurHashPainter = if (blurHash != null) {
		val width = if (aspectRatio > 1) round(blurHashResolution * aspectRatio).toInt() else blurHashResolution
		val height = if (aspectRatio >= 1) blurHashResolution else round(blurHashResolution / aspectRatio).toInt()

		blurHashPainter(blurHash, IntSize(width, height), 1f)
	} else null

	val imagePainter = rememberAsyncImagePainter(
		model = url,
		contentScale = contentScale,
	)
	val imagePainterState by imagePainter.state.collectAsState()
	val transparentPainter = remember { ColorPainter(Color.Transparent) }

	val visiblePainter = when (imagePainterState) {
		is AsyncImagePainter.State.Success -> imagePainter
		is AsyncImagePainter.State.Loading -> blurHashPainter ?: placeholder ?: transparentPainter
		else -> placeholder ?: blurHashPainter ?: transparentPainter
	}

	Crossfade(visiblePainter) { painter ->
		Image(
			painter = painter,
			contentDescription = null,
			modifier = modifier
				.fillMaxSize(),
			contentScale = contentScale,
		)
	}
}

@Composable
@Stable
fun blurHashPainter(
	blurHash: String,
	size: IntSize,
	punch: Float = 1f,
	fallback: Painter = ColorPainter(Color.Transparent),
): Painter {
	val painter by produceState(initialValue = fallback, blurHash, size, punch) {
		val bitmap = withContext(Dispatchers.IO) {
			BlurHashDecoder.decode(
				blurHash = blurHash,
				width = size.width,
				height = size.height,
				punch = punch,
			)
		}?.asImageBitmap()

		if (bitmap != null) value = BitmapPainter(bitmap)
	}
	return painter
}
