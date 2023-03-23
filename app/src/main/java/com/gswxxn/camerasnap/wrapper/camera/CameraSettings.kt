package com.gswxxn.camerasnap.wrapper.camera

import com.gswxxn.camerasnap.constant.Key
import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.IntType

object CameraSettings: StaticClass() {
    override val className: String
        get() = "com.android.camera.CameraSettings"

    val KEY_CAMERA_SNAP = clazz.field { name = "KEY_CAMERA_SNAP" }.get().string()

    fun getPreferVideoQuality(cameraId: Int, defaultQuality: Int) = clazz.method {
        name = "getPreferVideoQuality"
        param(IntType, IntType)
    }.get().int(cameraId, defaultQuality)

    private fun getString(i: Int) = clazz.method {
        name = "getString"
        param(IntType)
    }.get().string(i)

    /**
     * 根据下拉框名称获取对应街拍模式的设置键
     *
     * @param str 下拉框选中名称
     * @return [String] 对应的设置键
     */
    fun getMiuiSettingsKeyForStreetSnap(str: String) = when (str) {
        getString(R.string.pref_camera_snap_value_take_picture) ->
            Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_PICTURE
        getString(R.string.pref_camera_snap_value_take_movie) ->
            Key.LONG_PRESS_VOLUME_DOWN_STREET_SNAP_MOVIE
        else -> Key.LONG_PRESS_VOLUME_DOWN_DEFAULT
    }
}