package com.sunnyday.lib_net.request

import okhttp3.FormBody
import okhttp3.Request
import java.lang.StringBuilder

/**
 * Created by SunnyDay on 2022/1/17 20:36
 */
class CommonRequest {

    companion object {
        /**
         * get request
         * */
        fun createGetRequest(url: String, params: RequestParams?): Request {
            val sb: StringBuilder = StringBuilder(url).append("?")

            return if (params != null) {
                for (entry: Map.Entry<String, String> in params.urlParams.entries) {
                    sb.append(entry.key).append("=").append(entry.value).append("&")
                }
                val disposedUrl = sb.toString().substring(0, sb.length - 1)
                Request.Builder()
                    .url(disposedUrl)
                    .get()
                    .build()
            } else {
                Request.Builder()
                    .url(url)
                    .get()
                    .build()
            }
        }

        /**
         * post request,simple upload form data!
         * */
        fun createPostRequest(url: String, params: RequestParams): Request {
            val bodyBuilder: FormBody.Builder = FormBody.Builder()

            for (entry: Map.Entry<String, String> in params.urlParams.entries) {
                bodyBuilder.add(entry.key, entry.value)
            }

            return Request.Builder()
                .url(url)
                .post(bodyBuilder.build())
                .build()
        }

        /**
         * other request，implementation by you want.
         * 可以吧请求方式定义为接口形式。CommonRequest实现方法。
         * */
        fun createXXXRequest() {
            // implementation here yourself
        }
    }
}