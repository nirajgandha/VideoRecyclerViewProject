package com.niraj.videorecyclerviewproject

import androidx.lifecycle.Observer
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.niraj.videorecyclerviewproject.adapter.VideoRecyclerViewAdapterCustom
import com.niraj.videorecyclerviewproject.databinding.ActivityMainBinding
import com.niraj.videorecyclerviewproject.model.ResponseFromApi
import com.niraj.videorecyclerviewproject.ui.CustomRecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val connectivityService: ConnectivityManager by
    lazy{ getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    private val videoListViewModel: VideoListViewModel by viewModels()
    private var adapter: VideoRecyclerViewAdapterCustom ?= null
    private lateinit var customRecyclerView: CustomRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initController()
        initWork()
    }

    private fun initController() {
        binding.refreshButton.setOnClickListener {
            initWork()
        }
    }

    private fun initWork() {
        if (checkNetworkConnectivity()) {
            binding.networkErrorCL.visibility = View.GONE
            startNetworkRelatedWork()
        } else {
            binding.networkErrorCL.visibility = View.VISIBLE
        }
    }

    private fun startNetworkRelatedWork() {
        val observer = Observer<ResponseFromApi>{
            if (null != it && !it.videos.isNullOrEmpty()) {
                val layoutManager = LinearLayoutManager(this)
                adapter = VideoRecyclerViewAdapterCustom(it.videos!!, getGlide())

                customRecyclerView = CustomRecyclerView(this)
                customRecyclerView.setVideoArray(it.videos!!)

                binding.root.addView(customRecyclerView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                customRecyclerView.layoutManager = layoutManager
                customRecyclerView.adapter = adapter
                Handler(mainLooper).postDelayed({
                    customRecyclerView.playVideo(it.videos!!.size == 1)
                }, 1000)
            }
        }
        videoListViewModel.getVideoListLiveData()?.observe(this, observer)
    }

    private fun checkNetworkConnectivity(): Boolean {
        val network = connectivityService.activeNetwork
        return network != null
    }

    private fun getGlide(): RequestManager {
        val requestOptions = RequestOptions().placeholder(R.color.purple_200).error(R.color.black)
        return Glide.with(this).setDefaultRequestOptions(requestOptions)
    }

    override fun onResume() {
        super.onResume()
        if (this::customRecyclerView.isInitialized) {
            customRecyclerView.resumePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::customRecyclerView.isInitialized) {
            customRecyclerView.pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::customRecyclerView.isInitialized) {
            customRecyclerView.releasePlayer()
        }
    }
}