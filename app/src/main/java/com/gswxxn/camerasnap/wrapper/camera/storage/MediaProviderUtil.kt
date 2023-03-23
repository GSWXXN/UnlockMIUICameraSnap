package com.gswxxn.camerasnap.wrapper.camera.storage

import android.content.Context
import android.net.Uri
import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.StringClass

object MediaProviderUtil: StaticClass() {
    override val className: String
        get() = "com.android.camera.storage.MediaProviderUtil"

    fun getContentUriFromPath(context: Context, path: String) = clazz.method {
        name = "getContentUriFromPath"
        param(ContextClass, StringClass)
    }.get().invoke<Uri>(context, path)!!
}