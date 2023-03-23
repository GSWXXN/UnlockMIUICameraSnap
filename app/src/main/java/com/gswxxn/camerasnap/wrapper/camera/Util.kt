package com.gswxxn.camerasnap.wrapper.camera

import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass

object Util: StaticClass() {
    override val className: String
        get() = "com.android.camera.Util"

    fun convertOutputFormatToFileExt(i: Int) = clazz.method {
        name = "convertOutputFormatToFileExt"
        param(IntType)
    }.get().invoke<String>(i)

    fun convertOutputFormatToMimeType(i: Int) = clazz.method {
        name = "convertOutputFormatToMimeType"
        param(IntType)
    }.get().invoke<String>(i)

    fun getDuration(uri: String) = clazz.method {
        name = "getDuration"
        param(StringClass)
    }.get().long(uri)
}