package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker.uniqueFindMethodInvoking
import com.gswxxn.camerasnap.utils.DexKitHelper

/** 配置及设置项被混淆成员的 Finder **/
object SettingsMembersFinder: BaseFinder() {
    override fun onFindMembers() {
        CameraMembers.SettingsMembers.mGetSupportSnap = bridge.uniqueFindMethodInvoking {
            methodDeclareClass = "com.android.camera.CameraSettings"
            methodName = "getCameraSnapSettingNeed"
            methodReturnType = DexKitHelper.TypeSignature.BOOLEAN

            beInvokedMethodReturnType = DexKitHelper.TypeSignature.BOOLEAN
            beInvokedMethodParameterTypes = arrayOf()
        }
    }
}