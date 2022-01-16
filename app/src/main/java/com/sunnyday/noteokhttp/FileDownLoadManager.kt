package com.sunnyday.noteokhttp

import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

/**
 * Created by SunnyDay on 2022/1/16 12:24:20
 *  todo 以后学过 Kt File 扩展函数后，使用File 扩展函数对下载代码进行优化。
 */
object FileDownLoadManager {

    private const val TAG = "FileDownLoadManager"
    const val APP_CACHE_DIR = "data/data/${BuildConfig.APPLICATION_ID}/cache/"
    const val APP_FILE_DIR = "data/data/${BuildConfig.APPLICATION_ID}/files/"
    const val APP_ROOT_DIR = "data/data/${BuildConfig.APPLICATION_ID}"

    /**
     * 文件下载
     * @param path 本地路径，下载的文件需要被放置的位置。
     * @param url 要下载文件的url。
     * @param iFileDownLoadListener 文件下载监听
     * */
    fun downloadFile(path: String, url: String, iFileDownLoadListener: IFileDownLoadListener) {

        val startTime = System.currentTimeMillis()
        Log.i(TAG, "startTime=$startTime")
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                iFileDownLoadListener.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                var inputStream: InputStream? = null
                val buf = ByteArray(2048)
                var len: Int
                var fos: FileOutputStream? = null
                try {
                    inputStream = response.body?.byteStream()
                    val total: Long = response.body!!.contentLength()
                    val file = File(path, url.substring(url.lastIndexOf("/") + 1))
                    fos = FileOutputStream(file)
                    var sum: Long = 0
                    while (inputStream?.read(buf).also { len = it!! } != -1) {
                        fos.write(buf, 0, len)
                        sum += len.toLong()
                        val progress = (sum * 1.0f / total * 100).toInt()
                        iFileDownLoadListener.onDownloading(progress)
                    }
                    fos.flush()
                    iFileDownLoadListener.onSuccess()
                    Log.i(TAG, "totalTime=" + (System.currentTimeMillis() - startTime))

                } catch (e: Exception) {
                    e.printStackTrace()
                    iFileDownLoadListener.onFailure(e)
                } finally {
                    try {
                        inputStream?.close()
                    } catch (e: IOException) {
                    }
                    try {
                        fos?.close()
                    } catch (e: IOException) {
                    }
                }
            }
        })
    }
}

/**
 * 下载监听
 * */
interface IFileDownLoadListener {
    // 下载中
    fun onDownloading(progress: Int)
    // 下载成功
    fun onSuccess()
    // 下载失败
    fun onFailure(e: Exception)
}