package org.jellyfin.androidtv.ui.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

sealed interface Destination : NavKey {
	data class ComposableContent(
		val content: @Composable () -> Unit,
	) : Destination

	data class Fragment(
		val fragment: KClass<out androidx.fragment.app.Fragment>,
		val arguments: Bundle = bundleOf(),
	) : Destination
}

fun composeDestination(
	content: @Composable () -> Unit,
) = Destination.ComposableContent(
	content = content,
)

inline fun <reified T : Fragment> fragmentDestination(
	vararg arguments: Pair<String, Any?>,
) = Destination.Fragment(
	fragment = T::class,
	arguments = bundleOf(*arguments),
)
