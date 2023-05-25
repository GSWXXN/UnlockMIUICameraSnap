package com.gswxxn.camerasnap.utils

import java.lang.reflect.Field

object ReflectUtils {
    fun Field.setValue(obj: Any, value: Any) {
        this.isAccessible = true
        this.set(obj, value)
    }
}