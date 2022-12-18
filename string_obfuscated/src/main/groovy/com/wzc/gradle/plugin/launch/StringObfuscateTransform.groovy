package com.wzc.gradle.plugin.launch

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.wzc.gradle.plugin.StringObfuscateConfig
import com.wzc.gradle.plugin.utils.ConstantUtil
import com.wzc.gradle.plugin.utils.LogUtils
import com.wzc.gradle.plugin.utils.ScanJarUtils
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class StringObfuscateTransform extends Transform {

    private Project mProject

    StringObfuscateTransform(Project project) {
       mProject = project
    }
    /**
     * 获取Transform名称
     * @return transform名称,可以在AS右侧Gradle中找到,路径app/tasks/other/transformClassWithStringObfuscateForDebug
     */
    @Override
    String getName() {
        return "StringObfuscate"
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * TransformManager內有多种组合
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指 Transform 要操作内容的范围，官方文档 Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     * TransformManager內有多种组合
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否增量编译
     * 所谓增量编译，是指当源程序的局部发生变更后进重新编译的工作只限于修改的部分及与之相关部分的内容，而不需要对全部代码进行编译
     *
     * @return false：否
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 文档：https://google.github.io/android-gradle-dsl/javadoc/3.4/
     *
     * @param transformInvocation transformInvocation
     * @throws TransformException* @throws InterruptedException* @throws IOException
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        println("#########################################")
        println("#########     开始字符串混淆")
        println("#########################################")
        _transform(transformInvocation.getContext(), transformInvocation.getInputs(), transformInvocation.getOutputProvider(), transformInvocation.isIncremental())
        println("#########################################")
        println("#########     字符串混淆结束")
        println("#########################################")
    }

    /**
     * _transform
     * @param context context
     * @param collectionInput transform输入流，包含两种类型，目录格式和jar
     * @param outputProvider 是用来获取输出目录，我们要将操作后的文件复制到输出目录中。调用getContentLocation方法获取输出目录
     * @param isIncremental 是否增量编译
     * @throws IOException* @throws TransformException* @throws InterruptedException
     */
    private void _transform(Context context, Collection<TransformInput> collectionInput, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {

        if (!isIncremental) {
            //非增量,需要删除输出目录
            outputProvider.deleteAll()
        }
        if (collectionInput == null) {
            throw new IllegalArgumentException("TransformInput is null !!!")
        }
        if (outputProvider == null) {
            throw new IllegalArgumentException("TransformOutputProvider is null !!!")
        }

        StringObfuscateConfig config = mProject.property(ConstantUtil.EXTENSIONS_NAME)
        // or
        // StringObfuscateConfig config = mProject.getExtensions().findByName(ConstantUtil.EXTENSIONS_NAME)
        LogUtils.setOpenLog(config.isOpenLog())

        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        collectionInput.each { TransformInput transformInput ->
            //对类型为“文件夹”的input进行遍历
            transformInput.directoryInputs.each { DirectoryInput directoryInput ->
                File dir = directoryInput.file
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                if (dir.isDirectory()) {
                    dir.eachFileRecurse(FileType.FILES) { File file ->
                        if (ScanJarUtils.shouldProcessClass(file.absolutePath, config)) {
                            LogUtils.d("StringObfuscate find class " + file.name)
                            ScanJarUtils.scanClass(file)
                        }
                    }
                }
                //这里执行字节码的注入，不操作字节码的话也要将输入路径拷贝到输出路径
                FileUtils.copyDirectory(dir, dest)
            }
            //jar
            transformInput.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name.replaceAll(":", "")
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def src = jarInput.file
                //获取输出路径下的jar包名称；+MD5是为了防止重复打包过程中输出路径名不重复，否则会被覆盖。
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (ScanJarUtils.shouldProcessPreDexJar(src.absolutePath)) {

                    //scan jar file to find classes
                    def jarFilePath = context.temporaryDir.absolutePath + File.separator + md5Name + jarName + ".jar"
                    def jarFile = ScanJarUtils.scanJar(src, jarFilePath, config)
                    if (jarFile == null) {
                        FileUtils.copyFile(src, dest)
                    } else {
                        FileUtils.copyFile(jarFile, dest)
                    }
                } else {
                    //这里执行字节码的注入，不操作字节码的话也要将输入路径拷贝到输出路径
                    FileUtils.copyFile(src, dest)
                }
            }
        }
    }

}