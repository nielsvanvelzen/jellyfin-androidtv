package org.jellyfin.androidtv.ui.quickswitch

import androidx.compose.runtime.Stable
import org.jellyfin.androidtv.auth.model.PrivateUser
import org.jellyfin.androidtv.auth.model.Server

@Stable
data class QuickSwitchState(
	val recentUsers: List<PrivateUser>,
	val currentServer: Server,
)
