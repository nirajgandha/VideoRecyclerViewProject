package com.niraj.videorecyclerviewproject.retrofit_api

import com.niraj.videorecyclerviewproject.model.ResponseFromApi
import retrofit2.Call
import retrofit2.http.GET

interface APIInterface {

    @GET("9dab915c-85b9-4ea6-8d6f-33fe992d8ae3")
    fun videoListApi(): Call<ResponseFromApi>
}