package org.jellyfin.androidtv.ui.main.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.first
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.ImageType
import org.jellyfin.androidtv.data.model.home.HomeRow
import org.jellyfin.androidtv.data.model.home.HomeRowItem
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.base.button.ButtonBase
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.composable.item.ItemCard
import org.jellyfin.androidtv.ui.composable.item.ItemCardBaseItemOverlay
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.main.composable.libraryIcon
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.navigation.focus.LocalFocusPersistManager
import org.jellyfin.androidtv.ui.navigation.focus.focusKey
import org.jellyfin.androidtv.ui.presentation.MutableObjectAdapter
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemKind
import org.koin.compose.koinInject

@Composable
internal fun HomeMediaRow(
	row: HomeRow,
	items: List<HomeRowItem>,
	focusRestoration: HomeFocusRestoration,
	modifier: Modifier = Modifier,
	initialFocus: Boolean = false,
) {
	val itemLauncher = koinInject<ItemLauncher>()
	val navigation = koinInject<NavigationRepository>()
	val backgroundService = koinInject<BackgroundService>()
	val context = LocalContext.current
	var selectedItemKey by rememberSaveable(row.key) { mutableStateOf<String?>(null) }
	val focusManager = LocalFocusPersistManager.current
	val restoredItemKey = focusManager.pendingRestorationKey
		?.takeIf { it.startsWith("${row.key}.") }?.removePrefix("${row.key}.")
	val targetItemKey = restoredItemKey ?: selectedItemKey
	val selectedIndex = items.indexOfFirst { it.key == targetItemKey }.coerceAtLeast(0)
	val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
	LaunchedEffect(restoredItemKey, items) {
		val index = items.indexOfFirst { it.key == restoredItemKey }
		if (index >= 0) listState.scrollToItem(index)
	}
	val requesters = remember { mutableMapOf<String, FocusRequester>() }
	val placedItems = remember { mutableStateMapOf<String, Boolean>() }
	val scope = rememberCoroutineScope()
	var restoringFocus by remember { mutableStateOf(false) }
	DisposableEffect(items.map { it.key }, focusRestoration) {
		requesters.keys.retainAll(items.map { it.key }.toSet())
		onDispose { focusRestoration.cancelPending(row.key) }
	}
	fun requestItemFocus(requester: FocusRequester) {
		restoringFocus = true
		try {
			requester.requestFocus()
		} finally {
			restoringFocus = false
		}
	}

	val media = remember(items, row.presentation) {
		items.filterIsInstance<HomeRowItem.Media>().associate { entry ->
			entry.key to BaseItemDtoBaseRowItem(entry.item, preferSeriesPoster = row.presentation.preferSeriesPoster)
		}
	}
	// The existing launcher uses the row to build audio queues.
	val adapter = remember(media) { MutableObjectAdapter<Any>().apply { media.values.forEach(::add) } }
	LazyRow(
		state = listState,
		modifier = modifier
			.fillMaxWidth()
			.focusProperties {
				onEnter = {
					if (!restoringFocus && items.isNotEmpty()) {
						val index = items.indexOfFirst { it.key == targetItemKey }.coerceAtLeast(0)
						val key = items[index].key
						val requester = requesters.getOrPut(key) { FocusRequester() }
						focusRestoration.cancelPending()
						if (placedItems[key] == true) {
							requestItemFocus(requester)
						} else {
							// A saved selection can be outside the lazy row's composed range.
							// Keep focus in the source row until its target has been placed.
							cancelFocusChange()
							focusRestoration.restore(scope, row.key) {
								listState.scrollToItem(index)
								snapshotFlow { placedItems[key] == true }.first { it }
								requestItemFocus(requester)
							}
						}
					}
				}
			},
		contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
	) {
		itemsIndexed(items, key = { _, entry -> entry.key }) { index, entry ->
			val requester = remember(entry.key) { requesters.getOrPut(entry.key) { FocusRequester() } }
			DisposableEffect(entry.key) {
				onDispose { placedItems.remove(entry.key) }
			}
			val itemModifier = Modifier
				.focusKey(entry.key, focusRequester = requester, initialFocus = initialFocus && index == 0)
				.onPlaced { placedItems[entry.key] = true }
				.onFocusChanged { if (it.isFocused) selectedItemKey = entry.key }
			when (entry) {
				is HomeRowItem.Media -> {
					val item = media.getValue(entry.key)
					val onClick = {
						if (entry.item.type == BaseItemKind.PHOTO) {
							navigation.navigate(Destinations.photoPlayer(entry.item.id, false, null, null))
						} else itemLauncher.launch(item, adapter, context)
					}
					if (row.presentation.buttons) {
						Button(onClick = onClick, modifier = itemModifier) {
							Icon(painterResource(entry.item.collectionType.libraryIcon), contentDescription = null)
							Spacer(Modifier.width(8.dp))
							Text(entry.item.name.orEmpty())
						}
					} else {
						HomeMediaCard(row, item, onClick, itemModifier) { backgroundService.setBackground(entry.item) }
					}
				}

				is HomeRowItem.LiveTvAction -> HomeLiveTvButton(entry, itemModifier)
			}
		}
	}
}

@Composable
private fun HomeLiveTvButton(action: HomeRowItem.LiveTvAction, modifier: Modifier = Modifier) {
	val navigation = koinInject<NavigationRepository>()
	val destination = when (action) {
		HomeRowItem.LiveTvAction.GUIDE -> Destinations.liveTvGuide
		HomeRowItem.LiveTvAction.RECORDINGS -> Destinations.liveTvRecordings
		HomeRowItem.LiveTvAction.SCHEDULE -> Destinations.liveTvSchedule
		HomeRowItem.LiveTvAction.SERIES -> Destinations.liveTvSeriesRecordings
	}
	val label = when (action) {
		HomeRowItem.LiveTvAction.GUIDE -> R.string.lbl_live_tv_guide
		HomeRowItem.LiveTvAction.RECORDINGS -> R.string.lbl_recorded_tv
		HomeRowItem.LiveTvAction.SCHEDULE -> R.string.lbl_schedule
		HomeRowItem.LiveTvAction.SERIES -> R.string.lbl_series
	}
	Button(onClick = { navigation.navigate(destination) }, modifier = modifier) {
		Text(stringResource(label))
	}
}

@Composable
private fun HomeMediaCard(
	row: HomeRow,
	item: BaseItemDtoBaseRowItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onFocus: () -> Unit,
) {
	val api = koinInject<ApiClient>()
	val context = LocalContext.current
	val baseItem = requireNotNull(item.baseItem)
	val poster = row.presentation.posters
	val square = baseItem.type in squareItemTypes
	val width = if (poster) 112.dp else if (square) 144.dp else 200.dp
	val height = if (poster) 168.dp else if (square) 144.dp else 112.dp
	val image = remember(item, poster, square) { item.getImage(if (poster || square) ImageType.POSTER else ImageType.THUMB) }
	val imageUrl = remember(image, api) { image?.getUrl(api, maxWidth = 600) }
	var focused by remember { mutableStateOf(false) }
	val shape = JellyfinTheme.shapes.medium
	val title = item.getFullName(context).orEmpty()
	Column(modifier = Modifier.width(width), verticalArrangement = Arrangement.spacedBy(6.dp)) {
		ButtonBase(
			onClick = onClick,
			shape = shape,
			modifier = modifier
				.size(width, height)
				.semantics { contentDescription = title }
				.border(2.dp, if (focused) JellyfinTheme.colorScheme.onBackground else Color.Transparent, shape)
				.onFocusChanged {
					focused = it.isFocused
					if (it.isFocused) onFocus()
				},
			colors = ButtonDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
		) {
			ItemCard(
				modifier = Modifier
					.fillMaxSize()
					.padding(2.dp),
				image = {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						Icon(painterResource(baseItem.collectionType.libraryIcon), null, modifier = Modifier.size(40.dp))
						AsyncImage(
							model = imageUrl,
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier.fillMaxSize()
						)
					}
				},
				overlay = { ItemCardBaseItemOverlay(baseItem) },
			)
		}
		Text(
			text = title,
			color = JellyfinTheme.colorScheme.listHeader,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.height(40.dp),
		)
	}
}

private val squareItemTypes = setOf(BaseItemKind.AUDIO, BaseItemKind.MUSIC_ALBUM, BaseItemKind.MUSIC_ARTIST)
