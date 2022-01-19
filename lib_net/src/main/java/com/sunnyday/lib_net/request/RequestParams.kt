package com.sunnyday.lib_net.request

import java.util.concurrent.ConcurrentHashMap

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