package com.sunnyday.noteokhttp

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull

/**
 * Create by SunnyDay on 14:20 2022/01/16
 * 模拟网络返回Response
 */
class MockResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 模拟返回数据
        val mockJson = "{\"message\":\"I am mock data ~ \"}"
            return Response.Builder()
                .code(400)// responseCode
                .request(request)
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create("application/json".toMediaTypeOrNull(), mockJson))
                .addHeader("content-type", "application/json")
                .message("request error")//responseMessage
                .build()

    }
}