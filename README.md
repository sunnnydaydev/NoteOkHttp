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



# 上传文件

# 总结

# 参考资料

[Okhttp Github ReadMe](https://github.com/square/okhttp)