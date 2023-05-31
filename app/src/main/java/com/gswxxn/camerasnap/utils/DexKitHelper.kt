package com.gswxxn.camerasnap.utils

import io.luckypray.dexkit.DexKitBridge
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