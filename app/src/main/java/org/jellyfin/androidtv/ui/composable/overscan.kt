package org.jellyfin.androidtv.ui.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Default overscan values of 48 horizontal and 27 vertical display pixels.
 */
val overscanPaddingValues get() = PaddingValues(48.dp, 27.dp)

/**
 * Apply a [padding] with the default overscan values of 48 horizontal and 27 vertical display pixels.
 */
fun Modifier.overscan(): Modifier = then(padding(overscanPaddingValues))
