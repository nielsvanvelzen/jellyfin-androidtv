package org.jellyfin.androidtv.ui.browsing2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.constant.QueryType
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.composable.item.ItemCard
import org.jellyfin.androidtv.ui.composable.item.ItemPreview
import org.jellyfin.androidtv.ui.composable.modifier.overscanPaddingValues
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.itemhandling.ItemRowAdapter
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.presentation.CardPresenter
import org.jellyfin.androidtv.ui.presentation.MutableObjectAdapter
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.serializer.toUUIDOrNull
import org.koin.compose.koinInject
import java.util.UUID

@Composable
fun BrowserScreenItem(
	item: BaseItemDto,
	onOpen: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val api = koinInject<ApiClient>()
	val backgroundService = koinInject<BackgroundService>()
	val interactionSource = remember { MutableInteractionSource() }
	val focused by interactionSource.collectIsFocusedAsState()

	LaunchedEffect(focused) {
		if (focused) backgroundService.setBackground(item)
	}

	val imageTag = item.imageTags?.get(ImageType.PRIMARY)
	val imageBlurHash = item.imageBlurHashes?.get(ImageType.PRIMARY)?.get(imageTag)

	ItemPreview(
		card = {
			ItemCard(
				image = {
					AsyncImage(
						url = api.imageApi.getItemImageUrl(item.id, ImageType.PRIMARY, tag = imageTag, fillWidth = 173, fillHeight = 258),
						blurHash = imageBlurHash,
						scaleType = ImageView.ScaleType.CENTER_CROP,
						modifier = Modifier.fillMaxSize()
					)
				},
				modifier = modifier,
			)
		},
		title = {
			Text(
				text = item.name.orEmpty(),
				maxLines = 1,
			)
		},
		modifier = Modifier
			.focusable()
			.clickable(onClick = onOpen)
	)
}

@Composable
fun BrowserFolder(
	item: BaseItemDto,
	modifier: Modifier = Modifier,
) {
	val api = koinInject<ApiClient>()
	val navigationRepository = koinInject<NavigationRepository>()
	val itemLauncher = koinInject<ItemLauncher>()
	val context = LocalContext.current

	var items by remember { mutableStateOf<List<BaseItemDto>>(emptyList()) }

	LaunchedEffect(item) {
		withContext(Dispatchers.IO) {
			items = try {
				val itemsResult by api.itemsApi.getItems(
					parentId = item.id,
					fields = setOf(ItemFields.DISPLAY_PREFERENCES_ID),
					limit = 100,
					startIndex = 0,
				)

				itemsResult.items
			} catch (err: ApiClientException) {
				emptyList()
			}
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.focusGroup()
			.then(modifier)
	) {
		Text(
			item.run { "$name ($type / $collectionType / $id)" },
			fontSize = 22.sp,
			color = Color.White
		)

		val aspectRatio = when (item.type) {
			BaseItemKind.USER_VIEW -> 384f / 216f
			BaseItemKind.COLLECTION_FOLDER -> when (item.collectionType) {
				CollectionType.MOVIES,
				CollectionType.TVSHOWS -> 2f / 3f

				else -> 1f
			}

			BaseItemKind.FOLDER,
			BaseItemKind.MOVIE,
			BaseItemKind.SERIES,
			BaseItemKind.SEASON -> 2f / 3f

			else -> 1f
		}

		LazyVerticalGrid(
			columns = GridCells.Adaptive(minSize = 100.dp),
			horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
			verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
			contentPadding = overscanPaddingValues,
		) {
			items(
				items = items,
				key = { item -> item.id },
				contentType = { item -> item.type },
			) { child ->
				BrowserScreenItem(
					item = child,
					modifier = Modifier.
						heightIn(100.dp, 200.dp)
						.aspectRatio(aspectRatio),
					onOpen = {
						if (child.isFolder == true || child.collectionType != null || child.type == BaseItemKind.FOLDER) {
							navigationRepository.navigate(Destinations.browser(child.id))
						} else {
							// Please kill me
							val launchItem = child.copy(
								displayPreferencesId = child.displayPreferencesId
									?: child.id.toString()
							)
							itemLauncher.launch(
								BaseItemDtoBaseRowItem(launchItem),
								ItemRowAdapter(
									context,
									listOf(launchItem),
									CardPresenter(),
									MutableObjectAdapter(),
									QueryType.StaticItems
								),
								context,
							)
						}
					},
				)
			}
		}
	}
}

@Composable
fun BrowserScreen(itemId: UUID?, modifier: Modifier = Modifier) {
	val api = koinInject<ApiClient>()

	var item by remember { mutableStateOf<BaseItemDto?>(null) }

	LaunchedEffect(itemId) {
		item = if (itemId == null) {
			null
		} else try {
			val itemResult by api.userLibraryApi.getItem(
				itemId = itemId,
			)

			itemResult
		} catch (err: ApiClientException) {
			null
		}
	}

	if (item != null) {
		BrowserFolder(item!!)
	} else {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.focusGroup()
				.then(modifier)
		) {
			Text(
				"Loading $itemId",
				fontSize = 22.sp,
				color = Color.White
			)
		}
	}
}

class BrowserFragment : Fragment() {
	companion object {
		const val EXTRA_ID = "id"
	}

	private val itemId = MutableStateFlow<UUID?>(null)

	override fun setArguments(args: Bundle?) {
		super.setArguments(args)

		itemId.value = args?.getString(EXTRA_ID)?.toUUIDOrNull()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	) = content {
		val itemIdState by itemId.collectAsState()
		BrowserScreen(itemId = itemIdState)
	}
}
