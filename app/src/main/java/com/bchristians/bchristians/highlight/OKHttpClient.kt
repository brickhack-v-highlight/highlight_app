package com.bchristians.bchristians.highlight

import android.util.Log
import okhttp3.*
import java.io.IOException


class OKHttpClient(val url: String, val cb: (Response) -> Unit) {

    private val client = OkHttpClient()

    fun run() {
        val request = Request.Builder()
            .url(this.url)
            .build()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return

                val responseHeaders = response.headers()
                var i = 0
                val size = responseHeaders.size()
                while (i < size) {
                    Log.e(responseHeaders.name(i), responseHeaders.value(i))
                    i++
                }

                Log.e("response", response.body()?.string())
                this@OKHttpClient.cb(response)
            }
        })
    }
}