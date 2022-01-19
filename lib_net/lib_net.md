# OkHttp 简单封装

了解过OkHttp基础的同学或许会发现直接使用OkHttp框架还是有好多小问题的：

- 每次手动线程切换：callBack回调结果运行在子线程，这意味着我们想要在安卓中更新UI时要做线程切换。
- 重复对象创建：HttpClient 每次使用都要重新创建。
- 参数拼接：get请求每次都要手动拼接url
- Json对象映射：获取结果是json 字符串时每次都需要手动写重复的Gson映射代码

可见问题还是很多的，这些都是影响我们开发效率的，因此我们可以针对框架进行简单封装，来解决上面的问题。顺便也可以熟悉下OkHttp这个框架 ~ ~ ~

# 设计流程

![请求流程设计](https://github.com/sunnnydaydev/NoteOkHttp/raw/master/screenshot/RequestFlow.png)

# 封装实现

###### 1、全局单例统一管理

```java

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
```

##### 2、Request请求

> 对request进行封装，根据不同的请求方式可实现不同的request方法。可扩展。
>
> 对请求参数进行封装。满足基本的get url 拼接，form表单数据上报。可扩展。

```java

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
```

```java

/**
 * Created by SunnyDay on 2022/1/17 20:50
 *
 * 参数封装成 map，便于统一处理。
 */
class RequestParams {

    // url参数拼接、String 类型Key-value表单数据上传都可用这个map
     val urlParams: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    // todo 其他需求map，自己实现。


    constructor(source: Map<String, String>) {
        for (entry: Map.Entry<String, String> in source.entries) {
            put(entry.key, entry.value)
        }
    }

    constructor(key: String, value: String) : this(mapOf<String, String>().apply { key to value })

    constructor() : this(mapOf<String, String>().apply {})


    /**
     * put url params into map
     * */
    private fun put(key: String, value: String) {
        urlParams[key] = value
    }

    fun hasParams(): Boolean {
        if (urlParams.size > 0) return true
        return false
    }
}
```

###### 3、CallBack

> 自定义特定功能的Callback，这里实现了线程切换+Json自动转换的callback。可自行模仿实现自己的。

```java

/**
 *  Created by SunnyDay on 2022/1/17 21:04
 *
 * CommonJson callBack, Convert stream data to json String !
 */

class CommonJsonCallback<T>(private val mListener: DisposeBaseListDataListener<T>) : Callback {

    private var mHandler: Handler = Handler(Looper.getMainLooper())

    override fun onFailure(call: Call, e: IOException) {
        mHandler.post {
            mListener.onFailure(call, e)
        }
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
            mHandler.post {
                handleStringJson(response.body?.string())
            }
        } else {
            mListener.onFailure(
                call,
                Exception("response.code:${response.code} response.message:${response.message}")
            )
        }
    }


    private fun handleStringJson(jsonStr: String?) {

        try {
            val obj = Gson().fromJson<BaseListModel<T>>(jsonStr, BaseListModel::class.java)
            if (obj != null) {
                mListener.onSuccess(obj)
            } else {
                mListener.onFailure(null, Exception("parse json error!"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mListener.onFailure(null, Exception("parse json error!"))
        }

    }
}
```

> 其实具体的数据还是要通过接口再回调一次，传给用户。
>
> 接口数据也是根据具体的需求进行自定义即可。

```java
/**
 * Create by SunnyDay on 21:08 2022/01/17
 */
interface DisposeBaseListDataListener<T>{
    fun onSuccess(response: BaseListModel<T>)
    fun onFailure(call: Call?, e: Exception)
}
```



###### 4、实体类

> 一般前后台接口交互时有些东西是固定的如后台返回的请求成功、请求失败、等等之类的字段，不可能每个接口都重新返回这些数据，这些通用的数据定义一份实体类即可。同理安卓端也可遵循这个规范，定义Base类。具体的Base类定义要和后端统一下即可。假如列表数据都定义成如下格式：

```java
/**
 * Create by SunnyDay on 21:41 2022/01/17
 * 基本的列表数据
 */

class BaseListModel<T> {
     var responseCode: String? = null
     var responseMsg: String? = null
     var dataList: List<T>? = null
}
```

> 只需要一个泛型去接受即可。假如后端通知我们接口A要返回数据为：

```java
{
  "responseCode": "00",
  "responseMsg": "request success",
  "dataList": [
    { // 接口A的数据对象类型
      "categoryId": 0,
      "categoryName": "打野"
    }
  ]
}
```

> 我们只需要定义一个实体类去接受这个数据即可,这样后续假如其他接口B返回其他格式的实体类，我们再定义一份实体类即可。

```java
/**
 * Create by SunnyDay on 21:59 2022/01/17
 * 具体的列表实体类
 */

class DataListBean {//接口A的数据对象类型
     var categoryId: String? = null
     var categoryName: String? = null
}
```

###### 小结

使用很easy，以基本的json请求直接获取实体类为栗子：

```java

       CommonOkHttpClient
           .instance
           .sendHttpRequest(
               CommonRequest.createGetRequest("http://192.168.31.30:8080/OkHttp/TestListJson.json",null),
               CommonJsonCallback(object : DisposeBaseListDataListener<BaseListModel<DataListBean>> {
                   override fun onSuccess(response: BaseListModel<BaseListModel<DataListBean>>) {
                     Log.i(TAG,"onSuccess:${response.dataList?.size}")
                   }

                   override fun onFailure(call: Call?, e: Exception) {
                       e.printStackTrace()
                       Log.i(TAG,"onFailure:${e.message}")
                   }

               })
           )

```

最基本的封装就实现了~

