package com.niraj.videorecyclerviewproject.ui

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.niraj.videorecyclerviewproject.adapter.VideoRecyclerViewAdapterCustom
import com.niraj.videorecyclerviewproject.model.Video


class CustomRecyclerView : RecyclerView {
    private val mTAG = "CustomRecyclerView"
    private lateinit var contextApp: Context
    private lateinit var videoSurface: StyledPlayerView
    private var thumbnail: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var viewParent: View? = null
    private var videoPlayer: ExoPlayer? = null
    private var isVideoViewAdded: Boolean = false
    private var playPosition = -1
    private var requestManager: RequestManager? = null
    private var frameLayout: FrameLayout? = null
    private var videoSurfaceHeightDef: Int = 0
    private var screenHeightDef: Int = 0
    private lateinit var videoArrayList: ArrayList<Video>

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        contextApp = context.applicationContext
        screenHeightDef = context.resources.displayMetrics.heightPixels

        videoSurface = StyledPlayerView(contextApp)
        videoSurface.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        videoPlayer = ExoPlayer.Builder(contextApp).build()
        videoSurface.useController = true
        videoSurface.player = videoPlayer

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE) {
                    if (null != thumbnail) {
                        thumbnail?.visibility = View.VISIBLE
                    }

                    playVideo(!recyclerView.canScrollVertically(1))
                }
            }
        })

        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {

            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (null != viewParent && viewParent == view) {
                    resetVideoView()
                }
            }

        })

        videoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.d(mTAG, "onPlayWhenReadyChanged: buffering")
                        progressBar?.visibility = VISIBLE
                    }
                    Player.STATE_ENDED -> {
                        Log.d(mTAG, "onPlayWhenReadyChanged: video end")
                        videoPlayer?.seekTo(0)
                    }
                    Player.STATE_READY -> {
                        Log.d(mTAG, "onPlayerStateChanged: player ready")
                        progressBar?.visibility = GONE
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }

                    Player.STATE_IDLE -> {
                    }
                }
            }
        })

    }

    internal fun playVideo(isListEnd: Boolean) {
        val targetPos: Int

        if (!isListEnd) {
            val startPos = (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
            var endPos = (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()

            if (endPos - startPos > 1) {
                endPos = startPos + 1
            }

            if (startPos < 0 || endPos < 0) {
                return
            }

            targetPos = if (startPos != endPos) {
                val startPosVideoHeight = getVisibleVideoSurfaceHeight(startPos)
                val endPosVideoHeight = getVisibleVideoSurfaceHeight(endPos)

                if (startPosVideoHeight > endPosVideoHeight) {
                    startPos
                } else {
                    endPos
                }
            } else {
                startPos
            }
        } else {
            targetPos = if (this::videoArrayList.isInitialized) {
                videoArrayList.size - 1
            } else {
                0
            }
        }
        Log.d(mTAG, "playVideo at targetPos: $targetPos")

        if (targetPos == playPosition) {
            return
        }

        if (playPosition != -1) {
            videoArrayList[playPosition].time = videoPlayer?.currentPosition
        }

        playPosition = targetPos
        videoSurface.visibility = View.INVISIBLE
        removeVideoView(videoSurface)

        val currentPos =
            targetPos - ((layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition())

        val child = getChildAt(currentPos) ?: return

        val holder = child.tag as VideoRecyclerViewAdapterCustom.VideoViewHolderCustom

        thumbnail = holder.binding.thumbnail
        progressBar = holder.binding.progressBar
        viewParent = holder.binding.root
        requestManager = holder.requestManager
        frameLayout = holder.binding.mediaC

        videoSurface.player = videoPlayer

        val dataSourceFactory = DefaultDataSource.Factory(context)
        if (null != holder.videoObject) {
            val mediaUrl = holder.videoObject!!.sources
            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(Uri.parse(mediaUrl))
            )
            videoPlayer?.setMediaSource(videoSource)
            videoPlayer?.prepare()
            videoPlayer?.playWhenReady = true

        }
    }

    private fun getVisibleVideoSurfaceHeight(playPos: Int): Int {
        val atPos = playPos - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        Log.d(mTAG, "getVisibleVideoSurfaceHeight: $atPos")

        val child = getChildAt(atPos) ?: return 0
        videoSurfaceHeightDef = child.height
        val location = IntArray(2)
        child.getLocationInWindow(location)

        return if (location[1] < 0) {
            location[1] + videoSurfaceHeightDef
        } else {
            screenHeightDef - location[1]
        }

    }

    // Remove the old player from holder
    private fun removeVideoView(videoView: StyledPlayerView) {
        val parent = videoView.parent as? ViewGroup ?: return
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
            viewParent?.setOnClickListener(null)
        }
    }

    private fun addVideoView() {
        frameLayout?.addView(videoSurface)
        isVideoViewAdded = true
        videoSurface.requestFocus()
        videoSurface.visibility = VISIBLE
        videoSurface.alpha = 1F
        thumbnail?.visibility = GONE
        //seek video to resume from previous position
        if (-1 != playPosition) {
            videoPlayer?.seekTo(videoArrayList[playPosition].time ?: 0)
            videoArrayList[playPosition].time = 0L
        }
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            //save time of previous playing video
            if (playPosition != -1) {
                videoArrayList[playPosition].time = videoPlayer?.currentPosition
            }
            removeVideoView(videoSurface)
            playPosition = -1
            videoSurface.visibility = INVISIBLE
            thumbnail!!.visibility = VISIBLE
        }
    }

    fun releasePlayer() {
        videoPlayer?.release()
        videoPlayer = null
        viewParent = null
    }

    fun pausePlayer() {
        videoPlayer?.pause()
    }

    fun resumePlayer() {
        videoPlayer?.play()
    }

    fun setVideoArray(videoArrayList: ArrayList<Video>) {
        this.videoArrayList = videoArrayList
    }
}