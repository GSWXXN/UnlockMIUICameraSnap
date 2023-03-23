package com.gswxxn.camerasnap.wrapper.camera2

import com.highcapable.yukihookapi.hook.factory.current

class CameraCapabilities(private val instance: Any?) {
    fun getSensorOrientation() = instance?.current()?.method {
        name = "getSensorOrientation"
        emptyParam()
    }!!.invoke<Int>() ?: -1

    fun getFacing() = instance?.current()?.method {
        name = "getFacing"
        emptyParam()
    }!!.invoke<Int>() ?: -1
}