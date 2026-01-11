package io.github.onreg.core.network.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.network.BuildConfig
import io.github.onreg.core.network.moshi.InstantJsonAdapter
import io.github.onreg.core.network.rawg.interceptor.RawgApiKeyInterceptor
import io.github.onreg.core.network.retrofit.NetworkResponseCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

private const val RAWG_BASE_URL = "https://api.rawg.io/api/"

@Module
@InstallIn(SingletonComponent::class)
public object NetworkModule {

    @Provides
    @Singleton
    public fun provideMoshi(): Moshi = Moshi.Builder()
        .add(InstantJsonAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    public fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }


    @Provides
    @Singleton
    public fun provideApiKeyInterceptor(): Interceptor = RawgApiKeyInterceptor()

    @Provides
    @Singleton
    public fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        apiKeyInterceptor: Interceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    public fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(RAWG_BASE_URL)
        .client(okHttpClient)
        .addCallAdapterFactory(NetworkResponseCallAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}
