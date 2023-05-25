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

    /**
     * 通过 [DexMethodDescriptor] 获取方法实例, 传入参数默认为 [appClassLoader]
     *
     * @return [Method]
     */
    fun DexMethodDescriptor.getMethodInstance() = getMethodInstance(appClassLoader)

    /**
     * 查找给定方法的调用函数, 如果有多个查找到的函数, 则会抛出异常
     *
     * @throws [IllegalArgumentException]
     * @return [Method]
     */
    fun DexKitBridge.uniqueFindMethodInvoking(builder: MethodInvokingArgs.Builder.() -> Unit): Method {
        val invokingList = findMethodInvoking(builder)
        val flatMap = invokingList.flatMap { it.value }

        require(flatMap.size == 1) { "uniqueFindMethodInvoking() Error: invokingList must contain exactly one item; Data: $invokingList" }

        return flatMap.first().getMethodInstance()
    }

    /**
    * 查找调用给定方法的函数, 如果有多个查找到的函数, 则会抛出异常
    *
    * @throws [IllegalArgumentException]
    * @return [Method]
    */
    fun DexKitBridge.uniqueFindMethodCalling(builder: MethodCallerArgs.Builder.() -> Unit): Method {
        val callingList = findMethodCaller(builder)
        val flatMap = callingList.flatMap { it.value }

        require(flatMap.size == 1) { "uniqueFindMethodCalling() Error: callingList must contain exactly one item; Data: $callingList" }

        return flatMap.first().getMethodInstance()
    }

    /**
     * 查找调用给定变量的函数, 如果有多个查找到的函数, 则会抛出异常
     *
     * @throws [IllegalArgumentException]
     * @return [Method]
     */
    fun DexKitBridge.uniqueFindMethodUsingField(builder: MethodUsingFieldArgs.Builder.() -> Unit): Method {
        val usingList = findMethodUsingField(builder).keys

        require(usingList.size == 1) { "uniqueFindMethodUsingField() Error: UsingList must contain exactly one item; Data: $usingList" }

        return usingList.first().getMethodInstance()
    }

    /**
     * 加载 Finder, 将 DexKitBridge 实例传递给 Finder, 并调用 onFindMembers 方法
     *
     * @param finder 被操作的 Finder
     */
    fun DexKitBridge.loadFinder(finder: BaseFinder) {
        finder.bridge = this
        finder.onFindMembers()
    }
}