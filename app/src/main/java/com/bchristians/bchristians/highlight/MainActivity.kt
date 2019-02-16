package com.bchristians.bchristians.highlight

import android.Manifest
import android.net.TrafficStats
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import com.github.faucamp.simplertmp.RtmpHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import net.ossrs.yasea.SrsEncodeHandler
import net.ossrs.yasea.SrsPublisher
import net.ossrs.yasea.SrsRecordHandler
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.net.SocketException
import java.util.concurrent.TimeUnit

class MainActivity :
    AppCompatActivity(),
    RtmpHandler.RtmpListener,
    SrsRecordHandler.SrsRecordListener,
    SrsEncodeHandler.SrsEncodeListener {

    private lateinit var srsPublisher: SrsPublisher

    var initialBytesReceived: Long = -1
    var initialBytesSent: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)

        // Start Transmission
        this.srsPublisher = SrsPublisher(this.findViewById(R.id.camera_view))
        this.srsPublisher.setEncodeHandler(SrsEncodeHandler(this))
        this.srsPublisher.setRtmpHandler(RtmpHandler(this))
        this.srsPublisher.setRecordHandler(SrsRecordHandler(this))


        this.srsPublisher.setVideoHDMode()

        // Start Logging
        this.initialBytesReceived = TrafficStats.getTotalRxBytes()
        this.initialBytesSent = TrafficStats.getTotalTxBytes()

        Log.d("this_net >>> initial bytes rcvd", this.initialBytesReceived.toString())

        Log.d("this_net >>> initial bytes sent", this.initialBytesSent.toString())

        val handlerThread = HandlerThread("backgroundThread")
        if (!handlerThread.isAlive)
            handlerThread.start()
        AndroidSchedulers.from(handlerThread.looper).schedulePeriodicallyDirect(
            {
                this.logBytesUsed()
            },
            1000L,
            1000L,
            TimeUnit.MILLISECONDS
        )
    }

    private fun logBytesUsed() {

        val curBytesReceived = TrafficStats.getTotalRxBytes() - this.initialBytesReceived
        val curBytesSent = TrafficStats.getTotalTxBytes() - this.initialBytesSent

        Log.d("this_net >>> traffic status", "s:($curBytesSent) \t\tr:($curBytesReceived)")
    }

    override fun onStart() {
        super.onStart()

        this.srsPublisher.setPreviewResolution(640, 360)
        this.srsPublisher.setOutputResolution(360, 640)

        this.srsPublisher.startCamera()

        this.srsPublisher.startPublish(this.getString(R.string.config_stream_url))
    }

    override fun onPause() {
        super.onPause()

        this.srsPublisher.stopPublish()
        this.srsPublisher.stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()

        this.srsPublisher.stopPublish()
        this.srsPublisher.stopCamera()
    }

    override fun onEncodeIllegalArgumentException(e: IllegalArgumentException?) {
    }

    override fun onNetworkResume() {
        
    }

    override fun onNetworkWeak() {
    }

    override fun onRecordFinished(msg: String?) {
    }

    override fun onRecordIOException(e: IOException?) {
    }

    override fun onRecordIllegalArgumentException(e: IllegalArgumentException?) {
    }

    override fun onRecordPause() {
    }

    override fun onRecordResume() {
    }

    override fun onRecordStarted(msg: String?) {
    }

    override fun onRtmpAudioBitrateChanged(bitrate: Double) {
    }

    override fun onRtmpAudioStreaming() {
    }

    override fun onRtmpConnected(msg: String?) {
    }

    override fun onRtmpConnecting(msg: String?) {
    }

    override fun onRtmpDisconnected() {
    }

    override fun onRtmpIOException(e: IOException?) {
    }

    override fun onRtmpIllegalArgumentException(e: IllegalArgumentException?) {
    }

    override fun onRtmpIllegalStateException(e: IllegalStateException?) {
    }

    override fun onRtmpSocketException(e: SocketException?) {
    }

    override fun onRtmpStopped() {
    }

    override fun onRtmpVideoBitrateChanged(bitrate: Double) {
    }

    override fun onRtmpVideoFpsChanged(fps: Double) {
    }

    override fun onRtmpVideoStreaming() {
    }
}
