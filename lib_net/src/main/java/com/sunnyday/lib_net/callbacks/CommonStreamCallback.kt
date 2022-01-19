package com.sunnyday.lib_net.callbacks

import android.os.Handler
import android.os.Looper
import com.sunnyday.lib_net.listener.DisposeStreamListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * Create by SunnyDay on 20:55 2022/01/19
 * keep  callback  in  UI Thread .
 */
class CommonStreamCallback(private val mListener: DisposeStreamListener) : Callback {
    private var mHandler: Handler = Handler(Looper.getMainLooper())
    override fun onFailure(call: Call, e: IOException) {
        mHandler.post {
            mListener.onFailure(call, e)
        }
    }

    override fun onResponse(call: Call, response: Response) {
        mListener.onSuccess(call, response)
    }

}