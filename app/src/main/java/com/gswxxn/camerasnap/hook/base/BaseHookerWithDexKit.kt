package com.gswxxn.camerasnap.hook.base

import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import io.luckypray.dexkit.DexKitBridge
import io.luckypray.dexkit.builder.MethodCallerArgs
import io.luckypray.dexkit.builder.MethodInvokingArgs
import io.luckypray.dexkit.builder.MethodUsingFieldArgs
import io.luckypray.dexkit.descriptor.member.DexMethodDescriptor
import java.lang.reflect.Method

/** 使用 DexKit 的 Base Hooker, 在需要的作用域 Hooker 入口继承使用 **/
abstract class BaseHookerWithDexKit: YukiBaseHooker() {

    override fun onHook() {
        System.loadLibrary("dexkit")

        DexKitBridge.create(appInfo.sourceDir)?.use { bridge ->
            onFindMembers(bridge)
        }

        startHook()
    }

    abstract fun onFindMembers(bridge: DexKitBridge)

    abstract fun startHook()

    /**
     * 对给定 [Method] 执行 Hook
     *
     * @param block 要执行的 [Unit]
     */
    fun Method.methodHook(block: YukiMemberHookCreator.MemberHookCreator.() -> Unit) {
        declaringClass.hook {
            injectMember {
                members(this@methodHook)
                block()
            }
        }
    }


}