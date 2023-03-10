package com.gswxxn.camerasnap.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.luckypray.dexkit.DexKitBridge

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog { tag = "UnlockMIUICameraSnap" }
        isDebug = false
    }

    override fun onHook() = encase {
        loadApp("com.android.camera") {
            "com.android.camera.snap.SnapCamera".hook {
                injectMember {
                    method {
                        name = "playSound"
                        emptyParam()
                    }
                    intercept()
                }
            }

            System.loadLibrary("dexkit")
            DexKitBridge.create(appInfo.sourceDir)?.use { bridge ->
                bridge.findMethodInvoking {
                    methodDeclareClass = "com.android.camera.CameraSettings"
                    methodName = "getCameraSnapSettingNeed"
                    methodReturnType = "Z"
                    beInvokedMethodReturnType = "Z"
                    beInvokedMethodParameterTypes = arrayOf()
                }.forEach { (_, invokingList) ->
                    val method = invokingList.first().getMethodInstance(appClassLoader)

                    method.declaringClass.name.hook {
                        injectMember {
                            members(method)
                            replaceToTrue()
                        }
                    }
                }
            }
        }
    }
}