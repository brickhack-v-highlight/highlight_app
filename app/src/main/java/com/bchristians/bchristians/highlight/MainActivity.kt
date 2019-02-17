package com.bchristians.bchristians.highlight

import android.net.TrafficStats
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.HandlerThread
import android.support.v4.app.Fragment
import android.util.Log
import android.view.WindowManager
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class MainActivity :
    AppCompatActivity() {

    var initialBytesReceived: Long = -1
    var initialBytesSent: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        switchToFragment(MenuFragment(), "menu")

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

    fun switchToFragment(fragment: Fragment, tag: String?) {
        val transaction = supportFragmentManager?.beginTransaction() ?: return
        transaction.replace(R.id.root, fragment)
        transaction.addToBackStack(tag)
        transaction.commit()
    }

    private fun logBytesUsed() {

        val curBytesReceived = TrafficStats.getTotalRxBytes() - this.initialBytesReceived
        val curBytesSent = TrafficStats.getTotalTxBytes() - this.initialBytesSent

        Log.d("this_net >>> traffic status", "s:($curBytesSent) \t\tr:($curBytesReceived)")
    }
}
