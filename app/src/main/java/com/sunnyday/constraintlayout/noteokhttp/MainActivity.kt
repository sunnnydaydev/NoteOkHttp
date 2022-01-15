package com.sunnyday.constraintlayout.noteokhttp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object{
        const val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //getDataSync()
    }

    /**
     * 以get请求为栗子，同步的方式请求网络.
     * ps：同步请求方式需要自己开子线程。否则系统会抛异常 [android.os.NetworkOnMainThreadException]
     * */
    private fun getDataSync() {
        thread {
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder()
                .get()
                .url("https://www.baidu.com")
                .build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful){
                response.body?.let {
                    Log.d(TAG,"getDataSync#Successful:${it.string()}")
                }
            }
        }

    }
}