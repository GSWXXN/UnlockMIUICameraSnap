package com.gswxxn.camerasnap.hook

import com.gswxxn.camerasnap.dexkit.camera.CameraSnapMembersFinder
import com.gswxxn.camerasnap.dexkit.camera.CameraTriggerMembersFinder
import com.gswxxn.camerasnap.dexkit.camera.SettingsMembersFinder
import com.gswxxn.camerasnap.hook.base.BaseHookerWithDexKit
import com.gswxxn.camerasnap.hook.camera.CameraSnapHooker
import com.gswxxn.camerasnap.hook.camera.CameraTriggerHooker
import com.gswxxn.camerasnap.hook.camera.SettingsHooker
import io.luckypray.dexkit.DexKitBridge

/** 相机 Hooker**/
object CameraHooker: BaseHookerWithDexKit() {

    /** 开始查找混淆成员 **/
    override fun onFindMembers(bridge: DexKitBridge) {

        bridge.loadFinder(CameraTriggerMembersFinder)

        bridge.loadFinder(CameraSnapMembersFinder)

        bridge.loadFinder(SettingsMembersFinder)
    }

    /** 相机 Hook 入口 **/
    override fun startHook() {

        loadHooker(CameraTriggerHooker)

        loadHooker(CameraSnapHooker)

        loadHooker(SettingsHooker)
    }
}