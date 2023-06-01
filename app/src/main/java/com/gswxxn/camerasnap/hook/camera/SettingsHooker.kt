package com.gswxxn.camerasnap.hook.camera

import com.gswxxn.camerasnap.dexkit.CameraMembers
import com.gswxxn.camerasnap.hook.CameraHooker.onFindMembers
import com.gswxxn.camerasnap.utils.ReflectUtils.methodHook
import com.gswxxn.camerasnap.wrapper.camera.CameraSettings
import com.gswxxn.camerasnap.wrapper.camera.R
import com.gswxxn.camerasnap.wrapper.camera.fragment.settings.CameraPreferenceFragment
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.type.java.CharSequenceClass

/** 相机配置及设置项的 Hooker **/
object SettingsHooker: YukiBaseHooker() {
    override fun onHook() {
        /**
         * 替换是否支持街拍模式, 部分机型此方法始终返回 false
         *
         * 由于原方法被混淆, 所以此方法在 [onFindMembers] 中查找
         */
        CameraMembers.SettingsMembers.mGetSupportSnap.methodHook {
            replaceToTrue()
        }

        var isAllowFindCameraSnap = true
        "com.android.camera.fragment.settings.CameraPreferenceFragment".hook {
            /**
             * 将街拍开关替换为下拉框
             *
             * 在同类下的 addModulePreferences() 方法中被调用
             */
            injectMember {
                method {
                    superClass()
                    name = "addCheckBoxPreference"
                }
                beforeHook {
                    val key = args[1].toString()
                    if (key == CameraSettings.KEY_CAMERA_SNAP) {
                        instance.current().method {
                            superClass()
                            name = "addPreviewListPreference"
                        }.call(
                            args[0],                                  // Preference Category
                            CameraSettings.KEY_CAMERA_SNAP,           // Key
                            R.string.pref_camera_snap_default,        // Default value
                            R.string.pref_camera_snap_enable_title,   // Enable Title
                            R.array.pref_camera_snap_entries,         // Entries
                            R.array.pref_camera_snap_entryvalues      // Entry Values
                        )
                        resultNull()
                    }
                }
            }

            /**
             * 替换街拍模式配置模式, 例如如何读取及写入配置
             */
            injectMember {
                method {
                    name = "updatePreferenceEntries"
                    emptyParam()
                }
                beforeHook {
                    CameraPreferenceFragment(instance).updatePreferenceEntries()
                    isAllowFindCameraSnap = false
                }
                afterHook { isAllowFindCameraSnap = true }
            }
        }

        /**
         * 移除原街拍配置模式
         *
         * 在 com.android.camera.fragment.settings.CameraPreferenceFragment 类中的
         * updatePreferenceEntries() 方法中被调用, 原理为替换返回值为 null 从而影响 if 判断
         */
        "androidx.preference.PreferenceGroup".hook {
            injectMember {
                method {
                    name = "findPreference"
                    param(CharSequenceClass)
                }
                beforeHook {
                    if (args[0] == CameraSettings.KEY_CAMERA_SNAP && !isAllowFindCameraSnap) {
                        resultNull()
                    }
                }
            }
        }

        /**
         * 替换获取街拍配置的方法
         *
         * 原方法无论街拍配置为何值, 均返回街拍模式 A, 即息屏拍照
         */
        CameraMembers.SettingsMembers.mGetMiuiSettingsKeyForStreetSnap.methodHook {
            replaceAny {
                CameraSettings.getMiuiSettingsKeyForStreetSnap(args[0].toString())
            }
        }
    }
}