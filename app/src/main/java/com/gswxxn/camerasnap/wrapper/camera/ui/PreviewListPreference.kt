package com.gswxxn.camerasnap.wrapper.camera.ui

import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.type.java.AnyClass
import com.highcapable.yukihookapi.hook.type.java.StringClass

class PreviewListPreference(private val instance: Any) {

    fun setDefaultValue(value: Any) = instance.current().method {
        superClass()
        name = "setDefaultValue"
        param(AnyClass)
    }.call(value)

    fun setValue(value: String) = instance.current().method {
        superClass()
        name = "setValue"
        param(StringClass)
    }.call(value)
}