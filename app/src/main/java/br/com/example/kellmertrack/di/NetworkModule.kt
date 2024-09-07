package br.com.example.kellmertrack.di

import br.com.example.kellmertrack.BuildConfig
import br.com.example.kellmertrack.remote.Authenticator
import br.com.example.kellmertrack.remote.HttpCallInterceptor
import br.com.example.kellmertrack.remote.service.CommonService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideRetrofit(converterFactory: GsonConverterFactory, client: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            //.baseUrl("http://192.168.0.7:8080")
            .baseUrl(BuildConfig.urlApi)
            .addConverterFactory(converterFactory)
            .client(client)
            .build()
    }

    @Provides
    fun provideAuthenticator(): Authenticator {
        return Authenticator()
    }

    @Provides
    fun provideHttpClient(authenticator: Authenticator, httpCallInterceptor: HttpCallInterceptor):OkHttpClient{
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(authenticator)
            .addInterceptor(httpCallInterceptor)
            .build()
    }

    @Provides
    fun provideConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setLenient()
                .create()
        )
    }

    @Provides
    fun provideCommonService(retrofit: Retrofit): CommonService {
        return retrofit.create(CommonService::class.java)
    }
}