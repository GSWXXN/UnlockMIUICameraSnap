package com.gswxxn.camerasnap.wrapper.preference

import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.type.java.CharSequenceClass

class PreferenceGroup(private val instance: Any) {

    fun findPreference(key: String) = instance.current().method {
        superClass()
        name = "findPreference"
        param(CharSequenceClass)
    }.call(key)
}