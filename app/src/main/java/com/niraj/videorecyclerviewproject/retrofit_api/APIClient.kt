package com.niraj.videorecyclerviewproject.retrofit_api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

class APIClient {
    companion object {
        var baseURL = "https://run.mocky.io/v3/"
        var apiInterface: APIInterface ?= null
        var okHttpClient: OkHttpClient? = null
    }

    /*private val client: Retrofit
        get() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client: OkHttpClient = Builder().addInterceptor(interceptor).build()
            return Retrofit.Builder()
                .baseUrl("https://run.mocky.io/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }*/

    fun getApiInterfaceUrl(): APIInterface? {
        if (null == apiInterface) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient!!)
                .build()
            apiInterface = retrofit.create(APIInterface::class.java)
        }
        return apiInterface
    }
}