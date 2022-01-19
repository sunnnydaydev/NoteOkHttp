package com.sunnyday.lib_net.model

/**
 * Create by SunnyDay on 20:44 2022/01/19
 * 基本对象数据
 */
class BaseObjModel<T> {
    var responseCode: String? = null
    var responseMsg: String? = null
    var dat: T? = null
}