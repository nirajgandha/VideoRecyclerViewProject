package com.niraj.videorecyclerviewproject.retrofit_api

import androidx.lifecycle.MutableLiveData
import com.niraj.videorecyclerviewproject.model.ResponseFromApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoClass {
    companion object {

        fun getVideosList(): MutableLiveData<ResponseFromApi> {
            val videoListFromApi: MutableLiveData<ResponseFromApi> = MutableLiveData()

            CoroutineScope(Dispatchers.Default).launch {
                launch(Dispatchers.Default) {
                    val apiInterface = APIClient().getApiInterfaceUrl()
                    val apiCall = apiInterface?.videoListApi()
                    withContext(Dispatchers.Default) {
                        apiCall?.enqueue(object : Callback<ResponseFromApi> {
                            override fun onResponse(
                                call: Call<ResponseFromApi>,
                                response: Response<ResponseFromApi>) {
                                if (response.isSuccessful) {
                                    videoListFromApi.postValue(response.body())
                                }
                            }

                            override fun onFailure(call: Call<ResponseFromApi>, t: Throwable) {
                                videoListFromApi.postValue(null)
                            }

                        })
                    }
                }
            }
            return videoListFromApi
        }
    }
}