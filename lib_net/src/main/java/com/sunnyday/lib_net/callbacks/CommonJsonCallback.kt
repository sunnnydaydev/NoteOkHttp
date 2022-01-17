package com.sunnyday.lib_net.callbacks

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.sunnyday.lib_net.listener.DisposeDataListener
import com.sunnyday.lib_net.model.BaseModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.lang.Exception

/**
 *  Created by SunnyDay on 2022/1/17 21:04
 *
 * CommonJson callBack, Convert stream data to json String !
 */

class CommonJsonCallback<T>(private val mListener: DisposeDataListener<T>) : Callback {

    private var mHandler: Handler = Handler(Looper.getMainLooper())

    override fun onFailure(call: Call, e: IOException) {
        mHandler.post {
            mListener.onFailure(call, e)
        }
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
            mHandler.post {
                handleStringJson(response.body?.string())
            }
        } else {
            mListener.onFailure(
                call,
                Exception("response.code:${response.code} response.message:${response.message}")
            )
        }
    }


    private fun handleStringJson(jsonStr: String?) {

        try {
            val obj = Gson().fromJson<BaseModel<T>>(jsonStr, BaseModel::class.java)
            if (obj != null) {
                mListener.onSuccess(obj)
            } else {
                mListener.onFailure(null, Exception("parse json error!"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mListener.onFailure(null, Exception("parse json error!"))
        }

    }
}