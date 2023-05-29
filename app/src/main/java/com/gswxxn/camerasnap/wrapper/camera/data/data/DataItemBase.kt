package com.gswxxn.camerasnap.wrapper.camera.data.data

import com.gswxxn.camerasnap.wrapper.camera.data.provider.DataProvider
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.type.java.StringClass

// 此类下的函数暂未被混淆, 反射名称查找即可, 无需使用DexKit
class DataItemBase(private val instance: Any) {

    fun getString(key: String, defaultValue: String?) = instance.current().method {
        superClass()
        name = "getString"
        param(StringClass, StringClass)
    }.invoke<String>(key, defaultValue)

    fun editor() = instance.current().method {
        superClass()
        name = "editor"
    }.call().let { DataProvider.ProviderEditor(it!!) }
}