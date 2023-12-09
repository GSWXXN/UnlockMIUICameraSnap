package com.gswxxn.camerasnap.hook.camera

import android.app.PendingIntent
import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.hook.CameraHooker.onFindMembers
import com.gswxxn.camerasnap.wrapper.camera.snap.SnapTrigger
import com.gswxxn.camerasnap.wrapper.camera.statistic.CameraStatUtils
import com.gswxxn.camerasnap.wrapper.camera.storage.Storage
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.IntentClass
import com.highcapable.yukihookapi.hook.type.java.IntType

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
        CameraMembers.SnapTriggerMembers.mSnapRunner.hook {
            replaceUnit {
                val mSnapTrigger = SnapTrigger(instance.current().field { type = CameraMembers.SnapTriggerMembers.cSnapTrigger }.any()!!)

                if (mSnapTrigger.mCamera.instance == null || !mSnapTrigger.mCamera.mIsCamcorder) {
                    callOriginal()
                    return@replaceUnit
                }

                if (mSnapTrigger.mPowerManager == null || !mSnapTrigger.mPowerManager!!.isInteractive) {
                    if (!mSnapTrigger.shouldQuitSnap() && Storage.getAvailableSpace() >= Storage.LOW_STORAGE_THRESHOLD) {
                        mSnapTrigger.shutdownWatchDog()
                        mSnapTrigger.vibratorShort()
                        mSnapTrigger.mCamera.startCamcorder()
                        YLog.debug(msg = "take movie")
                        CameraStatUtils.trackSnapInfo(true)
                    }
                }
            }
        }

        /**
         * 安卓 S 以上版本, 需要在 [android.app.PendingIntent.getActivity] 方法中添加标记,
         * 但是 MIUI 相机并没有添加, 这会导致街拍过后准备发送通知时会抛出异常, 通知不能正常发出
         */
        PendingIntent::class.java.method {
            name = "getActivity"
            param(ContextClass, IntType, IntentClass, IntType)
        }.hook {
            before {
                if (appInfo.targetSdkVersion < 31) return@before

                val flag = args[3] as Int
                if (flag == 0)
                    args[3] = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            }
        }
    }
}