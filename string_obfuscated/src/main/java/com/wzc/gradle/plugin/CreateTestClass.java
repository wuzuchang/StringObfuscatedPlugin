package com.wzc.gradle.plugin;

import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;

/**
 * 通过ASM创建class，但是ASM创建的class不会自动导包。建议使用javapoet生成class文件
 */
public class CreateTestClass {


    public void create(String javac, String transforms, String packageName) throws IOException {
        //定义一个叫做Example的类
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Example", null, "java/lang/Object", null);

        //生成默认的构造方法
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);

        //生成构造方法的字节码指令
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(1, 1);
        mw.visitEnd();


        //生成String name字段
        FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC, "name", "Ljava/lang/String;", null, null);
        AnnotationVisitor av = fv.visitAnnotation("LNotNull;", true);
        av.visit("value", "abc");
        av.visitEnd();
        fv.visitEnd();


        //生成main方法
        mw = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null);

        //生成main方法中的字节码指令
        mw.visitFieldInsn(Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;");

        mw.visitLdcInsn("Hello world!");
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V",
                false);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(2, 2);

        //字节码生成完成
        mw.visitEnd();

        // 获取生成的class文件对应的二进制流
        byte[] code = cw.toByteArray();

        //拷贝到javac
        FileUtils.writeByteArrayToFile(
                new File(javac + packageName + "Example.class"), code);
        //将生成类拷贝到transforms，用于dex
        FileUtils.writeByteArrayToFile(
                new File(transforms + packageName + "Example.class"), code);
    }

}
