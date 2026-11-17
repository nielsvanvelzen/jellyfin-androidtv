package org.jellyfin.androidtv.ui.main.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Keeps an off-screen restoration from stealing focus after another D-pad action.
 */
internal class HomeFocusRestoration {
	private var pendingRow: String? = null
	private var pendingJob: Job? = null

	fun restore(scope: CoroutineScope, row: String, block: suspend () -> Unit) {
		cancelPending()
		pendingRow = row
		pendingJob = scope.launch { block() }
	}

	fun cancelPending(row: String? = null) {
		if (row == null || row == pendingRow) {
			pendingJob?.cancel()
			pendingJob = null
			pendingRow = null
		}
	}
}
