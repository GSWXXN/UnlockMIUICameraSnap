package com.gswxxn.camerasnap.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog { tag = "UnlockMIUICameraSnap" }
        // 由于 Hook 了一个会被大量调用的函数, 所以这里关闭 Debug 日志输出
        isDebug = false
    }

    override fun onHook() = encase {
        loadApp("com.android.camera", CameraHooker)

        loadSystem(AndroidHooker)
    }
}