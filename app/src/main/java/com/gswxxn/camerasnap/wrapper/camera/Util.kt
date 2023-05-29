package com.gswxxn.camerasnap.wrapper.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.wrapper.base.StaticClass

object Util: StaticClass() {
    override val className: String
        get() = "com.android.camera.Util"

    fun convertOutputFormatToFileExt(outputFileFormat: Int) = if (outputFileFormat == 2) ".mp4" else ".3gp"

    fun convertOutputFormatToMimeType(outputFileFormat: Int) = if (outputFileFormat == 2) "video/mp4" else "video/3gpp"

    fun getDuration(filePath: String) = CameraMembers.OtherMembers.mGetDuration.invoke(null, filePath) as Long
}