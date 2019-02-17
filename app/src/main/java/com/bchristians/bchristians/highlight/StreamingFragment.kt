package com.bchristians.bchristians.highlight

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.faucamp.simplertmp.RtmpHandler
import net.ossrs.yasea.SrsEncodeHandler
import net.ossrs.yasea.SrsPublisher
import net.ossrs.yasea.SrsRecordHandler
import java.io.IOException
import java.net.SocketException

class StreamingFragment:
    Fragment(),
    RtmpHandler.RtmpListener,
    SrsRecordHandler.SrsRecordListener,
    SrsEncodeHandler.SrsEncodeListener {

    var rootView: View? = null
    private lateinit var srsPublisher: SrsPublisher
    private var deviceId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.rootView = inflater.inflate(R.layout.fragment_streaming, container, false)

        (this.context as? Activity)?.let { activity ->
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE), 1)
            // Start Transmission
            this.srsPublisher = SrsPublisher(this.rootView?.findViewById(R.id.camera_view))
            this.srsPublisher.setEncodeHandler(SrsEncodeHandler(this))
            this.srsPublisher.setRtmpHandler(RtmpHandler(this))
            this.srsPublisher.setRecordHandler(SrsRecordHandler(this))

            this.srsPublisher.setPreviewResolution(640, 360)
            this.srsPublisher.setOutputResolution(360, 640)

            this.srsPublisher.setVideoHDMode()
            this.srsPublisher.startCamera()

            if( ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                this.deviceId =
                        (activity.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)
                            ?.imei
                            ?.hashCode()
                            ?.toString()
                            ?: "unknown_" + (Math.random() * 100000).toInt()
            } else {
                this.deviceId = "unknown_" + (Math.random() * 100000).toInt()
            }
        }

        return this.rootView
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            this.srsPublisher.startCamera()
            this.srsPublisher.startPublish(this.getString(R.string.config_stream_url, this.deviceId))
        }, 1000) // there's some async stuff here, so just give it a second
    }

    override fun onPause() {
        super.onPause()

//        this.srsPublisher.stopPublish()
//        this.srsPublisher.stopCamera()
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