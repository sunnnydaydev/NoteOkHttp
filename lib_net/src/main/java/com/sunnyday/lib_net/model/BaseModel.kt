package com.sunnyday.lib_net.model

/**
 * Create by SunnyDay on 21:41 2022/01/17
 */

class BaseModel<T> {
    private var responseCode: String? = null
    private var responseMsg: String? = null
    private var dataList: T? = null
}
/**
 * 一般与后台沟通定义好返回数据的基本Model，变化的只有T。
 *
 * 假如变化的只有dataList这部分：
 *     {
"responseCode": "00",
"responseMsg": "request success",
"dataList": [
{
"categoryId": 0,
"categoryName": "打野"
}
]
}
 *
 *
 * */