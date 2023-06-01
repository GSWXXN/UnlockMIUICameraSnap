package com.gswxxn.camerasnap.dexkit.base

import io.luckypray.dexkit.DexKitBridge
import com.gswxxn.camerasnap.hook.base.BaseHookerWithDexKit
import io.luckypray.dexkit.builder.BatchFindArgs
import io.luckypray.dexkit.descriptor.member.DexClassDescriptor
import io.luckypray.dexkit.descriptor.member.DexMethodDescriptor
import io.luckypray.dexkit.enums.MatchType

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
    companion object {
        var finders = mutableListOf<BaseFinder>()
        lateinit var batchFindClassesUsingStringsResultMap: Map<String, List<DexClassDescriptor>>
        lateinit var batchFindMethodsUsingStringsResultMap: Map<String, List<DexMethodDescriptor>>

        fun DexKitBridge.onFinishLoadFinder() {
            batchFindClassesUsingStringsResultMap = batchFindClassesUsingStrings {
                matchType = MatchType.FULL
                finders.forEach { apply(it.prepareBatchFindClassesUsingStrings()) }
            }
            batchFindMethodsUsingStringsResultMap = batchFindMethodsUsingStrings {
                matchType = MatchType.FULL
                finders.forEach { apply(it.prepareBatchFindMethodsUsingStrings()) }
            }
            finders.forEach { it.onFindMembers() }
        }
    }

    lateinit var bridge: DexKitBridge

    open fun prepareBatchFindClassesUsingStrings(): BatchFindArgs.Builder.() -> Unit = {}

    open fun prepareBatchFindMethodsUsingStrings(): BatchFindArgs.Builder.() -> Unit = {}

    abstract fun onFindMembers()
}