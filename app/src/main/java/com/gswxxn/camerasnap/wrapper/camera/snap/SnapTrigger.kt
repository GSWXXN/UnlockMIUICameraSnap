package com.gswxxn.camerasnap.wrapper.camera.snap

import android.os.Handler
import android.os.PowerManager
import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.highcapable.yukihookapi.hook.log.YLog

class SnapTrigger(private val instance: Any) {


    companion object {
        const val MAX_VIDEO_DURATION = 3600000
    }

    val mCamera get() = SnapCamera.getWrapper(CameraMembers.SnapTriggerMembers.fMCamera.get(instance))
    val mPowerManager get() = CameraMembers.SnapTriggerMembers.fMPowerManager.get(instance) as PowerManager?
    private val mHandler get() = CameraMembers.SnapTriggerMembers.fMHandler.get(instance) as Handler?

    fun shouldQuitSnap() = CameraMembers.SnapTriggerMembers.mShouldQuitSnap.invoke(instance) as Boolean
    fun vibratorShort() { CameraMembers.SnapTriggerMembers.mVibrator.invoke(instance, longArrayOf(10, 20)) }


    fun shutdownWatchDog() {
        if (this.mHandler != null) {
            YLog.debug(msg = "watch dog Off")
            this.mHandler!!.removeMessages(101)
        }
    }
}