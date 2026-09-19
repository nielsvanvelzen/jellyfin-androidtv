package org.jellyfin.androidtv.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.NetworkFetcher
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.serviceLoaderEnabled
import coil3.svg.SvgDecoder
import coil3.util.Logger
import org.jellyfin.androidtv.BuildConfig
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.browsing.MainActivity
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.util.AndroidVersion
import org.jellyfin.androidtv.util.KeyProcessor
import org.jellyfin.androidtv.util.coil.CoilTimberLogger
import org.jellyfin.androidtv.util.coil.createCoilConnectivityChecker
import org.jellyfin.androidtv.util.profile.createDeviceProfile
import org.jellyfin.playback.core.playbackManager
import org.jellyfin.playback.jellyfin.jellyfinPlugin
import org.jellyfin.playback.media3.exoplayer.ExoPlayerOptions
import org.jellyfin.playback.media3.exoplayer.exoPlayerPlugin
import org.jellyfin.playback.media3.session.MediaSessionOptions
import org.jellyfin.playback.media3.session.media3SessionPlugin
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.android.androidDevice
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.HttpClientOptions
import org.jellyfin.sdk.api.okhttp.OkHttpFactory
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.DeviceInfo
import org.jellyfin.sdk.model.ServerVersion
import org.jellyfin.sdk.model.api.MediaSegmentType
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Module
@ComponentScan("org.jellyfin.androidtv")
class AppModule {
	@Factory
	fun uiModeManager(context: Context): UiModeManager = context.getSystemService()!!

	@Factory
	fun audioManager(context: Context): AudioManager = context.getSystemService()!!

	@Factory
	fun workManager(context: Context): WorkManager = WorkManager.getInstance(context)

	@Single
	@Named("defaultDeviceInfo")
	fun defaultDeviceInfo(context: Context): DeviceInfo = androidDevice(context)

	@Single
	fun okHttpFactory() = OkHttpFactory()

	@Single
	fun httpClientOptions() = HttpClientOptions()

	@Single
	fun jellyfin(
		context: Context,
		@Named("defaultDeviceInfo") deviceInfo: DeviceInfo,
		okHttpFactory: OkHttpFactory,
	): Jellyfin = createJellyfin {
		this.context = context

		val clientName = buildString {
			append("Jellyfin for Android TV")
			if (BuildConfig.DEBUG) append(" (debug)")
		}
		clientInfo = ClientInfo(clientName, BuildConfig.VERSION_NAME)
		this.deviceInfo = deviceInfo
		minimumServerVersion = ServerRepository.minimumServerVersion
		apiClientFactory = okHttpFactory
		socketConnectionFactory = okHttpFactory
	}

	@Single
	fun apiClient(jellyfin: Jellyfin, httpClientOptions: HttpClientOptions): ApiClient =
		jellyfin.createApi(httpClientOptions = httpClientOptions)

	@OptIn(ExperimentalCoilApi::class)
	@Single
	fun networkFetcherFactory(
		okHttpFactory: OkHttpFactory,
		httpClientOptions: HttpClientOptions,
	): NetworkFetcher.Factory {
		return OkHttpNetworkFetcherFactory(
			callFactory = { okHttpFactory.createClient(httpClientOptions) },
			connectivityChecker = ::createCoilConnectivityChecker,
		)
	}

	@Single
	fun imageLoader(context: Context, networkFetcherFactory: NetworkFetcher.Factory): ImageLoader =
		ImageLoader.Builder(context).apply {
			serviceLoaderEnabled(false)
			logger(CoilTimberLogger(if (BuildConfig.DEBUG) Logger.Level.Warn else Logger.Level.Error))

			components {
				add(networkFetcherFactory)

				if (AndroidVersion.isAtLeastP) add(AnimatedImageDecoder.Factory())
				else add(GifDecoder.Factory())
				add(SvgDecoder.Factory())
			}
		}.build()

	@Factory
	fun serverVersion(serverRepository: ServerRepository): ServerVersion =
		serverRepository.currentServer.value?.serverVersion ?: ServerRepository.minimumServerVersion

	@Single
	fun httpDataSourceFactory(
		okHttpFactory: OkHttpFactory,
		httpClientOptions: HttpClientOptions,
	): HttpDataSource.Factory = OkHttpDataSource.Factory(
		okHttpFactory.createClient(
			httpClientOptions.copy(requestTimeout = Duration.ZERO)
		)
	)

	@Single
	fun playbackManager(
		context: Context,
		userPreferences: UserPreferences,
		userSettingPreferences: UserSettingPreferences,
		httpDataSourceFactory: HttpDataSource.Factory,
		api: ApiClient,
		serverVersion: ServerVersion,
	) = playbackManager(context) {
		val activityIntent = Intent(context, MainActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

		val notificationChannelId = "session"
		if (AndroidVersion.isAtLeastO) {
			val channel = NotificationChannel(
				notificationChannelId,
				notificationChannelId,
				NotificationManager.IMPORTANCE_LOW
			)
			channel.setShowBadge(false)
			NotificationManagerCompat.from(context).createNotificationChannel(channel)
		}

		val bufferLength = userPreferences[UserPreferences.bufferLength]
		val exoPlayerOptions = ExoPlayerOptions(
			preferFfmpeg = userPreferences[UserPreferences.preferExoPlayerFfmpeg],
			enableLibass = userPreferences[UserPreferences.assDirectPlay],
			enableDebugLogging = userPreferences[UserPreferences.debuggingEnabled],
			baseDataSourceFactory = httpDataSourceFactory,
			minBufferDuration = bufferLength.minBufferDuration,
			maxBufferDuration = bufferLength.maxBufferDuration,
			bufferForPlaybackDuration = bufferLength.bufferForPlaybackDuration,
			bufferForPlaybackAfterRebufferDuration = bufferLength.bufferForPlaybackAfterRebufferDuration,
		)
		install(exoPlayerPlugin(context, exoPlayerOptions))

		val mediaSessionOptions = MediaSessionOptions(
			channelId = notificationChannelId,
			notificationId = 1,
			iconSmall = R.drawable.app_icon_foreground,
			openIntent = pendingIntent,
		)
		install(media3SessionPlugin(context, mediaSessionOptions))

		val deviceProfileBuilder = { createDeviceProfile(context, userPreferences, serverVersion) }
		install(jellyfinPlugin(api, deviceProfileBuilder, setOf(MediaSegmentType.INTRO), ProcessLifecycleOwner.get().lifecycle))

		defaultRewindAmount = { userSettingPreferences[UserSettingPreferences.skipBackLength].milliseconds }
		defaultFastForwardAmount = { userSettingPreferences[UserSettingPreferences.skipForwardLength].milliseconds }
	}

	@Single
	fun itemLauncher() = ItemLauncher()

	@Single
	fun keyProcessor() = KeyProcessor()

}
