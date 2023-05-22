package com.gswxxn.camerasnap.dexkit.base

import io.luckypray.dexkit.DexKitBridge
import com.gswxxn.camerasnap.hook.base.BaseHookerWithDexKit

/**
 * 使用 DexKit 查找混淆字段的基类
 *
 * 在 [BaseHookerWithDexKit.onFindMembers] 方法中使用
 *
 * 使用方法:
 *
 *     override fun onFindMembers(bridge: DexKitBridge) {
 *         bridge.loadFinder(CameraTriggerMembersFinder)
 *     }
 *
 *
 * @property bridge [DexKitBridge] 在不要要手动赋值, 调用 DexKitBridge.loadFinder 时会自动被赋值
 */
abstract class BaseFinder {
    lateinit var bridge: DexKitBridge

    abstract fun onFindMembers()
}