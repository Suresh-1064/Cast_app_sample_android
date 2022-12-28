package com.example.videocast_xml

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var mCastContext: CastContext? = null
    private val localExecutor: Executor = Executors.newSingleThreadExecutor()
    private var mMediaRouteButton: MediaRouteButton? = null
    private var mCastSession: CastSession? = null
    private var mSessionManagerListener: SessionManagerListener<CastSession>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Cast Button
        mMediaRouteButton = findViewById<View>(R.id.media_route_button) as MediaRouteButton
        CastButtonFactory.setUpMediaRouteButton(applicationContext, mMediaRouteButton!!)
       mCastContext = CastContext.getSharedInstance(this, localExecutor).result
        setupCastListener()
        val mSessionManager =
            CastContext.getSharedInstance(this, localExecutor).result.sessionManager
        mCastSession = mSessionManager.currentCastSession
    }

    override fun onResume() {
        super.onResume()
        mCastContext!!.sessionManager.addSessionManagerListener(
            mSessionManagerListener!!, CastSession::class.java
        )
    }

    // Listener for cast event
    private fun setupCastListener() {
        mSessionManagerListener = object : SessionManagerListener<CastSession> {
            override fun onSessionEnded(session: CastSession, error: Int) {
                Log.e(TAG, "Application Disconnected : onSessionEnded")
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                Log.e(TAG, "Application Connected : onSessionResumed")
                onApplicationConnected(session)
            }

            override fun onSessionResumeFailed(session: CastSession, error: Int) {
                Log.e(TAG, "Application Disconnected : onSessionResumeFailed")
            }

            override fun onSessionStarted(session: CastSession, sessionId: String) {
                Log.e(TAG, "Application Connected : onSessionStarted")
                onApplicationConnected(session)
            }

            override fun onSessionStartFailed(session: CastSession, error: Int) {
                Log.e(TAG, "Application disconnected : onSessionStartFailed")
            }

            override fun onSessionStarting(session: CastSession) {
                Log.e(TAG, "Application Connected : onSessionStarting")
            }

            override fun onSessionEnding(session: CastSession) {
                Log.e(TAG, "Application disconnected : onSessionEnding")
            }

            override fun onSessionResuming(session: CastSession, sessionId: String) {
                Log.e(TAG, "Application Connected : onSessionResuming")
            }

            override fun onSessionSuspended(session: CastSession, reason: Int) {
                Log.e(TAG, "Application disconnected : onSessionSuspended")
            }

            // To stream media in to receiver
            private fun onApplicationConnected(castSession: CastSession) {
                mCastSession = castSession
                loadRemoteMedia(true)

            }
        }
    }


    private fun loadRemoteMedia(autoPlay: Boolean) {

        if (mCastSession == null) {
            return
        }
        val remoteMediaClient = mCastSession!!.remoteMediaClient ?: return
        remoteMediaClient.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {
                val intent = Intent(this@MainActivity, ExpandedControlsActivity::class.java)
                startActivity(intent)
                remoteMediaClient.unregisterCallback(this)
            }
        })
        remoteMediaClient.load(MediaLoadRequestData.Builder()
            .setMediaInfo(buildMediaInfo())
            .setAutoplay(autoPlay)
            .setCredentials("user-credentials")
            .setAtvCredentials("atv-user-credentials")
            .build())



    }


    private fun buildMediaInfo(): MediaInfo {
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, "Ambarish")
        movieMetadata.putString(MediaMetadata.KEY_TITLE, "This is my first movie")
        return MediaInfo.Builder("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("videos/mp4")
            .setMetadata(movieMetadata)
            .build()
    }

    companion object {
        private const val TAG = "CastAppSample"
    }
}