package com.gswxxn.camerasnap.utils

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker
import io.luckypray.dexkit.DexKitBridge
import io.luckypray.dexkit.builder.MethodCallerArgs
import io.luckypray.dexkit.builder.MethodInvokingArgs
import io.luckypray.dexkit.builder.MethodUsingFieldArgs
import io.luckypray.dexkit.descriptor.member.DexFieldDescriptor
import io.luckypray.dexkit.descriptor.member.DexMethodDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Method

/** DexKit 工具类 **/
object DexKitHelper {

    /** 字段签名 **/
    object TypeSignature {
        const val VOID = "V"
        const val BOOLEAN = "Z"
        const val BYTE = "B"
        const val CHAR = "C"
        const val SHORT = "S"
        const val INT = "I"
        const val FLOAT = "F"
        const val LONG = "J"
        const val DOUBLE = "D"
        const val STRING = "Ljava/lang/String;"
        const val URI = "Landroid/net/Uri;"
        const val LOCATION = "Landroid/location/Location;"
        const val CameraCharacteristics = "Landroid/hardware/camera2/CameraCharacteristics;"
    }

    /**
     * 通过 [DexMethodDescriptor] 获取方法实例, 传入参数默认为 [appClassLoader]
     *
     * @return [Method]
     */
    fun DexMethodDescriptor.getMethodInstance() = getMethodInstance(CameraHooker.appClassLoader).apply { isAccessible = true }

    /**
     * 查找给定方法的调用函数, 如果有多个查找到的函数, 则会抛出异常
     *
     * @throws [IllegalArgumentException]
     * @return [Method]
     */
    fun DexKitBridge.uniqueFindMethodInvoking(builder: MethodInvokingArgs.Builder.() -> Unit): Method {
        val invokingList = findMethodInvoking(builder)
        val flatMap = invokingList.flatMap { it.value }

        require(flatMap.size == 1) {
            var builderInfo = ""
            builder.javaClass.declaredFields.forEach {
                it.isAccessible = true
                builderInfo += ("${it.name}: ${it.get(builder)}\n")
            }
            "uniqueFindMethodInvoking() Error: invokingList must contain exactly one item; Data: $invokingList \n Builder: $builderInfo"

        }

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

    /**
     * 给定字段及方法, 查找指定方法具体使用的一个字段, 如果有多个查找到的字段, 则会抛出异常
     *
     * @throws [AssertionError]
     * @Param [fields] 字段数组
     * @Param [method] 方法
     **/
    fun DexKitBridge.findFieldUsingByMethod(fields: List<Field>, method: Method): Field {
        val result = mutableListOf<Field>()
        for (field in fields) {
            val findMethodUsingFieldResult = findMethodUsingField {
                fieldDescriptor = DexFieldDescriptor(field).descriptor
                callerMethodDescriptor = DexMethodDescriptor(method).descriptor
            }

            if (findMethodUsingFieldResult.isNotEmpty()) {
                result += field
            }
        }
        assert(result.size == 1) {
            "findFieldUsingByMethod: result.size != 1, method: ${method.declaringClass} -> ${method.name}(), result: ${result.size}"
        }
        return result[0].apply { isAccessible = true }
    }
}