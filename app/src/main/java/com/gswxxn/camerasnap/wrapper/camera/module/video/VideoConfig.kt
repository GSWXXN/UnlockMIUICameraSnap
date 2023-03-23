package com.gswxxn.camerasnap.wrapper.camera.module.video

import com.gswxxn.camerasnap.wrapper.base.StaticClass
import com.highcapable.yukihookapi.hook.factory.field

object VideoConfig: StaticClass() {
    override val className: String
        get() = "com.android.camera.module.video.VideoConfig"

    val VIDEO_MIN_SINGLE_FILE_SIZE = clazz.field { name = "VIDEO_MIN_SINGLE_FILE_SIZE" }.get().long()
}