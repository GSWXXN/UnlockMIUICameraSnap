package com.gswxxn.camerasnap.wrapper.base

import com.gswxxn.camerasnap.hook.CameraHooker.appClassLoader
import com.highcapable.yukihookapi.hook.factory.toClass

abstract class StaticClass {
    abstract val className: String
    // 通过类名获取实体类
    val clazz get() = className.toClass(appClassLoader)
}