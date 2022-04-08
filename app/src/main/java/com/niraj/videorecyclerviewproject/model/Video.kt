package com.niraj.videorecyclerviewproject.model

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Video {
    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("sources")
    @Expose
    var sources: String? = null

    @SerializedName("subtitle")
    @Expose
    var subtitle: String? = null

    @SerializedName("thumb")
    @Expose
    var thumb: String? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    var time: Long? = 0
}