package edu.uga.eits.android.module

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import edu.uga.eits.android.api.Restapi
import edu.uga.eits.android.extensions.ForApplication
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton



@Module class NetworkModule(){

    @Provides @Singleton fun provideGson() = GsonBuilder().create()
    @Provides @Singleton fun provideOkHttpClient(@ForApplication context: Context,logger:HttpLoggingInterceptor,interceptor: Interceptor) : OkHttpClient {
      return OkHttpClient.Builder()
              .cache(Cache(File(context.cacheDir, "http"), 10 * 1024 * 1024L))
              .connectTimeout(2, TimeUnit.SECONDS)
              .readTimeout(30, TimeUnit.SECONDS)
              .writeTimeout(10, TimeUnit.SECONDS)
              .retryOnConnectionFailure(true)
              .addNetworkInterceptor(logger)
              .build()
    }
    @Provides @Singleton fun provideRetrofit(client:OkHttpClient, gson:Gson) =
            Retrofit.Builder()
                    .baseUrl("https://my.uga.edu/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
    @Provides @Singleton fun provideRestapi(retrofit: Retrofit) = retrofit.create(Restapi::class.java)
    @Provides @Singleton
    fun provideLogger(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> Log.d("NetworkModule46", "~~~~http message~~~~ ====> " + message.toString()) })
        logger.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        return logger
    }
    @Provides @Singleton
    fun provideHeaderInterceptor() : Interceptor {
        return Interceptor { chain -> chain.request().newBuilder()
                .apply { addHeader("Connection","Close") } // keep alive doesn't work on android, force close
                .build().let{chain.proceed(it)}
        }
    }

}