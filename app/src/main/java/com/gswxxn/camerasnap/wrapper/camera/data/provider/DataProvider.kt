package com.gswxxn.camerasnap.wrapper.camera.data.provider

import com.highcapable.yukihookapi.hook.factory.current

object DataProvider {
    class ProviderEditor(private val instance: Any) {
        fun apply() = instance.current().method {
            name = "apply"
            emptyParam()
        }.call()

        fun remove(key: String) = instance.current().method {
            name = "remove"
            param(String::class.java)
        }.call(key).run { this@ProviderEditor }
    }
}