package com.gswxxn.camerasnap.hook

import com.gswxxn.camerasnap.dexkit.camera.CameraSnapMembersFinder
import com.gswxxn.camerasnap.dexkit.camera.CameraTriggerMembersFinder
import com.gswxxn.camerasnap.dexkit.camera.OtherMembersFinder
import com.gswxxn.camerasnap.dexkit.camera.SettingsMembersFinder
import com.gswxxn.camerasnap.hook.base.BaseHookerWithDexKit
import com.gswxxn.camerasnap.hook.camera.CameraSnapHooker
import com.gswxxn.camerasnap.hook.camera.CameraTriggerHooker
import com.gswxxn.camerasnap.hook.camera.SettingsHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.log.loggerI
import com.highcapable.yukihookapi.hook.type.java.FileClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import io.luckypray.dexkit.DexKitBridge
import java.io.File


/** 相机 Hooker**/
object CameraHooker: BaseHookerWithDexKit() {
    // TODO: 移除版本判断, 全部使用 Dexkit 查找成员, 并且保存查找结果
    val versionCode by lazy {
        val pkg = "android.content.pm.PackageParser".toClass()
            .constructor { emptyParam() }.get().call()!!.current()
            .method { name = "parsePackage"; param(FileClass, IntType) }
            .call(File(appInfo.sourceDir), 0)!!

        pkg.current().field { name = "mVersionCode" }.int().apply {
            loggerI(msg = "camera version code: $this")
        }

        5000000
    }

    /** 开始查找混淆成员 **/
    override fun onFindMembers(bridge: DexKitBridge) {

        // TODO: 解两个 Finder 的耦合
        // CameraSnapMembersFinder 一定要在 CameraTriggerMembersFinder之前调用,
        // CameraTriggerMembersFinder 的其中一个查找需要用到 CameraSnapMembersFinder 的结果
        bridge.loadFinder(CameraSnapMembersFinder)
        bridge.loadFinder(CameraTriggerMembersFinder)

        bridge.loadFinder(SettingsMembersFinder)

        bridge.loadFinder(OtherMembersFinder)
    }

    /** 相机 Hook 入口 **/
    override fun startHook() {

        loadHooker(CameraTriggerHooker)

        loadHooker(CameraSnapHooker)

        loadHooker(SettingsHooker)
    }
}