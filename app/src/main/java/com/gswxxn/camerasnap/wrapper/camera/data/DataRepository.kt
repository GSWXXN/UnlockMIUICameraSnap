package com.gswxxn.camerasnap.wrapper.camera.data

import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.gswxxn.camerasnap.wrapper.camera.data.data.DataItemBase
import com.highcapable.yukihookapi.hook.factory.method

object DataRepository: StaticClass() {
    override val className: String
        get() = "com.android.camera.data.DataRepository"

    fun dataItemGlobal() = clazz.method {
        name = "dataItemGlobal"
        emptyParam()
    }.get().call()!!.let { DataItemBase(it) }

}