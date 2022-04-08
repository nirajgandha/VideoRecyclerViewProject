package com.niraj.videorecyclerviewproject.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ResponseFromApi{
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("videos")
    @Expose
    var videos: ArrayList<Video>? = null
}