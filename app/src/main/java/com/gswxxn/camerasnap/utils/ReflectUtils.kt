package com.gswxxn.camerasnap.utils

import java.lang.reflect.Field

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