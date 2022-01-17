package com.sunnyday.lib_net.listener

import com.sunnyday.lib_net.model.BaseModel
import okhttp3.Call

/**
 * Create by SunnyDay on 21:08 2022/01/17
 */

interface DisposeDataListener<T>{
    fun onSuccess(response: BaseModel<T>)
    fun onFailure(call: Call?, e: Exception)
}