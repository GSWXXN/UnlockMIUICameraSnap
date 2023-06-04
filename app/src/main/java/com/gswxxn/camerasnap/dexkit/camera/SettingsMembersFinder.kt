package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.dexkit.const.CameraQueryKey
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.utils.DexKitHelper.getMethodInstance
import com.gswxxn.camerasnap.utils.DexKitHelper
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodInvoking
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import io.luckypray.dexkit.annotations.DexKitExperimentalApi
import io.luckypray.dexkit.builder.BatchFindArgs
import io.luckypray.dexkit.descriptor.member.DexMethodDescriptor
import java.lang.Exception

/** 配置及设置项被混淆成员的 Finder **/
object SettingsMembersFinder: BaseFinder() {

    override fun prepareBatchFindClassesUsingStrings(): BatchFindArgs.Builder.() -> Unit = {
        addQuery(CameraQueryKey.CameraSettings, arrayOf("filterByConfig: isSupportVideoFrontMirror = "))
    }

    override fun prepareBatchFindMethodsUsingStrings(): BatchFindArgs.Builder.() -> Unit = {
        addQuery(CameraQueryKey.UserRecordSetting_getQuality, arrayOf("getQuality: quality = "))
        addQuery(CameraQueryKey.CameraSettings_getMiuiSettingsKeyForStreetSnap, arrayOf("none"))
    }

    @OptIn(DexKitExperimentalApi::class)
    override fun onFindMembers() {

        // 混淆前类名 com.android.camera.CameraSettings
        CameraMembers.SettingsMembers.cCameraSettings = batchFindClassesUsingStringsResultMap[CameraQueryKey.CameraSettings]!!.first().name.toClass(CameraHooker.appClassLoader)

        val getCameraSnapSettingNeedDescriptor = try {
            bridge.findMethodUsingAnnotation {
                annotationUsingString = "isSupportedQuickSnap"
            }.first { it.parameterTypesSig == DexKitHelper.TypeSignature.INT + DexKitHelper.TypeSignature.BOOLEAN }
        } catch (e: Exception) {
            "com.android.camera.CameraSettings".toClass(CameraHooker.appClassLoader).method {
                name = "getCameraSnapSettingNeed"
                param(IntType, BooleanType)
            }.give()!!.let { DexMethodDescriptor(it) }
        }

        CameraMembers.SettingsMembers.mGetSupportSnap = bridge.uniqueFindMethodInvoking {
            methodDeclareClass = getCameraSnapSettingNeedDescriptor.declaringClassName
            methodName = getCameraSnapSettingNeedDescriptor.name
            methodReturnType = DexKitHelper.TypeSignature.BOOLEAN

            beInvokedMethodReturnType = DexKitHelper.TypeSignature.BOOLEAN
            beInvokedMethodParameterTypes = arrayOf()
        }

        val getQualityDescriptor = batchFindMethodsUsingStringsResultMap[CameraQueryKey.UserRecordSetting_getQuality]!!.first {
            it.returnTypeSig == DexKitHelper.TypeSignature.INT
        }
        val cameraSettingsClassDescriptor = batchFindClassesUsingStringsResultMap[CameraQueryKey.CameraSettings]!!.first()
        CameraMembers.SettingsMembers.mGetPreferVideoQuality = bridge.uniqueFindMethodInvoking {
            methodDescriptor = getQualityDescriptor.descriptor

            beInvokedMethodDeclareClass = cameraSettingsClassDescriptor.name
            beInvokedMethodParameterTypes = arrayOf(DexKitHelper.TypeSignature.INT, DexKitHelper.TypeSignature.INT)
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.INT
        }

        CameraMembers.SettingsMembers.mGetMiuiSettingsKeyForStreetSnap =
            batchFindMethodsUsingStringsResultMap[CameraQueryKey.CameraSettings_getMiuiSettingsKeyForStreetSnap]!!.first {
                it.declaringClassName == CameraMembers.SettingsMembers.cCameraSettings.name &&
                        it.returnTypeSig == DexKitHelper.TypeSignature.STRING &&
                        it.parameterTypesSig == DexKitHelper.TypeSignature.STRING
            }.getMethodInstance()
    }
}