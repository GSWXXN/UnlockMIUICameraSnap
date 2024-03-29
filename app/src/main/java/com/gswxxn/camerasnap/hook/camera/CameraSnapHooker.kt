package com.gswxxn.camerasnap.hook.camera

import android.provider.Settings
import com.gswxxn.camerasnap.constant.Key
import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.wrapper.camera.snap.SnapCamera
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog

/** CameraSnap 类的 Hooker **/
object CameraSnapHooker: YukiBaseHooker() {
    override fun onHook() {
        /**
         * 替换街拍时的模式获取, 原方法无论街拍配置为何值, 均禁用录像模式
         */
        CameraMembers.SnapCameraMembers.mInitSnapType.hook {
            replaceUnit {
                val snapCamera = SnapCamera.getWrapper(instance)

                val snapType = Settings.Secure.getString(
                    snapCamera.mContext.contentResolver,
                    Key.LONG_PRESS_VOLUME_DOWN
                )
                snapCamera.mIsCamcorder =
                    snapType == Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_MOVIE

            }
        }

        /**
         * 移除街拍时的声音
         */
        CameraMembers.SnapCameraMembers.mPlaySound.hook { intercept() }

        /**
         * 添加街拍结束后的视频资源释放, 原方法未对街拍录像模式进行处理
         */
        CameraMembers.SnapCameraMembers.mRelease.hook {
            before {
                try {
                    YLog.warn(msg = "stopCamcorder: release")
                    SnapCamera.getWrapper(instance).stopCamcorder()
                    SnapCamera.removeWrapper(instance)
                } catch (e: Exception) {
                    YLog.error(msg = "release: ${e.message}", e = e)
                }
            }
        }
    }
}