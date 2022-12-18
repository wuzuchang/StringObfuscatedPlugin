package com.wzc.gradle.plugin.utils;

import com.wzc.gradle.plugin.ScanClassVisitor;
import com.wzc.gradle.plugin.StringObfuscateConfig;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class ScanJarUtils {
    public static File scanJar(File inputFile, String outputJarPath, StringObfuscateConfig config) {
        JarFile jarFile = null;
        File outputJar = null;
        JarOutputStream jarOutputStream = null;
        try {
            jarFile = new JarFile(inputFile);
            outputJar = new File(outputJarPath);
            jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar));
            Enumeration<JarEntry> enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                if (!shouldProcessJarClass(jarFile.getName(), config)) {
                    continue;
                }
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                jarOutputStream.putNextEntry(zipEntry);
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
                if (!entryName.endsWith(".class")) {
                    jarOutputStream.write(sourceClassBytes);
                    jarOutputStream.closeEntry();
                    inputStream.close();
                    continue;
                }
                LogUtils.d("StringObfuscate find jar " + jarEntry.getName());
                try {
                    ClassReader cr = new ClassReader(sourceClassBytes);
                    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
                    ScanClassVisitor sc = new ScanClassVisitor(Opcodes.ASM7, cw);
                    cr.accept(sc, ClassReader.EXPAND_FRAMES);

                    // 写入文件
                    byte[] code = cw.toByteArray();
                    jarOutputStream.write(code);
                } catch (Exception e) {
                    e.printStackTrace();
                    jarOutputStream.write(sourceClassBytes);
                } finally {
                    inputStream.close();
                    jarOutputStream.closeEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (jarFile != null) jarFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (jarOutputStream != null) jarOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return outputJar;
    }

    public static void scanClass(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            ClassReader cr = new ClassReader(inputStream);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            ScanClassVisitor sc = new ScanClassVisitor(Opcodes.ASM7, cw);
            cr.accept(sc, ClassReader.EXPAND_FRAMES);

            // 写入文件
            byte[] code = cw.toByteArray();
            FileUtils.writeByteArrayToFile(file, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository") && !path.contains("\\.gradle\\caches\\");
    }

    public static boolean shouldProcessClass(String filePath, StringObfuscateConfig config) {
        List<String> configPackageNames = config.getPackageName();
        if (configPackageNames == null || configPackageNames.size() == 0) {
            return true;
        }
        for (String packageName : config.getPackageName()) {
            String targetPackageName = packageName.replaceAll("\\.", "\\\\");
            if (filePath.contains(targetPackageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldProcessJarClass(String fileName, StringObfuscateConfig config) {
        List<String> configPackageNames = config.getPackageName();
        if (configPackageNames == null || configPackageNames.size() == 0) {
            return true;
        }
        for (String packageName : config.getPackageName()) {
            String targetPackageName = packageName.replaceAll("\\.", "/");
            if (fileName.startsWith(targetPackageName)) {
                return true;
            }
        }
        return false;
    }
}
