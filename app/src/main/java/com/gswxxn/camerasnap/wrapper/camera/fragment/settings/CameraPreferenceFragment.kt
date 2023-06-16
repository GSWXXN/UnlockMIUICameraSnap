package com.gswxxn.camerasnap.wrapper.camera.fragment.settings

import android.app.Activity
import android.content.Context
import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.wrapper.preference.PreferenceGroup
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.java.IntType

// 此类下的函数暂未被混淆, 反射名称查找即可, 无需使用DexKit
class CameraPreferenceFragment(private val instance: Any) {

    val mPreferenceGroup get() = PreferenceGroup(
        instance.current().field {
            type = "androidx.preference.PreferenceScreen"
            superClass()
        }.any()!!
    )

    val mFromWhere get() = try {
        instance.javaClass.field {
            name = "mFromWhere"
            type = IntType
            superClass()
        }.ignored().onNoSuchField { throw it }.get(instance).int()
    } catch (e: Throwable) {
        "com.android.camera.fragment.settings.BasePreferenceFragment".toClass(CameraHooker.appClassLoader).field {
            type = IntType
            modifiers { !isStatic }
        }.get(instance).int()
    }

    fun getString(id: Int) = instance.current().method {
        superClass()
        name = "getString"
        param(IntType)
    }.string(id)

    fun getActivity() = instance.current().method {
        superClass()
        name = "getActivity"
    }.invoke<Activity>()!!

    fun getContext() = instance.current().method {
        superClass()
        name = "getContext"
        emptyParam()
    }.invoke<Context>()!!

    fun registerListener() { CameraMembers.OtherMembers.mRegisterListener.invoke(instance, mPreferenceGroup.getInstance(), instance) }
}