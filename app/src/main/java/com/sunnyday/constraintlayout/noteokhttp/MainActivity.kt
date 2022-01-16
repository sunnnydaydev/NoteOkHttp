package com.sunnyday.constraintlayout.noteokhttp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //getDataSync()
        // getDataAsync()
        upLoadKeyValueStringByPost()
    }

    /**
     * 以get请求为栗子，“同步”的方式请求网络.
     *
     * ps：
     *
     * 1、同步请求方式需要自己开子线程。否则系统会抛异常 [android.os.NetworkOnMainThreadException]
     *
     * 2、Response.code是http响应行中的code，如果访问成功则返回200.这个不是服务器设置的，而是http协议中自
     * 带的。
     *
     * 3、response.body().string()只能调用一次，在第一次时有返回值，第二次再调用时将会返回null。原因是,
     * response.body().string()的本质是输入流的读操作，必须有服务器的输出流的写操作时客户端的读操作才能
     * 得到数据。而服务器的写操作只执行一次，所以客户端的读操作也只能执行一次，第二次将返回null。
     *
     * 4、拿到数据更新UI时还需要自己手动在子线程中切回主线程。
     * */
    private fun getDataSync() {
        thread {
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder()
                .get()//默认
                .url("https://www.baidu.com")
                .build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let {
                    Log.d(TAG, "getDataSync#Successful:${it.string()}")
                }
            }
        }
    }

    /**
     * 以get请求为栗子，“异步”的方式请求网络.
     *
     * ps:
     *
     * 1、异步的意思就是网络请求在“子线程”中进行。而且这个操作OkHttp框架帮我们封装好了。如下回调onFailure、
     * onResponse都是运行在子线程中的。好处就是我们不用在自己开子线程了。但是当我们拿到数据时还要自己进行
     * 线程切换更新UI。
     *
     * 2、与同步请求的区别主要是调用执行Call的方法不同这里使用的Call#enqueue
     * */
    private fun getDataAsync() {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .get()// 默认
            .url("https://www.baidu.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "getDataASync#onFailure:${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let {
                        Log.d(TAG, "getDataASync#onResponse:${it.string()}")
                    }
                }
            }

        })
    }

    /**
     * 以"Post异步"请求的方式举个上传键值对的栗子.
     * */
    private fun upLoadKeyValueStringByPost() {

        //FormBody是RequestBody的子类。内部对RequestBody进行了简化，
        // 上传参数定义为String类型的key value值。
        val formBody: FormBody = FormBody.Builder()
            .add("name", "DevSunnyDay")
            .add("age", "18")
            .build()

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .post(formBody)//post(body: RequestBody) 需要RequestBody类型参数。
            .url("https://www.baidu.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "upLoadKeyValueStringByPost#onFailure:${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                // 打印：upLoadKeyValueStringByPost#onResponse:false 百度拒绝我们的post请求
                Log.d(TAG, "upLoadKeyValueStringByPost#onResponse:${response.isSuccessful}")
                if (response.isSuccessful) {
                    response.body?.let {
                        Log.d(TAG, "upLoadKeyValueStringByPost#onResponse:${it.string()}")
                    }
                }
            }
        })
    }

    /**
     * 上传键值对.
     * ps：
     * 这里直接使用FormBody类即可。然后稍加封装。
     * */
    private fun formBody(map: Map<String, String>): FormBody {
        return FormBody.Builder()
            .apply {
                if (map.isNotEmpty()) {
                    for (key in map.keys) {
                        map[key]?.let {
                            add(key, it) // key value 加入form body中
                        }
                    }
                }
            }.build()
    }

    /**
     * @function 上传json。
     *
     *  ps：
     *  1、MediaType.parse("application/json; charset=utf-8") 可获取MediaType类。
     *  2、"".toMediaTypeOrNull() 是MediaType类对String进行的扩展，是一个扩展函数。使用时
     *  要导包okhttp3.MediaType.Companion.toMediaTypeOrNull
     * */
    private fun jsonRequestBody(jsonStr: String): RequestBody {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        return RequestBody.create(mediaType, jsonStr)
    }

    /**
     * @function 上传file。
     * */
    private fun fileRequestBody(file: File): RequestBody {
        val mediaType = "File/*".toMediaTypeOrNull()
        return RequestBody.create(mediaType, file)
    }

    /**
     * 上传多文件。
     * ps：
     * 这里为测试数据。自己可根据业务自己封装。
     * */
    private fun multipartBody(): MultipartBody {
        val file = File(cacheDir.absoluteFile.toString() + "a.txt")// 测试数据
        return MultipartBody.Builder()
            //设置文件类型，MultipartBody.MIXED 默认值
            .setType(MultipartBody.MIXED)
            // 测试数据，上传表单数据
            .addFormDataPart("name", "Tom")
            // 测试数据，上传文件
            .addFormDataPart("file", file.name, fileRequestBody(file))
            .build()
    }

    /**
     * okHttp 一些基础设置
     * */
    private fun okHttpBaseSetting() {
        thread {
            val client = OkHttpClient.Builder()
                // 连接超时，客户端请求连接目标域名端口的时间。默认10s。
                .connectTimeout(60 * 1000, TimeUnit.MILLISECONDS)
                //读取超时 默认10s
                .readTimeout(60 * 1000, TimeUnit.MILLISECONDS)
                //连接失败重试（默认1次，如果想自定义次数可以使用拦截器实现）
                .retryOnConnectionFailure(true)
                // 允许重定向
                .followRedirects(true)
                // 设置拦截器
                // .addInterceptor()
                .build()

            val request = Request.Builder()
                .get()
                .url("https://www.baidu.com")
                .build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let {
                    Log.d(TAG, "okHttpBaseSetting#Successful:${it.string()}")
                }
            }
        }
    }
}