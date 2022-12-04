package com.wzc.gradle.plugin.launch

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.wzc.gradle.plugin.CreateTestClass
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Simple version of AutoRegister plugin for ARouter
 * @author billy.qi email: qiyilike@163.com
 * @since 17/12/06 15:35
 */
class PluginLaunch implements Plugin<Project> {

    private static final String EXTENSIONS_NAME = "stringObfuscate"

    @Override
    void apply(Project project) {
        println("-----------------------------------------")
        println("|                                       |")
        println("|            欢迎来到德莱联盟!             |")
        println("|                                       |")
        println("-----------------------------------------")
        def isApp = project.plugins.hasPlugin(AppPlugin)
        def isLibrary = project.plugins.hasPlugin(LibraryPlugin)
        // 获取扩展中的配置
        //project.extensions.getByName(EXTENSIONS_NAME)
        if (isApp) {
            def android = project.extensions.getByType(AppExtension)
            android.registerTransform(new StringObfuscateTransform())
        } else if (isLibrary) {
            def android = project.extensions.getByType(LibraryExtension.class)
            android.registerTransform(new StringObfuscateTransform())
        }
    }

}
