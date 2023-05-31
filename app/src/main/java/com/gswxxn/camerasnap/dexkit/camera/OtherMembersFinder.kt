package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.utils.DexKitHelper.getMethodInstance
import com.gswxxn.camerasnap.utils.DexKitHelper
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodCalling
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodInvoking
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodUsingField
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import io.luckypray.dexkit.enums.MatchType

object OtherMembersFinder: BaseFinder() {
    override fun onFindMembers() {
        val batchFindMethodsUsingStringsResultMap = bridge.batchFindMethodsUsingStrings {
            addQuery("mSnapRunnable_run", arrayOf("isScreenOn is true, stop take snap"))
            addQuery("getContentUriFromPath", arrayOf("_id", "media_type", "external"))
            addQuery("initCamera", arrayOf("initCamera: "))
            addQuery("getCurrentLocationDirectly", arrayOf("No location received yet. cache location is "))
            addQuery("getDuration", arrayOf("getDuration Exception"))

            matchType = MatchType.FULL
        }
        val batchFindClassesUsingStringsResultMap = bridge.batchFindClassesUsingStrings {
            addQuery("LocationManager", arrayOf("No location received yet. cache location is "))
            addQuery("CameraCapabilities", arrayOf("Screen light brightness: "))
        }

        val runDescriptor = batchFindMethodsUsingStringsResultMap["mSnapRunnable_run"]!!.first {
            it.parameterTypesSig == "" && it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }

        CameraMembers.OtherMembers.mTrackSnapInfo = bridge.uniqueFindMethodInvoking {
            methodDescriptor = runDescriptor.descriptor

            beInvokedMethodParameterTypes = arrayOf(
                DexKitHelper.TypeSignature.BOOLEAN
            )
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.VOID
        }

        CameraMembers.OtherMembers.mGetContentUriFromPath = batchFindMethodsUsingStringsResultMap["getContentUriFromPath"]!!.first {
            it.returnTypeSig == DexKitHelper.TypeSignature.URI
        }.getMethodInstance()

        CameraMembers.OtherMembers.mGetAvailableSpace = bridge.uniqueFindMethodInvoking {
            methodDescriptor = runDescriptor.descriptor

            beInvokedMethodParameterTypes = arrayOf()
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.LONG
        }

        val locationManagerClassDescriptor = batchFindClassesUsingStringsResultMap["LocationManager"]!!.first()
        CameraMembers.OtherMembers.mInstance = locationManagerClassDescriptor.name.toClass(CameraHooker.appClassLoader).method {
            returnType = locationManagerClassDescriptor.name
        }.give()!!


        val getCurrentLocationDirectlyMethodDescriptor = batchFindMethodsUsingStringsResultMap["getCurrentLocationDirectly"]!!.first {
            it.parameterTypesSig == "" && it.returnTypeSig == DexKitHelper.TypeSignature.LOCATION
        }
        CameraMembers.OtherMembers.mGetCurrentLocation = bridge.uniqueFindMethodCalling {
            methodDescriptor = getCurrentLocationDirectlyMethodDescriptor.descriptor

            callerMethodDeclareClass = locationManagerClassDescriptor.name
            callerMethodParameterTypes = arrayOf()
            callerMethodReturnType = DexKitHelper.TypeSignature.LOCATION
        }

        CameraMembers.OtherMembers.mGetDuration = batchFindMethodsUsingStringsResultMap["getDuration"]!!.first {
            it.parameterTypesSig == DexKitHelper.TypeSignature.STRING && it.returnTypeSig == DexKitHelper.TypeSignature.LONG
        }.getMethodInstance()


        val cameraCapabilitiesClassDescriptor = batchFindClassesUsingStringsResultMap["CameraCapabilities"]!!.first()
        CameraMembers.OtherMembers.mGetSensorOrientation = bridge.uniqueFindMethodUsingField {
            fieldDeclareClass = DexKitHelper.TypeSignature.CameraCharacteristics
            fieldName = "SENSOR_ORIENTATION"

            callerMethodDeclareClass = cameraCapabilitiesClassDescriptor.name
            callerMethodReturnType = DexKitHelper.TypeSignature.INT
            callerMethodParamTypes = arrayOf()
        }

        CameraMembers.OtherMembers.mGetFacing = bridge.uniqueFindMethodUsingField {
            fieldDeclareClass = DexKitHelper.TypeSignature.CameraCharacteristics
            fieldName = "LENS_FACING"

            callerMethodDeclareClass = cameraCapabilitiesClassDescriptor.name
            callerMethodReturnType = DexKitHelper.TypeSignature.INT
            callerMethodParamTypes = arrayOf()
        }
    }
}