package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker.getMethodInstance
import com.gswxxn.camerasnap.utils.DexKitHelper
import io.luckypray.dexkit.enums.MatchType

/** CameraTrigger 类被混淆成员的 Finder **/
object CameraTriggerMembersFinder: BaseFinder() {

    /** 开始查找成员 **/
    override fun onFindMembers() {
        val batchFindMethodsUsingStringsResultMap = bridge.batchFindMethodsUsingStrings {
            addQuery("mSnapRunner", arrayOf("take snap"))
            matchType = MatchType.CONTAINS
        }


        CameraMembers.SnapTriggerMembers.mSnapRunner =
            batchFindMethodsUsingStringsResultMap["mSnapRunner"]!!.first {
                it.parameterTypesSig == "" && it.returnTypeSig == DexKitHelper.TypeSignature.VOID
            }.getMethodInstance()
    }
}