package com.gswxxn.camerasnap.wrapper.camera.ui

import android.content.Context
import com.gswxxn.camerasnap.hook.CameraHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.AnyClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass

class PreviewListPreference(private val instance: Any) {
    constructor(context: Context) : this(
        "com.android.camera.ui.PreviewListPreference".toClass(CameraHooker.appClassLoader).constructor {
            param(ContextClass)
        }.get().call(context)!!
    )

    fun getInstance() = instance

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

    fun setKey(key: String) = instance.current().method {
        superClass()
        name = "setKey"
        param(StringClass)
    }.call(key)

    fun setTitle(title: Int) = instance.current().method {
        superClass()
        name = "setTitle"
        param(IntType)
    }.call(title)

    fun setEntries(entries: Array<CharSequence>) = instance.current().method {
        superClass()
        name = "setEntries"
        param(Array<CharSequence>::class.java)
    }.call(entries)

    fun setEntryValues(entryValues: Array<CharSequence>) = instance.current().method {
        superClass()
        name = "setEntryValues"
        param(Array<CharSequence>::class.java)
    }.call(entryValues)

    fun setPersistent(persistent: Boolean) = instance.current().method {
        superClass()
        name = "setPersistent"
        param(BooleanType)
    }.call(persistent)

    fun setOrder(order: Int) = instance.current().method {
        superClass()
        name = "setOrder"
        param(IntType)
    }.call(order)
}