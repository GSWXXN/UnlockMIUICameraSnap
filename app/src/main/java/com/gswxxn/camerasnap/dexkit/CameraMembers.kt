package com.gswxxn.camerasnap.dexkit

import java.lang.reflect.Field
import java.lang.reflect.Method

/** 相机被混淆成员的存储类 **/
object CameraMembers {
    /** SnapTrigger 类中被混淆成员的存储类 **/
    object SnapTriggerMembers {
        lateinit var cSnapTrigger: Class<*>

        lateinit var mSnapRunner: Method
        lateinit var mShouldQuitSnap: Method
        lateinit var mVibrator: Method

        lateinit var fMCamera: Field
        lateinit var fMPowerManager: Field
        lateinit var fMHandler: Field
    }

    /** SnapService 类中被混淆成员的存储类 **/
    object SnapCameraMembers {
        lateinit var cSnapCamera: Class<*>

        lateinit var mInitSnapType: Method
        lateinit var mPlaySound: Method
        lateinit var mRelease: Method

        lateinit var fIsCamcorder: Field
        lateinit var fMCameraId: Field
        lateinit var fMCameraDevice: Field
        lateinit var fMCameraCapabilities: Field
        lateinit var fMOrientation: Field
        lateinit var fMCameraHandler: Field
        lateinit var fMContext: Field
        lateinit var fMStatusListener: Field
    }

    /** 相机配置及设置项中被混淆成员的存储类 **/
    object SettingsMembers {
        lateinit var cCameraSettings: Class<*>

        lateinit var mGetSupportSnap: Method
        lateinit var mGetMiuiSettingsKeyForStreetSnap: Method
        lateinit var mGetPreferVideoQuality: Method
    }

    /** 其他被混淆成员的存储类 **/
    object OtherMembers {
        // CameraUtils 类中被混淆成员的存储类
        lateinit var mTrackSnapInfo: Method

        // MediaProviderUtil 类中被混淆成员的存储类
        lateinit var mGetContentUriFromPath: Method

        // Storage 类中被混淆成员的存储类
        lateinit var mGetAvailableSpace: Method

        // LocationManager 类中被混淆成员的存储类
        lateinit var mInstance: Method
        lateinit var mGetCurrentLocation: Method

        // Util 类中被混淆成员的存储类
        lateinit var mGetDuration: Method

        // CameraCapabilities 类中被混淆成员的存储类
        lateinit var mGetSensorOrientation: Method
        lateinit var mGetFacing: Method

    }

}