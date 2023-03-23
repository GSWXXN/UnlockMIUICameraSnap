package com.gswxxn.camerasnap.wrapper.camera

import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.highcapable.yukihookapi.hook.factory.field

object R {
    object string: StaticClass() {
        override val className: String
            get() = "com.android.camera.R\$string"

        val pref_camera_snap_default = clazz.field { name = "pref_camera_snap_default" }.get().int()

        val pref_camera_snap_enable_title = clazz.field { name = "pref_camera_snap_enable_title" }.get().int()

        val pref_camera_snap_value_off = clazz.field { name = "pref_camera_snap_value_off" }.get().int()

        val pref_camera_snap_value_take_picture = clazz.field { name = "pref_camera_snap_value_take_picture" }.get().int()

        val pref_camera_snap_value_take_movie = clazz.field { name = "pref_camera_snap_value_take_movie" }.get().int()
    }

    object array: StaticClass() {
        override val className: String
            get() = "com.android.camera.R\$array"

        val pref_camera_snap_entries = clazz.field { name = "pref_camera_snap_entries" }.get().int()

        val pref_camera_snap_entryvalues = clazz.field { name = "pref_camera_snap_entryvalues" }.get().int()
    }
}