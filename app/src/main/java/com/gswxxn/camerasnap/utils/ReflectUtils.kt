package com.gswxxn.camerasnap.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageParser
import com.gswxxn.camerasnap.hook.CameraHooker.hook
import com.gswxxn.camerasnap.hook.CameraHooker.toClass
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.type.java.FileClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectUtils {

    /** 设置字段的变量值
     *
     * @param obj 对象
     * @return 成员变量值
     **/
    fun Field.setValue(obj: Any, value: Any) {
        this.isAccessible = true
        this.set(obj, value)
    }

    /** 通过 ApplicationInfo 获取 PackageParser.Package
     *
     * @return PackageParser.Package
     **/
    fun ApplicationInfo.getPackage() = "android.content.pm.PackageParser".toClass()
        .constructor { emptyParam() }.get().call()!!.current()
        .method { name = "parsePackage"; param(FileClass, IntType) }
        .invoke<PackageParser.Package>(File(sourceDir), 0)!!

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