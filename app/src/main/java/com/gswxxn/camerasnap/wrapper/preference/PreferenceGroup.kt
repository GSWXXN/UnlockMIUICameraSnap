package com.gswxxn.camerasnap.wrapper.preference

import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.type.java.CharSequenceClass

// 此类下的函数暂未被混淆, 反射名称查找即可, 无需使用DexKit
class PreferenceGroup(private val instance: Any) {

    fun getInstance() = instance

    fun findPreference(key: String) = instance.current().method {
        superClass()
        name = "findPreference"
        param(CharSequenceClass)
    }.call(key)

    fun removePreferenceRecursively(key: String) = instance.current().method {
        superClass()
        name = "removePreferenceRecursively"
        param(CharSequenceClass)
    }.call(key)

    fun addPreference(preference: Any) = instance.current().method {
        superClass()
        name = "addPreference"
        param("androidx.preference.Preference")
    }.call(preference)
}