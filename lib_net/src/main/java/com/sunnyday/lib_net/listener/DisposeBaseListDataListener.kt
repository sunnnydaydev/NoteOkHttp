package com.sunnyday.lib_net.listener

import com.sunnyday.lib_net.model.BaseListModel
import okhttp3.Call

/**
 * Create by SunnyDay on 21:08 2022/01/17
 */

interface DisposeBaseListDataListener<T>{
    fun onSuccess(response: BaseListModel<T>)
    fun onFailure(call: Call?, e: Exception)
}