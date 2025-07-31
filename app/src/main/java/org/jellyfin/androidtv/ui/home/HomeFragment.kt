package org.jellyfin.androidtv.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.repository.NotificationsRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.databinding.FragmentHomeBinding
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

@Composable
private fun ItemCardTitle(item: BaseItemDto) {
	Text(
		text = item.name.orEmpty(),
		fontSize = 12.sp,
		maxLines = 3,
		overflow = TextOverflow.Ellipsis,
		color = Color.White,
	)
}

@Composable
private fun ItemCardSubtitle(item: BaseItemDto) {
	Text(
		text = item.path.orEmpty(),
		fontSize = 12.sp,
		maxLines = 3,
		overflow = TextOverflow.Ellipsis,
		color = Color.White,
	)
}

@Composable
private fun ItemCardImage(item: BaseItemDto) {
	val image = item.itemImages[ImageType.PRIMARY]

	if (image != null) {
		val api = koinInject<ApiClient>()
		AsyncImage(
			url = image.getUrl(api),
			blurHash = image.blurHash,
			aspectRatio = image.aspectRatio ?: 1f,
			scaleType = ImageView.ScaleType.CENTER_CROP,
			modifier = Modifier.fillMaxSize()
		)
	}
}

@Stable
@Composable
fun ItemCard(
	item: BaseItemDto,

	modifier: Modifier = Modifier,
	image: @Composable (item: BaseItemDto) -> Unit = { ItemCardImage(it) },
	overlay: @Composable (item: BaseItemDto) -> Unit = {},
	title: @Composable (item: BaseItemDto) -> Unit = { ItemCardTitle(it) },
	subtitle: @Composable (item: BaseItemDto) -> Unit = { ItemCardSubtitle(it) },
	showDetails: Boolean = true,
) {
	Column(modifier = Modifier.then(modifier)) {
		Box {
			image(item)
			overlay(item)
		}

//		AnimatedVisibility(showDetails) {
			title(item)
			subtitle(item)
//		}
	}
}

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

	val image = item.itemImages[ImageType.PRIMARY]

	val shape = RoundedCornerShape(4.dp)
	Box(
		modifier = Modifier
			.clip(shape)
//			.aspectRatio(style.aspectRatio)
			.background(Color.Black)
			.focusable(true, interactionSource)
			.clickable { onOpen() }
			.then(modifier)
	) {
		if (image != null) {
			AsyncImage(
				url = image.getUrl(api),
				blurHash = image.blurHash,
				aspectRatio = image.aspectRatio ?: 1f,
				scaleType = ImageView.ScaleType.CENTER_CROP,
				modifier = Modifier.fillMaxSize()
			)
		}

		Text(
			text = item.name.orEmpty(),
			fontSize = 12.sp,
			maxLines = 3,
			overflow = TextOverflow.Ellipsis,
			color = Color.White,
			modifier = Modifier
				.align(Alignment.BottomStart)
				.fillMaxWidth()
				.background(Color.Black.copy(alpha = 0.6f))
				.padding(5.dp)
		)
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
				Text(row.hashCode().toString(), color = Color.White, fontSize = 18.sp)

				LazyRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
				) {
					items(row.items) { item ->
//						BrowserScreenItem(
//							item = item,
//							onOpen = {},
//							modifier = Modifier
//								.height(150.dp)
//						)
						ItemCard(
							item = item,
							showDetails = true,
							modifier = Modifier
								.height(150.dp)
						)
					}
				}
			}
		}
		// TODO
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
