package com.gswxxn.camerasnap.wrapper.camera.storage

import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method

object Storage: StaticClass() {
    override val className: String
        get() = "com.android.camera.storage.Storage"

    val LOW_STORAGE_THRESHOLD = clazz.field { name = "LOW_STORAGE_THRESHOLD" }.get().long()

    val DIRECTORY = clazz.field { name = "DIRECTORY" }.get().string()

    fun getAvailableSpace() = clazz.method {
        name = "getAvailableSpace"
        emptyParam()
    }.get().long()

}