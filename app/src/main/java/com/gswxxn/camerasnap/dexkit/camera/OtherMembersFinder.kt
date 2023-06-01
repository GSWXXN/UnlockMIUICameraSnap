package com.gswxxn.camerasnap.dexkit.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.dexkit.base.BaseFinder
import com.gswxxn.camerasnap.dexkit.const.CameraQueryKey
import com.gswxxn.camerasnap.hook.CameraHooker
import com.gswxxn.camerasnap.utils.DexKitHelper.getMethodInstance
import com.gswxxn.camerasnap.utils.DexKitHelper
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodCalling
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodInvoking
import com.gswxxn.camerasnap.utils.DexKitHelper.uniqueFindMethodUsingField
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import io.luckypray.dexkit.builder.BatchFindArgs

object OtherMembersFinder: BaseFinder() {

    override fun prepareBatchFindClassesUsingStrings(): BatchFindArgs.Builder.() -> Unit = {
        addQuery(CameraQueryKey.LocationManager, arrayOf("No location received yet. cache location is "))
        addQuery(CameraQueryKey.CameraCapabilities, arrayOf("addStreamConfigurationToList: but the key is null!"))
    }

    override fun prepareBatchFindMethodsUsingStrings(): BatchFindArgs.Builder.() -> Unit = {
        addQuery(CameraQueryKey.SnapTrigger_mSnapRunner, arrayOf("isScreenOn is true, stop take snap"))
        addQuery(CameraQueryKey.MediaProviderUtil_getContentUriFromPath, arrayOf("_id", "media_type", "external"))
        addQuery(CameraQueryKey.SnapCamera_initCamera, arrayOf("initCamera: "))
        addQuery(CameraQueryKey.LocationManager_getCurrentLocationDirectly, arrayOf("No location received yet. cache location is "))
        addQuery(CameraQueryKey.Util_getDuration, arrayOf("getDuration Exception"))
    }

    override fun onFindMembers() {

        val runDescriptor = batchFindMethodsUsingStringsResultMap[CameraQueryKey.SnapTrigger_mSnapRunner]!!.first {
            it.parameterTypesSig == "" && it.returnTypeSig == DexKitHelper.TypeSignature.VOID
        }

        CameraMembers.OtherMembers.mTrackSnapInfo = bridge.uniqueFindMethodInvoking {
            methodDescriptor = runDescriptor.descriptor

            beInvokedMethodParameterTypes = arrayOf(
                DexKitHelper.TypeSignature.BOOLEAN
            )
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.VOID
        }

        CameraMembers.OtherMembers.mGetContentUriFromPath = batchFindMethodsUsingStringsResultMap[CameraQueryKey.MediaProviderUtil_getContentUriFromPath]!!.first {
            it.returnTypeSig == DexKitHelper.TypeSignature.URI
        }.getMethodInstance()

        CameraMembers.OtherMembers.mGetAvailableSpace = bridge.uniqueFindMethodInvoking {
            methodDescriptor = runDescriptor.descriptor

            beInvokedMethodParameterTypes = arrayOf()
            beInvokedMethodReturnType = DexKitHelper.TypeSignature.LONG
        }

        val locationManagerClassDescriptor = batchFindClassesUsingStringsResultMap[CameraQueryKey.LocationManager]!!.first()
        CameraMembers.OtherMembers.mInstance = locationManagerClassDescriptor.name.toClass(CameraHooker.appClassLoader).method {
            returnType = locationManagerClassDescriptor.name
        }.give()!!


        val getCurrentLocationDirectlyMethodDescriptor = batchFindMethodsUsingStringsResultMap[CameraQueryKey.LocationManager_getCurrentLocationDirectly]!!.first {
            it.parameterTypesSig == "" && it.returnTypeSig == DexKitHelper.TypeSignature.LOCATION
        }
        CameraMembers.OtherMembers.mGetCurrentLocation = bridge.uniqueFindMethodCalling {
            methodDescriptor = getCurrentLocationDirectlyMethodDescriptor.descriptor

            callerMethodDeclareClass = locationManagerClassDescriptor.name
            callerMethodParameterTypes = arrayOf()
            callerMethodReturnType = DexKitHelper.TypeSignature.LOCATION
        }

        CameraMembers.OtherMembers.mGetDuration = batchFindMethodsUsingStringsResultMap[CameraQueryKey.Util_getDuration]!!.first {
            it.parameterTypesSig == DexKitHelper.TypeSignature.STRING && it.returnTypeSig == DexKitHelper.TypeSignature.LONG
        }.getMethodInstance()


        val cameraCapabilitiesClassDescriptor = batchFindClassesUsingStringsResultMap[CameraQueryKey.CameraCapabilities]!!.first()
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