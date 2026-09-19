package org.jellyfin.androidtv.di

import android.content.Context
import androidx.startup.Initializer
import org.jellyfin.androidtv.LogInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.module

class KoinInitializer : Initializer<KoinApplication> {
	override fun create(context: Context): KoinApplication = startKoin {
		androidContext(context)
		module<AppModule>()
	}

	override fun dependencies() = listOf(LogInitializer::class.java)
}
