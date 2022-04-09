package com.niraj.videorecyclerviewproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.niraj.videorecyclerviewproject.databinding.RecyclerViewListItemBinding
import com.niraj.videorecyclerviewproject.model.Video

class VideoRecyclerViewAdapterCustom(videoArrayList: ArrayList<Video>, requestManager: RequestManager): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal var videoArrayList: ArrayList<Video> = arrayListOf()
    private var requestManager: RequestManager
    init {
        this.videoArrayList = videoArrayList
        this.requestManager = requestManager
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolderCustom {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerViewListItemBinding.inflate(layoutInflater, parent, false)
        return VideoViewHolderCustom(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as VideoViewHolderCustom).onBind(requestManager, videoArrayList[position])
    }

    override fun getItemCount(): Int {
        return videoArrayList.size
    }

    inner class VideoViewHolderCustom(val binding: RecyclerViewListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var requestManager: RequestManager? = null
        var videoObject: Video ?= null

        fun onBind(manager: RequestManager, videoObject: Video) {
            if (videoObject.sources.isNullOrEmpty()) {
                binding.thumbnail.background = AppCompatResources.getDrawable(binding.root.context, android.R.color.holo_blue_dark)
            }
            requestManager = manager
            this.videoObject = videoObject
            binding.root.tag = this
            binding.titleTv.text = videoObject.title
            requestManager?.load(getImagePath(videoObject))?.into(binding.thumbnail)
        }

        private fun getImagePath(videoObject: Video): String {
            val source = videoObject.sources!!
            val lastIndex = source.lastIndexOf("/")
            val subString = source.subSequence(0, lastIndex + 1).toString()
            return subString.plus(videoObject.thumb!!)
        }

        fun saveTime(currentPosition: Long?) {
            videoObject?.time = currentPosition ?: 0
        }

    }
}