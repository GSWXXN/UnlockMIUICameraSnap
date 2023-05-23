package com.gswxxn.camerasnap.hook.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.hook.CameraHooker.onFindMembers
import com.gswxxn.camerasnap.wrapper.camera.snap.SnapTrigger
import com.gswxxn.camerasnap.wrapper.camera.statistic.CameraStatUtils
import com.gswxxn.camerasnap.wrapper.camera.storage.Storage
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD

/** CameraTrigger 类的 Hooker **/
object CameraTriggerHooker: YukiBaseHooker() {

    /** 开始 Hook **/
    override fun onHook() {
        /**
         * 在执行街拍前添加对录像模式的处理
         *
         * 原方法是匿名函数, 担心后续位置会有变动, 所以此方法在 [onFindMembers] 中查找
         *
         * 原始类位置为 com.android.camera.snap.SnapTrigger
         */
        CameraMembers.SnapTriggerMembers.mSnapRunner.declaringClass.hook {
            injectMember {
                members(CameraMembers.SnapTriggerMembers.mSnapRunner)
                replaceUnit {
                    val mSnapTrigger = SnapTrigger(field { name = "this\$0" }.get(instance).any()!!)

                    if (mSnapTrigger.mCamera.instance == null || !mSnapTrigger.mCamera.mIsCamcorder) {
                        callOriginal()
                        return@replaceUnit
                    }

                    if (mSnapTrigger.mPowerManager == null || !mSnapTrigger.mPowerManager!!.isInteractive) {
                        if (!mSnapTrigger.shouldQuitSnap() && Storage.getAvailableSpace() >= Storage.LOW_STORAGE_THRESHOLD) {
                            mSnapTrigger.shutdownWatchDog()
                            mSnapTrigger.vibratorShort()
                            mSnapTrigger.mCamera.startCamcorder()
                            loggerD(msg = "take movie")
                            CameraStatUtils.trackSnapInfo(true)
                        }
                    }
                }
            }
        }
    }
}