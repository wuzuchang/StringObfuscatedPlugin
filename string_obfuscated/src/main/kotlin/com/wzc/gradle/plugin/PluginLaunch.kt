package com.wzc.gradle.plugin

import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.wzc.gradle.plugin.model.StringObfuscateExtensions
import com.wzc.gradle.plugin.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class PluginLaunch : Plugin<Project> {

    private val EXTENSIONS_NAME = "stringObfuscate"
    override fun apply(project: Project) {
        Logger.d("###############################################")
        Logger.d("#########    欢迎使用字符串混淆插件!!!    #########")
        Logger.d("###############################################")
        val app = project.plugins.hasPlugin(AppPlugin::class.java)
        val library = project.plugins.hasPlugin(LibraryPlugin::class.java)
        project.extensions.create(EXTENSIONS_NAME, StringObfuscateExtensions::class.java)
        val androidComponentsExtension =
            project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponentsExtension.onVariants { variant ->
            val extensions =
                project.extensions.getByName(EXTENSIONS_NAME) as StringObfuscateExtensions
            Logger.openLog = extensions.openLog
            variant.instrumentation.apply {
                if (app) {
                    // ALL检测当前项目的类及其库依赖项。
                    transformClassesWith(
                        ObfuscateAsmClassVisitorFactory::class.java, InstrumentationScope.ALL
                    ) { params ->
                        // parameters configuration
                        params.packageList = extensions.packageName
                    }
                } else if (library) {
                    Logger.d("请在app module中使用插件!!!")
                }
                setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
            }
        }
    }

    interface ParametersImpl : InstrumentationParameters {
        @get:Internal
        var packageList: List<String>
    }

    abstract class ObfuscateAsmClassVisitorFactory : AsmClassVisitorFactory<ParametersImpl> {

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: ClassVisitor
        ): ClassVisitor {
            return ScanClassVisitor(Opcodes.ASM9, nextClassVisitor)
        }

        override fun isInstrumentable(classData: ClassData): Boolean {
            val packageList = parameters.get().packageList
            for (packageName in packageList) {
                if (classData.className.startsWith(packageName)) {
                    return true
                }
            }
            return false
        }
    }
}