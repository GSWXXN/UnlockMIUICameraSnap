package com.gswxxn.camerasnap.hook.base

import com.gswxxn.camerasnap.dexkit.base.BaseFinder.Companion.onFinishLoadFinder
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import io.luckypray.dexkit.DexKitBridge

/** 使用 DexKit 的 Base Hooker, 在需要的作用域 Hooker 入口继承使用 **/
abstract class BaseHookerWithDexKit: YukiBaseHooker() {

    override fun onHook() {
        System.loadLibrary("dexkit")

        DexKitBridge.create(appInfo.sourceDir)?.use { bridge ->
            onFindMembers(bridge)
            bridge.onFinishLoadFinder()
        }

        startHook()
    }

    abstract fun onFindMembers(bridge: DexKitBridge)

    abstract fun startHook()

}