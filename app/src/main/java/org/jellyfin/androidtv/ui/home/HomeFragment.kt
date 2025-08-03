package org.jellyfin.androidtv.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.repository.NotificationsRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.databinding.FragmentHomeBinding
import org.jellyfin.androidtv.ui.base.Badge
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

sealed interface ItemCardSize {
	data class Width(val width: Dp) : ItemCardSize
	data class Height(val height: Dp) : ItemCardSize
}

sealed interface ItemCardImageType {
	data object Primary : ItemCardImageType
	data object Thumbnail : ItemCardImageType
}

@Composable
fun ItemCardOverlay(item: BaseItemDto) = Box(
	modifier = Modifier
		.fillMaxSize()
		.padding(4.dp)
) {
	Badge(
		modifier = Modifier
			.align(Alignment.TopEnd),
	) {
		if (item.userData?.played == true) {
			Icon(
				imageVector = ImageVector.vectorResource(R.drawable.ic_watch),
				contentDescription = null,
				modifier = Modifier.size(12.dp)
			)
		} else if (item.userData?.unplayedItemCount?.takeIf { it > 0 } != null) {
			Text(
				text = item.userData!!.unplayedItemCount.toString(),
			)
		}
	}
}

@Stable
@Composable
fun ItemCard(
	item: BaseItemDto,
	modifier: Modifier = Modifier,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	size: ItemCardSize = ItemCardSize.Height(150.dp),
	imageType: ItemCardImageType = ItemCardImageType.Primary,
	shape: Shape = JellyfinTheme.shapes.medium,
	overlay: @Composable (item: BaseItemDto) -> Unit = { item -> ItemCardOverlay(item) },
) {
	val api = koinInject<ApiClient>()
	val image = when (imageType) {
		ItemCardImageType.Primary -> item.itemImages[ImageType.PRIMARY]
		ItemCardImageType.Thumbnail -> item.itemImages[ImageType.THUMB]
	}
	// TODO: We should NOT infer this from the actual image but have a hardcoded list (probably based on item type) instead
	val imageAspectRatio = image?.aspectRatio ?: 1f
	val imageSize = when (size) {
		is ItemCardSize.Height -> DpSize(size.height * imageAspectRatio, size.height)
		is ItemCardSize.Width -> DpSize(size.width, size.width / imageAspectRatio)
	}

	val focused by interactionSource.collectIsFocusedAsState()

	Column(
		modifier = modifier
			.requiredWidth(imageSize.width)
			.focusable(interactionSource = interactionSource)
	) {
		Box(
			modifier = Modifier
				.size(imageSize)
				.clip(shape)
		) {
			if (image != null) {
				AsyncImage(
					url = image.getUrl(api),
					blurHash = image.blurHash,
					aspectRatio = image.aspectRatio ?: 1f,
					scaleType = ImageView.ScaleType.CENTER_CROP,
					modifier = Modifier
						.fillMaxSize()
						.then(if (focused) Modifier.border(5.dp, Color.Red, shape) else Modifier)
				)
			}

			Box(modifier = Modifier.fillMaxSize()) {
				overlay(item)
			}
		}

		Column(
			modifier = Modifier.padding(4.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Text(
				text = item.name.orEmpty(),
				fontSize = 12.sp,
				fontWeight = FontWeight.SemiBold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				color = Color.White,
				modifier = Modifier
					.basicMarquee(
						iterations = if (focused) Int.MAX_VALUE else 0,
						initialDelayMillis = 0,
					),
			)

			Text(
				text = item.path.orEmpty(),
				fontSize = 10.sp,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				color = Color.White,
				modifier = Modifier
					.basicMarquee(
						iterations = if (focused) Int.MAX_VALUE else 0,
						initialDelayMillis = 0,
					),
			)
		}
	}
}

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
								ItemCard(
									item = item,
									modifier = Modifier
										.then(if (index == 0) Modifier.focusRequester(childFocusRequester) else Modifier)
								)
							}
						}
					}
				)
			}
		}
	}
}

class HomeFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? = content {
		HomeScreen()
	}
}

class HomeFragment2 : Fragment() {
	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!

	private val sessionRepository by inject<SessionRepository>()
	private val serverRepository by inject<ServerRepository>()
	private val notificationRepository by inject<NotificationsRepository>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		_binding = FragmentHomeBinding.inflate(inflater, container, false)

		binding.toolbar.setContent {
			MainToolbar(MainToolbarActiveButton.Home)
		}

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		sessionRepository.currentSession
			.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
			.map { session ->
				if (session == null) null
				else serverRepository.getServer(session.serverId)
			}
			.onEach { server ->
				notificationRepository.updateServerNotifications(server)
			}
			.launchIn(viewLifecycleOwner.lifecycleScope)
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}
}
