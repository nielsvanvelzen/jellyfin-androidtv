package org.jellyfin.androidtv.di

import org.jellyfin.androidtv.BuildConfig
import org.jellyfin.apiclient.Jellyfin
import org.jellyfin.apiclient.android
import org.jellyfin.apiclient.logging.AndroidLogger
import org.jellyfin.apiclient.model.ClientInfo
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
	single {
		Jellyfin {
			clientInfo = ClientInfo("Android TV", BuildConfig.VERSION_NAME)
			logger = AndroidLogger()

			android(androidApplication())
		}
	}

	single {
		get<Jellyfin>().createApi()
	}
}
