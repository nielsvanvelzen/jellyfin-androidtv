package org.jellyfin.androidtv.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.presentation.CardViewHolderContent
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HomeRow(
	visible: Boolean,
	title: @Composable () -> Unit,
	items: @Composable (contentPadding: PaddingValues) -> Unit,
) {
	AnimatedVisibility(visible) {
		Column {
			Box(
				modifier = Modifier
					.padding(48.dp, 0.dp)
			) {
				title()
			}

			Spacer(Modifier.height(4.dp))

			items(PaddingValues(48.dp, 0.dp))
		}
	}
}

@Composable
fun HomeScreen() {
	val viewModel = koinViewModel<HomeViewModel>()
	val state by viewModel.state.collectAsState()
	val backgroundService = koinInject<BackgroundService>()
	LaunchedEffect(backgroundService) { backgroundService.clearBackgrounds() }

	Column {
		MainToolbar(MainToolbarActiveButton.Home)

		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			items(state.rows) { row ->
				HomeRow(
					visible = row.items.isNotEmpty(),
					title = {
						Text(row.title, color = Color.White, fontSize = 18.sp)
					},
					items = { contentPadding ->
						val childFocusRequester = remember { FocusRequester() }
						LazyRow(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							contentPadding = contentPadding,
							modifier = Modifier
								.focusRestorer(childFocusRequester),
						) {
							itemsIndexed(row.items) { index, item ->
								CardViewHolderContent(
									item = BaseItemDtoBaseRowItem(item),
									focused = false,
									showInfo = true,
									imageType = org.jellyfin.androidtv.constant.ImageType.POSTER,
									staticHeight = 150,
									uniformAspect = false,
								)
							}
						}
					}
				)
			}
		}
	}
}
