package com.gswxxn.camerasnap.wrapper.camera

import android.location.Location
import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method

class LocationManager(private val instance: Any) {

    companion object: StaticClass() {
        override val className = "com.android.camera.LocationManager"

        fun instance() = clazz.method {
            name = "instance"
            emptyParam()
        }.get().call()!!.let { LocationManager(it) }
    }

    fun getCurrentLocation() = instance.current().method {
        name = "getCurrentLocation"
        emptyParam()
    }.invoke<Location>()




}