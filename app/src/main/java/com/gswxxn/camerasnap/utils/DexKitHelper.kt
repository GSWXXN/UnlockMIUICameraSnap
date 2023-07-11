package com.gswxxn.camerasnap.utils

import android.content.Context
import com.gswxxn.camerasnap.BuildConfig
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.hook.base.BaseHookerWithDexKit
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.type.java.JavaClass
import com.highcapable.yukihookapi.hook.type.java.JavaFieldClass
import com.highcapable.yukihookapi.hook.type.java.JavaMethodClass
import io.luckypray.dexkit.DexKitBridge
import io.luckypray.dexkit.builder.MethodCallerArgs
import io.luckypray.dexkit.builder.MethodInvokingArgs
import io.luckypray.dexkit.builder.MethodUsingFieldArgs
import io.luckypray.dexkit.descriptor.member.DexClassDescriptor
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
     * 加载 Finder, 将 DexKitBridge 实例传递给 Finder
     *
     * @param finder 被操作的 Finder
     */
    fun DexKitBridge.loadFinder(finder: BaseFinder) {
        finder.bridge = this
        BaseFinder.finders += finder
    }

    /**
     * 通过 [DexMethodDescriptor] 获取方法实例, 传入参数默认为 [CameraHooker.appClassLoader]
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

        require(flatMap.size == 1) {
            var builderInfo = ""
            builder.javaClass.declaredFields.forEach {
                it.isAccessible = true
                builderInfo += ("${it.name}: ${it.get(builder)}\n")
            }
            "uniqueFindMethodCalling() Error: callingList must contain exactly one item; Data: $callingList  \n Builder: $builderInfo"
        }

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

        require(usingList.size == 1) {
            var builderInfo = ""
            builder.javaClass.declaredFields.forEach {
                it.isAccessible = true
                builderInfo += ("${it.name}: ${it.get(builder)}\n")
            }
            "uniqueFindMethodUsingField() Error: UsingList must contain exactly one item; Data: $usingList \n Builder: $builderInfo"
        }

        return usingList.first().getMethodInstance()
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

    /**
     * 将对象的成员信息存储到 SharedPreferences 中
     *
     * @param context 上下文对象，用于获取 SharedPreferences
     * @param obj 被存储的对象
     */
    fun BaseHookerWithDexKit.storeMembers(context: Context, obj: Any) {
        context.getSharedPreferences("unlock_miui_camera_snap_anti_obfuscation", Context.MODE_PRIVATE).edit().apply {
            clear()

            appVersionCode?.let { putLong("app_version_code", it) }
            appVersionName?.let { putString("app_version_name", it) }
            putString("module_version_name", BuildConfig.VERSION_NAME)
            putInt("module_version_code", BuildConfig.VERSION_CODE)

            obj.javaClass.declaredClasses.forEach { clazz ->
                val className = clazz.simpleName
                clazz.declaredFields.forEach { field ->
                    val key = "${className}_${field.name}"
                    val value = when (field.type) {
                        JavaClass -> DexClassDescriptor(field.get(null) as Class<*>).descriptor
                        JavaMethodClass -> DexMethodDescriptor(field.get(null) as Method).descriptor
                        JavaFieldClass -> DexFieldDescriptor(field.get(null) as Field).descriptor
                        else -> null
                    }
                    value?.let { putString(key, it) }
                }
            }
        }.apply()
    }

    /**
     * 从 SharedPreferences 中读取对象的成员信息
     *
     * @param context 上下文对象，用于获取 SharedPreferences
     * @param obj 被存储的对象
     * @return 是否成功读取
     */
    fun BaseHookerWithDexKit.loadMembers(context: Context, obj: Any): Boolean {
        val pref = context.getSharedPreferences("unlock_miui_camera_snap_anti_obfuscation", Context.MODE_PRIVATE)

        val isVersionSame = try {
            pref.getLong("app_version_code", 0) == appVersionCode &&
                    pref.getString("app_version_name", "") == appVersionName &&
                    pref.getString("module_version_name", "") == BuildConfig.VERSION_NAME &&
                    pref.getInt("module_version_code", 0) == BuildConfig.VERSION_CODE
        } catch (e: Exception) {
            loggerE(msg = "failed to read app or module versions", e = e)
            return false
        }

        if (!isVersionSame) return false

        obj.javaClass.declaredClasses.forEach { clazz ->
            val className = clazz.simpleName
            for (field in clazz.declaredFields) {
                val key = "${className}_${field.name}"
                val value = pref.getString(key, "")!!

                if (field.type !in arrayOf(JavaClass, JavaMethodClass, JavaFieldClass)) continue

                if (value.isEmpty()) {
                    loggerE(msg = "failed to load ${key}, pref empty")
                    return false
                }

                val instance = try {
                     when (field.type) {
                        JavaClass -> DexClassDescriptor(value).getClassInstance(appClassLoader)
                        JavaMethodClass -> DexMethodDescriptor(value).getMethodInstance(appClassLoader).apply { isAccessible = true }
                        JavaFieldClass -> DexFieldDescriptor(value).getFieldInstance(appClassLoader).apply { isAccessible = true }
                        else -> null
                    }
                } catch (e: ReflectiveOperationException) {
                    loggerE(msg = "failed to load ${key}, no such members", e = e)
                    return false
                }

                field.set(null, instance)
            }
        }
        return true
    }
}