package com.gswxxn.camerasnap.utils

import com.gswxxn.camerasnap.hook.CameraHooker.hook
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
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
     * 判断当前类是否是给定超类的子类或者子接口。
     *
     * @param superClass 要判断的超类
     * @return 如果当前类是给定超类的子类或者子接口，则返回true；否则返回false。
     */
    infix fun Class<*>.isSubClassOf(superClass: Class<*>): Boolean {
        var currentClass: Class<*>? = this
        while (currentClass != null) {
            if (currentClass == superClass) {
                return true
            }
            currentClass = currentClass.superclass
        }
        return false
    }
}