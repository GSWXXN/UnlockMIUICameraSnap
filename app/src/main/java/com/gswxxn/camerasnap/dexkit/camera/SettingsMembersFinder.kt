package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.hook.CameraHooker.uniqueFindMethodInvoking
import com.gswxxn.camerasnap.hook.CameraHooker.getMethodInstance
import com.gswxxn.camerasnap.utils.DexKitHelper
import com.highcapable.yukihookapi.hook.factory.toClass
import io.luckypray.dexkit.annotations.DexKitExperimentalApi
import io.luckypray.dexkit.enums.MatchType

/** 配置及设置项被混淆成员的 Finder **/
object SettingsMembersFinder: BaseFinder() {

    override fun onFindMembers() {
        if (CameraHooker.versionCode < 500000000) {
            oldMembers()
        } else {
            newMembers()
        }
        commonSearch()
    }

    /** 查找旧版相机(5.0 以下)的方法 **/
    private fun oldMembers() {
        // 未被混淆
        CameraMembers.SettingsMembers.cCameraSettings = "com.android.camera.CameraSettings".toClass(CameraHooker.appClassLoader)

        CameraMembers.SettingsMembers.mGetSupportSnap = bridge.uniqueFindMethodInvoking {
            methodDeclareClass = "com.android.camera.CameraSettings"
            methodName = "getCameraSnapSettingNeed"
            methodReturnType = DexKitHelper.TypeSignature.BOOLEAN

            beInvokedMethodReturnType = DexKitHelper.TypeSignature.BOOLEAN
            beInvokedMethodParameterTypes = arrayOf()
        }
    }

    /** 查找新版相机(5.0 及以上)的方法 **/
    @OptIn(DexKitExperimentalApi::class)
    private fun newMembers() {
        val batchFindClassesUsingStringsResultMap = bridge.batchFindClassesUsingStrings {
            addQuery("CameraSettings", arrayOf("filterByConfig: isSupportVideoFrontMirror = "))
            matchType = MatchType.FULL
        }
        val batchFindMethodsUsingStringsResultMap = bridge.batchFindMethodsUsingStrings {
            addQuery("initCamera", arrayOf("initCamera: "))
        }

        // 混淆前类名 com.android.camera.CameraSettings
        CameraMembers.SettingsMembers.cCameraSettings = batchFindClassesUsingStringsResultMap["CameraSettings"]!!.first().name.toClass()


        val getCameraSnapSettingNeedDescriptor = bridge.findMethodUsingAnnotation {
            annotationUsingString = "isSupportedQuickSnap"
        }.first { it.parameterTypesSig == DexKitHelper.TypeSignature.INT + DexKitHelper.TypeSignature.BOOLEAN }

        CameraMembers.SettingsMembers.mGetSupportSnap = bridge.uniqueFindMethodInvoking {
            methodDeclareClass = getCameraSnapSettingNeedDescriptor.declaringClassName
            methodName = getCameraSnapSettingNeedDescriptor.name
            methodReturnType = DexKitHelper.TypeSignature.BOOLEAN

            beInvokedMethodReturnType = DexKitHelper.TypeSignature.BOOLEAN
            beInvokedMethodParameterTypes = arrayOf()
        }

        val initCameraDescriptor = batchFindMethodsUsingStringsResultMap["initCamera"]!!.first {
            it.parameterTypesSig == "" && it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }
        val cameraSettingsClassDescriptor = batchFindClassesUsingStringsResultMap["CameraSettings"]!!.first()
        CameraMembers.SettingsMembers.mGetPreferVideoQuality = bridge.uniqueFindMethodInvoking {
            methodDescriptor = initCameraDescriptor.descriptor

            beInvokedMethodDeclareClass = cameraSettingsClassDescriptor.name
            beInvokedMethodParameterTypes = arrayOf(DexKitHelper.TypeSignature.INT, DexKitHelper.TypeSignature.INT)
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.INT
        }
    }

    private fun commonSearch() {
        val batchFindMethodsUsingStringsResultMap = bridge.batchFindMethodsUsingStrings {
            addQuery("getMiuiSettingsKeyForStreetSnap", arrayOf("none"))
            matchType = MatchType.FULL
        }

        CameraMembers.SettingsMembers.mGetMiuiSettingsKeyForStreetSnap =
            batchFindMethodsUsingStringsResultMap["getMiuiSettingsKeyForStreetSnap"]!!.first {
                it.declaringClassName == CameraMembers.SettingsMembers.cCameraSettings.name &&
                        it.returnTypeSig == DexKitHelper.TypeSignature.STRING &&
                        it.parameterTypesSig == DexKitHelper.TypeSignature.STRING
            }.getMethodInstance()
    }
}