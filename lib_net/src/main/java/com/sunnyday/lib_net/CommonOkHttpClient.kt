package com.sunnyday.lib_net

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

/**
 * Create by SunnyDay on 19:40 2022/01/16
 */

class CommonOkHttpClient private constructor() {
    companion object {
        private const val DEFAULT_TIME_OUT = 60 * 1000L
        private var mOkHttpClient: OkHttpClient
        val instance: CommonOkHttpClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { CommonOkHttpClient() }

        init {
            val build: OkHttpClient.Builder = OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)//连接超时
                .readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)//读超时
                .writeTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)//写超时
//                .addInterceptor {
//
//                }
                .followRedirects(true)// 允许重定向
                // 添加对所有https的支持,
                // true: 支持任意类型的https（无论官方购买的https，或者是自己生成的https请求）
                .hostnameVerifier(HostnameVerifier { _, _ -> true })
            mOkHttpClient = build.build()
        }
    }

    /**
     * @function 发送Http请求
     * @param request request对象
     * @param callback Callback对象
     * */
    fun sendHttpRequest(request: Request, callback: Callback): Call {
        val call: Call = mOkHttpClient.newCall(request)
        call.enqueue(callback)
        return call
    }
}