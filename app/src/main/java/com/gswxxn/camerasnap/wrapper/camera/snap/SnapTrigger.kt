package com.gswxxn.camerasnap.wrapper.camera.snap

import android.os.Handler
import android.os.PowerManager
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.log.loggerD

class SnapTrigger(private val instance: Any) {


    companion object {
        const val MAX_VIDEO_DURATION = 0
    }

    val mCamera get() = SnapCamera.getWrapper(instance.current().field { name = "mCamera" }.any())
    val mPowerManager get() = instance.current().field { name = "mPowerManager" }.cast<PowerManager>()
    private val mHandler get() = instance.current().field { name = "mHandler" }.cast<Handler>()

    fun shouldQuitSnap() = instance.current().method {
        name = "shouldQuitSnap"
        emptyParam()
    }.boolean()

    fun shutdownWatchDog() {
        if (this.mHandler != null) {
            loggerD(msg = "watch dog Off")
            this.mHandler!!.removeMessages(101)
        }
    }

    fun vibratorShort() = instance.current().method {
        name = "vibratorShort"
        emptyParam()
    }.call()

}