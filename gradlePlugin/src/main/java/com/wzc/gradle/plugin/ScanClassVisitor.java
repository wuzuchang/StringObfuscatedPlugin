package com.wzc.gradle.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * ClassVisitor API: https://asm.ow2.io/javadoc/org/objectweb/asm/ClassVisitor.html
 */
public class ScanClassVisitor extends ClassVisitor {
    public ScanClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    /**
     * 可以拿到类的详细信息，然后对满足条件的类进行过滤
     *
     * @param version    JDK版本
     * @param access     类的修饰符
     * @param name       类的名称，通常使用完整包名+类名
     * @param signature  泛型信息，如果没有定义泛型该参数为null
     * @param superName  当前类的父类，所有类的父类java.lang.Object
     * @param interfaces 实现的接口列表
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        System.out.println("visit======" + getAccess(access) + " class: " + name + " extend " + superName);
    }

    /**
     * 访问内部类信息
     *
     * @param name      内部类名称
     * @param outerName 内部类所属的类的名称
     * @param innerName 内部类在其封闭类中的（简单）名称。对于匿名内部类，可能为 null。
     * @param access    内部类的修饰符
     */
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
        System.out.println("InnerClass======" + getAccess(access) + " " + name + "(), outerName:" + outerName
                + ",innerName:" + innerName);
    }

    /**
     * 类中字段
     *
     * @param access     修饰符
     * @param name       字段名
     * @param descriptor 字段类型
     * @param signature  泛型描述
     * @param value      默认值
     */
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        System.out.println("Field======" + getAccess(access) + " " + descriptor + " " + name + " " + signature + " " + value);
        if ("mSwitch".equals(name)) {
            System.out.println("修改mSwitch初始值为true");
            return super.visitField(access, name, descriptor, signature, true);
        } else if ("string".equals(name)) {
            System.out.println("修改string初始值为xxxx");
            return super.visitField(access, name, descriptor, signature, "xxxx");
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    /**
     * 拿到要修改的方法，然后进行修改
     *
     * @param access     方法的修饰符
     * @param name       方法名
     * @param descriptor 方法签名，返回值
     * @param signature  泛型相关信息
     * @param exceptions 抛出的异常，没有异常抛出该参数为null
     * @return
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        System.out.println("Method======" + getAccess(access) + " " + descriptor + " " + name + "()");
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        FieldVisitor fv = super.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "timer",
                "J", null, null);
        if (fv != null) {
            fv.visitEnd();
        }
        super.visitEnd();
    }

    private String getAccess(int access) {
        String accessString = "";
        switch (access) {
            case Opcodes.ACC_PUBLIC:
                accessString = "public";
                break;
            case Opcodes.ACC_PRIVATE:
                accessString = "private";
                break;
            case Opcodes.ACC_PROTECTED:
                accessString = "protected";
                break;
            case Opcodes.ACC_FINAL:
                accessString = "final";
                break;
            case Opcodes.ACC_SUPER:
                accessString = "extends";
                break;
            case Opcodes.ACC_INTERFACE:
                accessString = "interface";
                break;
            case Opcodes.ACC_ABSTRACT:
                accessString = "abstract";
                break;
            case Opcodes.ACC_ANNOTATION:
                accessString = "annotation";
                break;
            case Opcodes.ACC_ENUM:
                accessString = "enum";
                break;
            case Opcodes.ACC_DEPRECATED:
                accessString = "@Deprecated";
                break;
            default:
                accessString = String.valueOf(access);
                break;
        }
        return accessString;
    }
}
