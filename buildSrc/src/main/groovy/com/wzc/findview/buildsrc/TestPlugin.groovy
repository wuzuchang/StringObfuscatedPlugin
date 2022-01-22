package com.wzc.findview.buildsrc

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin);
        if (isApp) {
            println("----------------------------------------")
            println("|                                      |")
            println("|       TestPlugin apply isApp         |")
            println("|                                      |")
            println("----------------------------------------")
            def extensions = project.extensions.getByType(AppExtension)
            if (extensions != null) {
                extensions.registerTransform(new TesTransform())
            }
        }
        def isLibrary = project.plugins.hasPlugin(LibraryPlugin);
        if (isLibrary){
            println("----------------------------------------")
            println("|                                      |")
            println("|     TestPlugin apply isLibrary       |")
            println("|                                      |")
            println("----------------------------------------")
        }
    }
}