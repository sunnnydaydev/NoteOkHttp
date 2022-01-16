# OkHttp 简单封装

了解过OkHttp基础的同学或许会发现直接使用OkHttp框架还是有好多小问题的：

- 每次手动线程切换：callBack回调结果运行在子线程，这意味着我们想要在安卓中更新UI时要做线程切换。
- 重复对象创建：HttpClient 每次使用都要重新创建。
- 参数拼接：get请求每次都要手动拼接url
- Json对象映射：获取结果是json 字符串时每次都需要手动写重复的Gson映射代码

可见问题还是很多的，这些都是影响我们开发效率的，因此我们可以针对框架进行简单封装，来解决上面的问题。顺便也可以熟悉下OkHttp这个框架 ~ ~ ~

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

##### 2、

###### 3、



todo ：

- 目录xmind
- 框架请求流程图