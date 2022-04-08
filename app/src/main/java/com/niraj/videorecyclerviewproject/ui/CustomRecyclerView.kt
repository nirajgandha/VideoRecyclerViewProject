package com.niraj.videorecyclerviewproject.ui

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
    private var holder: VideoRecyclerViewAdapterCustom.VideoViewHolderCustom? = null

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
        val defaultDisplay =
            (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        defaultDisplay.getSize(point)
        videoSurfaceHeightDef = point.x
        screenHeightDef = point.y

        videoSurface = StyledPlayerView(contextApp)
        videoSurface.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        videoPlayer = ExoPlayer.Builder(contextApp).build()
        videoSurface.useController = true
        videoSurface.player = videoPlayer

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE) {
                    Log.d(mTAG, "onScrollStateChanged: ")
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
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
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

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (!isPlaying) {
                    holder?.videoObject?.time = videoPlayer?.currentPosition
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
            targetPos = if (null != adapter) {
                adapter!!.itemCount - 1
            } else {
                0
            }
        }
        Log.d(mTAG, "playVideo: targetPos: $targetPos")

        if (targetPos == playPosition) {
            return
        }

        if (holder != null) {
            holder!!.videoObject!!.time = videoPlayer?.currentPosition ?: 0
        }

        playPosition = targetPos
        videoSurface.visibility = View.INVISIBLE
        removeVideoView(videoSurface)

        val currentPos =
            targetPos - ((layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition())

        val child = getChildAt(currentPos) ?: return

        val holder = child.tag as VideoRecyclerViewAdapterCustom.VideoViewHolderCustom
        this.holder = holder

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
        val location = IntArray(2)
        child.getLocationInWindow(location)

        return if (location[1] < 0) {
            location[1] + videoSurfaceHeightDef
        } else {
            screenHeightDef - location[1]
        }

    }

    // Remove the old player
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
        if (null != holder) {
            videoPlayer?.seekTo(holder!!.videoObject!!.time ?: 0)
        }
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurface)
            playPosition = -1
            videoSurface.visibility = INVISIBLE
            thumbnail!!.visibility = VISIBLE
        }
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer!!.release()
            videoPlayer = null
        }
        viewParent = null
    }

    fun pausePlayer() {
        videoPlayer?.pause()
    }

    fun resumePlayer() {
        videoPlayer?.play()
    }
}