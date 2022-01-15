![知识点](https://github.com/sunnnydaydev/NoteOkHttp/raw/master/screenshot/Okhttp基础.png)

# 前置条件

使用网络时要在Project中声明网络权限的（估计没人不知道~）

```java
<uses-permission android:name="android.permission.INTERNET"/>
```

接下来就是依赖库的引入

```java
  implementation("com.squareup.okhttp3:okhttp:4.9.3")
```

# 基本请求流程

![OkHttp Request Flow](https://github.com/sunnnydaydev/NoteOkHttp/raw/master/screenshot/Okhttp_request_flow.png)

- OkHttpClient：请求客户端。这里可以进行一些通用配置如dns、cookie、超时等。
- Request：这里可以进行一些请求参数配置，如get、post请求等请求方法。添加头、去除头。缓存控制。
- Response：响应信息封装类。
- Call：一个接口，具体实现为RealCall。主要负责执行、取消请求。

# 请求

###### 1、同步请求栗子

```java
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
```

###### 2、异步请求栗子

```java
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
            .get()
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
```

# 上传文件

###### 1、基本的字符串键值对上传

> 上传文件一般以post形式上传，这里以异步请求post方式举个栗子。

```java
    /**
     * 以"Post异步"请求的方式举个上传键值对的栗子.
     * */
    private fun upLoadKeyValueStringByPost(){

        //FormBody是RequestBody的子类。内部对RequestBody进行了简化，
        // 上传参数定义为String类型的key value值。
        val formBody:FormBody = FormBody.Builder()
            .add("name","DevSunnyDay")
            .add("age","18")
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
```

###### 2、其他类型文件上传

> 还是老规矩直接上用法吧,不过上用法之前我们来看下Request支持上传的数据类型：

（1）RequestBody支持上传的数据类型

```java
RequestBody.java
    
public static RequestBody create(MediaType contentType, String content)
    
public static RequestBody create(final MediaType contentType, final File file)

public static RequestBody create(final MediaType contentType, final ByteString content)

public static RequestBody create(final MediaType contentType, final byte[] content)

```

- MediaType：文件类型，可通过MediaType.parse("xxx")来指定文件类型。如MediaType.parse("application/json; charset=utf-8")代表上传的文件类型为json。
- content、file：要上传的文件类型值。

（2）FormBody的源码：

```java
FormBody.kt
    
companion object {
    private val CONTENT_TYPE: MediaType = "application/x-www-form-urlencoded".toMediaType()
  }
// application/x-www-form-urlencoded 就代表为“表单”类型
```



（3）举一反三

> 看过RequestBody#create，同时也参考了FormBody表单的MediaType，RequestBody的实例构建也就信手拈来了：

```java
    /**
     * @function post请求，上传json。
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
     * @function post请求，上传file。
     * */
    private fun fileRequestBody(file: File): RequestBody {
        val mediaType = "File/*".toMediaTypeOrNull()
        return RequestBody.create(mediaType, file)
    }
```



待续~







# 总结

# 参考资料

[Okhttp Github ReadMe](https://github.com/square/okhttp)