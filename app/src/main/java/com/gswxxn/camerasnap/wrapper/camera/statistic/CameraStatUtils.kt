package com.gswxxn.camerasnap.wrapper.camera.statistic

import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object CameraStatUtils: StaticClass() {
    override val className: String
        get() = "com.android.camera.statistic.CameraStatUtils"

    fun trackSnapInfo(z: Boolean) = clazz.method {
        name = "trackSnapInfo"
        param(BooleanType)
    }.get().call(z)
}