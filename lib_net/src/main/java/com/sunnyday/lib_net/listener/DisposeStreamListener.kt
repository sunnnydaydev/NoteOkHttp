package com.sunnyday.lib_net.listener

import okhttp3.Call
import okhttp3.Response

/**
 * Create by SunnyDay on 20:59 2022/01/19
 */
interface DisposeStreamListener {
    fun onSuccess(call: Call, response: Response)
    fun onFailure(call: Call?, e: Exception)
}