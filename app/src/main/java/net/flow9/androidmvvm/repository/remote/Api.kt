package net.flow9.androidmvvm.repository.remote

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(ActivityComponent::class)
object Api {

    const val BASE_URL = "https://api.github.com/"

    var token = ""

    @Provides
    fun githubUserService(): GithubUserService = retrofit(BASE_URL).create(GithubUserService::class.java)

    fun schoolService(): SchoolService = retrofit(BASE_URL).create(SchoolService::class.java)
    fun cityService(): CityService = retrofit(BASE_URL).create(CityService::class.java)

    private fun retrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl).apply {

            val client = OkHttpClient.Builder().apply {

                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(interceptor)

                addInterceptor(
                    Interceptor { chain ->
                        Log.d("인증", "intercepter ==================> ${token}")
                        val builder = chain.request().newBuilder()
//                            .header("Authorization", "Bearer ${token}")
//                            .header("Platform", "ANDROID")
                        val response = chain.proceed(builder.build())

                        val authorization = response.header("Authorization")
                        Log.d("인증", "Authorization=$authorization")

                        if(authorization?.isNotEmpty() == true) {
                            token = authorization
                        }
                        return@Interceptor response
                    }
                )
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
            }.build()

            client(client)
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())

        }.build()
    }
}