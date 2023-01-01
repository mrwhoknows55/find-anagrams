package com.mrwhoknows.findanagrams

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.InputStreamReader
import java.io.Reader
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providesAppContext(
        @ApplicationContext context: Context
    ) = context

    @Provides
    @Singleton
    fun providesAssetManager(
        context: Context
    ): AssetManager = context.assets

    @Provides
    fun providesWordReader(
        assets: AssetManager
    ): Reader = InputStreamReader(assets.open("words.txt"))
}