package org.jellyfin.androidtv.ui.main.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.main.composable.Navigation
import org.jellyfin.androidtv.ui.main.composable.NavigationLayout
import org.jellyfin.androidtv.ui.navigation.focus.LocalFocusPersistManager
import org.jellyfin.androidtv.ui.navigation.focus.focusKey
import org.jellyfin.androidtv.ui.shared.toolbar.ToolbarClock
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun HomeScreen() {
	val viewModel = koinViewModel<HomeScreenViewModel>()
	val focusRestoration = remember { HomeFocusRestoration() }
	val uiState by viewModel.uiState.collectAsStateWithLifecycle()
	val serverRepository = koinInject<ServerRepository>()
	val backgroundService = koinInject<BackgroundService>()
	val currentServer by serverRepository.currentServer.collectAsStateWithLifecycle()

	LaunchedEffect(backgroundService, currentServer) {
		if (currentServer != null) backgroundService.setBackground(currentServer!!)
		else backgroundService.clearBackgrounds()
	}

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshRows() }

	AppBackground()

	NavigationLayout(
		navigation = {
			Navigation(initialFocus = (uiState as? HomeScreenUiState.Content)?.isEmpty == true)
		},
		modifier = Modifier
			.onPreviewKeyEvent {
				if (it.type == KeyEventType.KeyDown) focusRestoration.cancelPending()
				false
			},
	) {
		CompositionLocalProvider(LocalBringIntoViewSpec provides CenteredHomeBringIntoViewSpec) {
			HomeContent(
				uiState = uiState,
				onRetry = viewModel::retry,
				onRetryRow = viewModel::retryRow,
				focusRestoration = focusRestoration,
			)
		}
	}
}

private object CenteredHomeBringIntoViewSpec : BringIntoViewSpec {
	// Match Leanback's default alignment: the item's center follows the viewport's
	// center on both axes. The lazy lists clamp scrolling at their content edges.
	override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float =
		offset + size / 2f - containerSize / 2f
}

@Composable
private fun HomeContent(
	uiState: HomeScreenUiState,
	onRetry: () -> Unit,
	onRetryRow: (String) -> Unit,
	focusRestoration: HomeFocusRestoration,
) {
	val focusManager = LocalFocusPersistManager.current
	val content = uiState as? HomeScreenUiState.Content
	val visibleRows = content?.visibleRows.orEmpty()
	val initialRowKey = visibleRows.firstOrNull()?.key
	LaunchedEffect(uiState) {
		val key = focusManager.save() ?: return@LaunchedEffect
		when (uiState) {
			HomeScreenUiState.Loading -> Unit
			HomeScreenUiState.Error -> {
				if (key == "retry") focusManager.restore("retry")
			}

			is HomeScreenUiState.Content -> {
				// A missing key is only obsolete after all rows have finished loading.
				if (!uiState.isLoadingRows && !uiState.hasFocusKey(key)) {
					focusManager.restore(uiState.fallbackFocusKey(key))
				}
			}
		}
	}
	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.focusRestorer(),
		contentPadding = PaddingValues(vertical = 16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		item(key = "header") {
			Row(Modifier.padding(horizontal = 24.dp)) {
				Text(
					text = stringResource(R.string.lbl_home),
					color = JellyfinTheme.colorScheme.listHeader,
					style = JellyfinTheme.typography.listHeadline,
				)
				Spacer(Modifier.weight(1f))
				ToolbarClock()
			}
		}
		when (uiState) {
			HomeScreenUiState.Loading -> item(key = "loading") { Text(stringResource(R.string.loading)) }
			HomeScreenUiState.Error -> item(key = "error") {
				Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
					Text(stringResource(R.string.home_rows_load_error))
					Button(
						onClick = onRetry,
						modifier = Modifier.focusKey("retry", initialFocus = true),
					) { Text(stringResource(R.string.home_rows_retry)) }
				}
			}

			is HomeScreenUiState.Content -> {
				if (visibleRows.isEmpty()) {
					item(key = "empty") { Text(stringResource(R.string.lbl_no_items), modifier = Modifier.padding(horizontal = 24.dp)) }
				}
				items(visibleRows, key = { it.key }) { rowState ->
					val row = rowState.row
					Column(
						modifier = Modifier
							.focusKey(row.key)
							.focusGroup(),
						verticalArrangement = Arrangement.spacedBy(8.dp),
					) {
						Text(
							text = homeRowTitle(row),
							color = JellyfinTheme.colorScheme.listHeader,
							style = JellyfinTheme.typography.listHeader,
							modifier = Modifier.padding(horizontal = 24.dp),
						)
						when (rowState) {
							is HomeRowUiState.Loading -> Text(
								stringResource(R.string.loading),
								modifier = Modifier.padding(horizontal = 24.dp)
							)

							is HomeRowUiState.Error -> Row(
								modifier = Modifier.padding(horizontal = 24.dp),
								horizontalArrangement = Arrangement.spacedBy(16.dp),
							) {
								Text(stringResource(R.string.home_row_items_load_error))
								Button(
									onClick = { onRetryRow(row.key) },
									modifier = Modifier.focusKey("retry", initialFocus = row.key == initialRowKey),
								) { Text(stringResource(R.string.home_rows_retry)) }
							}

							is HomeRowUiState.Content -> HomeMediaRow(
								row,
								rowState.items,
								focusRestoration,
								initialFocus = row.key == initialRowKey
							)
						}
					}
				}
			}
		}
	}
}

private fun HomeScreenUiState.Content.hasFocusKey(key: String): Boolean =
	key in navigationFocusKeys || libraryIds.any { key == "navigation.library_$it" } || visibleRows.any { row ->
		when (row) {
			is HomeRowUiState.Content -> key == row.key || row.items.any { key == "${row.key}.${it.key}" }
			is HomeRowUiState.Error -> key == row.key || key == "${row.key}.retry"
			is HomeRowUiState.Loading -> false
		}
	}

private fun HomeScreenUiState.Content.fallbackFocusKey(previousKey: String): String {
	val rows = visibleRows.filterNot { it is HomeRowUiState.Loading }
	val row = rows.find { previousKey == it.key || previousKey.startsWith("${it.key}.") }
		?: rows.firstOrNull()
		?: return "navigation.home"
	return when (row) {
		is HomeRowUiState.Content -> "${row.key}.${row.items.first().key}"
		else -> "${row.key}.retry"
	}
}

private val navigationFocusKeys = setOf("navigation.home", "navigation.search", "navigation.settings")

@Composable
private fun homeRowTitle(row: HomeRow): String = stringResource(row.title.resourceId, *row.title.arguments.toTypedArray())
