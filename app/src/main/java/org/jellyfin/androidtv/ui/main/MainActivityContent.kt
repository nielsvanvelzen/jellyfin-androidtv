package org.jellyfin.androidtv.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.navigation.ProvideRouter
import org.jellyfin.androidtv.ui.navigation.RouterContent
import org.jellyfin.androidtv.ui.screensaver.InAppScreensaver
import org.jellyfin.androidtv.ui.settings.compat.MainActivitySettings

@Composable
fun MainActivityContent() {
	val duration = 250

	val transitionIn = fadeIn(
		animationSpec = tween(duration)
	) + scaleIn(
		initialScale = 0.98f,
		animationSpec = tween(
			durationMillis = duration,
			easing = FastOutSlowInEasing
		)
	)

	val transitionOut = fadeOut(
		animationSpec = tween(duration)
	)

	val popIn = fadeIn(
		animationSpec = tween(duration)
	)

	val popOut = fadeOut(
		animationSpec = tween(duration)
	) + scaleOut(
		targetScale = 0.98f,
		animationSpec = tween(
			durationMillis = duration,
			easing = FastOutSlowInEasing
		)
	)

	AppBackground()

	ProvideRouter(routes, Routes.MAIN) {
		RouterContent(
			transitionSpec = { transitionIn togetherWith transitionOut },
			popTransitionSpec = { popIn togetherWith popOut },
		)
	}

	InAppScreensaver()
	MainActivitySettings()
}
