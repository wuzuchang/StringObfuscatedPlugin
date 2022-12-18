package com.wzc.gradle.plugin.launch

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.wzc.gradle.plugin.StringObfuscateConfig
import com.wzc.gradle.plugin.utils.ConstantUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Simple version of AutoRegister plugin for ARouter
 * @author billy.qi email: qiyilike@163.com
 * @since 17/12/06 15:35
 */
class PluginLaunch implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("###############################################")
        println("#########    欢迎使用字符串混淆插件!!!    #########")
        println("###############################################")
        def isApp = project.plugins.hasPlugin(AppPlugin)
        def isLibrary = project.plugins.hasPlugin(LibraryPlugin)
        // 获取扩展中的配置
        project.extensions.create(ConstantUtil.EXTENSIONS_NAME, StringObfuscateConfig)
        if (isApp) {
            def android = project.extensions.getByType(AppExtension.class)
            android.registerTransform(new StringObfuscateTransform(project))
        } else if (isLibrary) {
            println("#########    请在app module中使用插件!!!    #########")
        }
    }

}
