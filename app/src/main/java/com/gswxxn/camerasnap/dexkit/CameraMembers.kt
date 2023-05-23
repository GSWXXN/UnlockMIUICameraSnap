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
        lateinit var mVibratorShort: Method

        lateinit var fMCamera: Field
        lateinit var fMPowerManager: Field
        lateinit var fMHandler: Field
    }

    /** SnapService 类中被混淆成员的存储类 **/
    object SnapCameraMembers {
        lateinit var cSnapCamera: Class<*>
    }

    /** 相机配置及设置项中被混淆成员的存储类 **/
    object SettingsMembers {
        lateinit var cCameraSettings: Class<*>

        lateinit var mGetSupportSnap: Method
        lateinit var mGetMiuiSettingsKeyForStreetSnap: Method
    }

}