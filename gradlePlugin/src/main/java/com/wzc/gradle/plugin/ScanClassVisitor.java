package com.wzc.gradle.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;

/**
 * ClassVisitor API: https://asm.ow2.io/javadoc/org/objectweb/asm/ClassVisitor.html
 */
public class ScanClassVisitor extends ClassVisitor {

    private static final int ACC_STATIC_FINAL = Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
    private static final String STRING_DESCRIPTOR = "Ljava/lang/String;";
    private HashMap<String, String> mStaticFinalField = new HashMap<>();
    private boolean hasClinit;
    private String mOwner;


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
        System.out.println("visit======" + " mOwner: " + name);
        mOwner = name;
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
        // System.out.println("InnerClass======" + getAccess(access) + " " + name + "(), outerName:" + outerName
        //        + ",innerName:" + innerName);
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

        boolean isStaticFinal = (access & ACC_STATIC_FINAL) == ACC_STATIC_FINAL;
        // 将 final + static 修饰的变量置空（不然无法在静态块中初始化），之后再在<clinit>中赋值
        if (STRING_DESCRIPTOR.equals(descriptor) && isStaticFinal && value instanceof String && !"".equals(value)) {
            mStaticFinalField.put(name, (String) value);
            return super.visitField(access, name, descriptor, signature, null);
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
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ("<clinit>".equals(name)) {
            hasClinit = true;
        }
        return new MethodAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor, mStaticFinalField, mOwner);
    }

    /**
     * 该方法是最后一个被调用的方法，用于通知访问者该类的所有字段和方法都已访问。
     */
    @Override
    public void visitEnd() {

        // 如果没有扫描<clinit>方法，则说明全是final + static；需要插入static <clinit>()方法
        if (!hasClinit && mStaticFinalField.size() > 0) {
            MethodVisitor mv = visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 0);
            mv.visitEnd();
        }
        super.visitEnd();
    }
}
