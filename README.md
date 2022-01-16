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
        return RequestBody.create(mediType, file)
    }
```



###### 3、多文件上传

> 多文件上传很好理解就是上传文件时可以上传多个文件，这多个文件可以为相同文件类型也可以为不同文件类型。

```java
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
```

> OkHttp提供了MultipartBody供我们实现多文件上传，MultipartBody与FormBody一样也是RequestBody的实现类。我们按照要api求很容易实现多文件上传。

# 其他

###### 1、文件下载

> OKHttp中没有提供下载文件的功能 ,不过从网络获取了输入流后我们就可实现文件的下载。

```java
/**
 * Created by SunnyDay on 2022/1/16 12:24:20
 * todo 以后学过 Kt File 扩展函数后，使用File 扩展函数对下载代码进行优化。
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
```



###### 2、常见配置

> 如下是一些常见的对OkHttpClient的配置。

```java
                val client = OkHttpClient.Builder()                 
                // 连接超时，客户端请求连接目标域名端口的时间。默认10s。
                .connectTimeout(60 * 1000, TimeUnit.MILLISECONDS)
                //读取超时 默认10s
                .readTimeout(60 * 1000, TimeUnit.MILLISECONDS)
                //连接失败重试（默认1次，如果想自定义次数可以使用拦截器实现）
                .retryOnConnectionFailure(true)
                // 允许重定向
                .followRedirects(true)
                // 设置分发器,分发OkHttp的请求，OkHttp的同步、异步请求都是通过分发器实现的。
                //   XxxDispatcher 自己自定义。
                .dispatcher(XxxDispatcher)
                // 设置拦截器，XxxInterceptor自己自定义。
                .addInterceptor(XxxInterceptor)
```

###### 3、拦截器

> 上面了解到了拦截器、分发器的概念，这里有必要再看下OkHttp“完整”请求流程。

![知识点](https://github.com/sunnnydaydev/NoteOkHttp/raw/master/screenshot/Okhttp_request_flow_full.png)

> 同步和异步都是通过Dispatc分发器来分发的 , 异步操作比同步操作多了创建线程池的操作，开启了子线程。 分发器最终会走到拦截器中。

啥是拦截器？可以理解为代码的Hook操作，就是在源码的某一处添加我们自己的代码。

拦截器的作用是啥呢？OkHttp的拦截器就是在客户端发起请求、服务端响应请求返回数据这一段时间进行拦截操作。

按照功能划分拦截器可分为应用拦截器、网络拦截器。应用拦截器关注的是发送请求，不能拦截 发起请求到请求成功后返回数据的中间的这段时期。  网络拦截器关注的是发起请求和请求后获取的数据中间的这一过程。 因此应用拦截器和网络拦截器的区别：网络拦截器可以重定向。 

（1）系统源码中的5个拦截器

- RetryAndFollowUpInterceptor： 负责失败重连工作，并不是所有的网络请求都会进行失败重连,在此拦截器内部会进行网络请求的异常检测和响应码的判断,如果都在限制范围内,那么就可以进行失败重连。 
- BridgeInterceptor： 负责设置内容长度、编码方式、设置gzip压缩、添加请求头、cookie等相关功能 。
- CacheInterceptor：  HTTP的缓存的工作是通过CacheInterceptor拦截器来完成的 ， 如果有缓存 但是不能使用网络 直接返回缓存结果。 如果当前未使用网络 并且缓存不可以使用，通过构建者模式创建一个Response响应 抛出504错误。  
- ConnectInterceptor：建立可用的连接
- CallServerInterceptor：负责将请求写入io，读取服务器写入的数据。

（2）自定义拦截器

> 有时候需要实现自己的特定功能，这时候就需要自定义拦截器了。

```java
       //HttpLoggingInterceptor三方库直接用，打印网络日志。
        val httpLogInterceptor = HttpLoggingInterceptor()
        //默认Level.NONE
        httpLogInterceptor.level = HttpLoggingInterceptor.Level.BODY 
            
        val client = OkHttpClient.Builder()
                     .addInterceptor(httpLogInterceptor)
                     .build()
```

这个是OkHttp官方作为一个Lib提供的使用时需要自己引入依赖。主要是用来打印网络日志的，日志有如下级别：

- Level.NONE：不打印网络日志
- Level.BASIC ：打印请求/响应行
- Level.HEADER ：打印请求/响应行 + 头
- Level.BODY ：打印请求/响应行 + 头 + 体

```java

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
```

```java
    /**
     * okHttp 拦截器测试
     * 1、HttpLoggingInterceptor，这个为官方提供的库com.squareup.okhttp3:logging-interceptor:4.9.3
     * 2、MockResponseInterceptor为自定义拦截器，拦截网络返回结果，更改为自定义值。
     * */
    private fun okHttpInterceptor() {
        val httpLogInterceptor = HttpLoggingInterceptor()
        //默认Level.NONE
        httpLogInterceptor.level = HttpLoggingInterceptor.Level.BODY
        thread {
            val client = OkHttpClient.Builder()
                .addInterceptor(httpLogInterceptor)
                .addInterceptor(MockResponseInterceptor())
                .build()
            val request = Request.Builder()
                .get()
                .url("https://www.baidu.com")
                .build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let {
                    Log.d(TAG, "okHttpInterceptor#Successful:${it.string()}")
                }
            }else{
                Log.d(TAG, "okHttpInterceptor#False")
                Log.d(TAG, "response.code:${response.code}")
                Log.d(TAG, "response.message:${response.message}")
                Log.d(TAG, "response.body.string:${response.body?.string()}")
            }
        }
    }
log：
com.sunnyday.noteokhttp D/MainActivity: okHttpInterceptor#False
com.sunnyday.noteokhttp D/MainActivity: response.code:400
com.sunnyday.noteokhttp D/MainActivity: response.message:request error
com.sunnyday.noteokhttp D/MainActivity: response.body.string:{"message":"I am mock data ~ "}
```



# [简单封装]()

# 参考资料

[Okhttp Github ReadMe](https://github.com/square/okhttp)