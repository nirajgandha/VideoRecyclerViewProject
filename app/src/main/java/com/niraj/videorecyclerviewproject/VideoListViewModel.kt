package com.niraj.videorecyclerviewproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.niraj.videorecyclerviewproject.model.ResponseFromApi
import com.niraj.videorecyclerviewproject.retrofit_api.RepoClass

class VideoListViewModel : ViewModel() {
    private var videoListLiveData:MutableLiveData<ResponseFromApi>? = MutableLiveData<ResponseFromApi>()

    fun getVideoListLiveData(): LiveData<ResponseFromApi>? {
        return videoListLiveData
    }

    init {
        videoListLiveData = RepoClass.getVideosList()
    }
}