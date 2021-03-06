package com.bchristians.bchristians.highlight

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import android.widget.ImageButton
import com.github.faucamp.simplertmp.RtmpHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import net.ossrs.yasea.SrsEncodeHandler
import net.ossrs.yasea.SrsPublisher
import net.ossrs.yasea.SrsRecordHandler
import java.io.IOException
import java.net.SocketException
import java.util.concurrent.TimeUnit

class StreamingFragment:
    Fragment(),
    RtmpHandler.RtmpListener,
    SrsRecordHandler.SrsRecordListener,
    SrsEncodeHandler.SrsEncodeListener {

    var rootView: View? = null
    var cameraFront = false
    var httpClient: OKHttpClient? = null
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

            this.srsPublisher.setPreviewResolution(1280, 720)
            this.srsPublisher.setOutputResolution(720, 1280)
            this.srsPublisher.setScreenOrientation(180)

            this.srsPublisher.setVideoHDMode()
            this.srsPublisher.startCamera()

            this.srsPublisher.switchCameraFace(Camera.CameraInfo.CAMERA_FACING_BACK)

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

        this.rootView?.findViewById<ImageButton>(R.id.swap_camera_button)?.setOnClickListener {
            if( this.cameraFront ) {
                this.srsPublisher.switchCameraFace(Camera.CameraInfo.CAMERA_FACING_BACK)
                this.cameraFront = false
            } else {
                this.srsPublisher.switchCameraFace(Camera.CameraInfo.CAMERA_FACING_FRONT)
                this.cameraFront = true
            }
        }

        this.startCheckingForRecording()

        return this.rootView
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            this.srsPublisher.startCamera()
            this.srsPublisher.startPublish(this.getString(R.string.config_stream_url, this.deviceId))
//            this.srsPublisher.startPublish("rtmp://a.rtmp.youtube.com/live2/x7cv-4duj-rkag-7ubp")
        }, 1000) // there's some async stuff here, so just give it a second
    }

    override fun onDestroy() {
        super.onDestroy()

//        this.srsPublisher.stopPublish()
//        this.srsPublisher.stopCamera()
    }

    fun startCheckingForRecording() {
        val url = "https://highlight-microservice.herokuapp.com/is_active/${this.deviceId}"

        this.httpClient = OKHttpClient(url) { response ->
            (this.context as? Activity)?.runOnUiThread {
                this.rootView?.findViewById<View>(R.id.recording_outline)?.visibility =
                        if (response.body()?.contentLength() == 4L) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
            }
        }

        val handlerThread = HandlerThread("recordingChecker")
        if (!handlerThread.isAlive)
            handlerThread.start()
        AndroidSchedulers.from(handlerThread.looper).schedulePeriodicallyDirect(
            {
                this.httpClient?.run()
            },
            1000L,
            1000L,
            TimeUnit.MILLISECONDS
        )
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