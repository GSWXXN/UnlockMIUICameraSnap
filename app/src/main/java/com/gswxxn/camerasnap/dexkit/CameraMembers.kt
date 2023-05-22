package com.gswxxn.camerasnap.dexkit

import java.lang.reflect.Method

/** 相机被混淆成员的存储类 **/
object CameraMembers {
    /** SnapTrigger 类中被混淆成员的存储类 **/
    object SnapTriggerMembers {
        lateinit var mSnapRunner: Method
    }

    /** SnapService 类中被混淆成员的存储类 **/
    object SnapServiceMembers {

    }

    /** 相机配置及设置项中被混淆成员的存储类 **/
    object SettingsMembers {
        lateinit var mGetSupportSnap: Method
    }

}