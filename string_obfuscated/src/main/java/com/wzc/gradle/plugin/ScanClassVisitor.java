package com.wzc.gradle.plugin;


import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.*;

import com.wzc.gradle.plugin.utils.Logger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Random;

/**
 * ClassVisitor API: https://asm.ow2.io/javadoc/org/objectweb/asm/ClassVisitor.html
 */
public class ScanClassVisitor extends ClassVisitor {

    private static final int ACC_STATIC_FINAL = Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
    private static final String STRING_DESCRIPTOR = "Ljava/lang/String;";
    private final HashMap<String, String> mStaticFinalField = new HashMap<>();
    private boolean hasClinit;
    public static boolean hasString;
    private boolean hasStringDecrypt;
    private String mOwner;
    private final int key1;
    // 同一个类里面 key2和key3是固定的，不同类key2和key3不一样
    private final int key2;
    private final int key3;


    public ScanClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
        hasString = false;
        hasStringDecrypt = false;
        key1 = getRandomKey();
        key2 = getRandomKey();
        key3 = getRandomKey();
    }

    private int getRandomKey() {
        Random rand = new Random();
        return rand.nextInt(127);
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
        if (value instanceof String && !"".equals(value)) {
            hasString = true;
            // 将 final + static 修饰的变量置空（不然无法在静态块中初始化），之后再在<clinit>中赋值
            if (STRING_DESCRIPTOR.equals(descriptor) && isStaticFinal) {
                mStaticFinalField.put(name, (String) value);
                return super.visitField(access, name, descriptor, signature, null);
            }
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
        if ("stringDecrypt".equals(name)) {
            hasStringDecrypt = true;
            return methodVisitor;
        }
        if ("<clinit>".equals(name)) {
            hasClinit = true;
        }
        return new MethodAdapter(Opcodes.ASM9, methodVisitor, access, name, descriptor
                , mStaticFinalField, mOwner, key1, key2, key3);
    }

    /**
     * 该方法是最后一个被调用的方法，用于通知访问者该类的所有字段和方法都已访问。
     */
    @Override
    public void visitEnd() {

        // 如果没有扫描<clinit>方法，则说明全是final + static；需要插入static <clinit>()方法
        if (!hasClinit && mStaticFinalField.size() > 0) {
            Logger.INSTANCE.d(mOwner + " add <clinit> method");
            MethodVisitor mv = visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 0);
            mv.visitEnd();
        }
        if (hasString && !hasStringDecrypt) {
            Logger.INSTANCE.d(mOwner + " add stringDecrypt method");
            addMethod(key2, key3);
        }
        super.visitEnd();
    }

    private void addMethod(int key2, int key3) {
        MethodVisitor methodVisitor = this.visitMethod(ACC_PUBLIC | ACC_STATIC, "stringDecrypt", "(Ljava/lang/String;I)Ljava/lang/String;", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception");
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(54, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitInsn(IDIV);
        methodVisitor.visitVarInsn(ISTORE, 2);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLineNumber(55, label3);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
        methodVisitor.visitVarInsn(ASTORE, 3);
        Label label4 = new Label();
        methodVisitor.visitLabel(label4);
        methodVisitor.visitLineNumber(56, label4);
        methodVisitor.visitVarInsn(ILOAD, 2);
        methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
        methodVisitor.visitVarInsn(ASTORE, 4);
        Label label5 = new Label();
        methodVisitor.visitLabel(label5);
        methodVisitor.visitLineNumber(57, label5);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ISTORE, 5);
        Label label6 = new Label();
        methodVisitor.visitLabel(label6);
        methodVisitor.visitFrame(Opcodes.F_FULL, 6, new Object[]{"java/lang/String", Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[B", Opcodes.INTEGER}, 0, new Object[]{});
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitVarInsn(ILOAD, 2);
        Label label7 = new Label();
        methodVisitor.visitJumpInsn(IF_ICMPGE, label7);
        Label label8 = new Label();
        methodVisitor.visitLabel(label8);
        methodVisitor.visitLineNumber(58, label8);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitInsn(IMUL);
        methodVisitor.visitVarInsn(ISTORE, 6);
        Label label9 = new Label();
        methodVisitor.visitLabel(label9);
        methodVisitor.visitLineNumber(59, label9);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitLdcInsn("0123456789abcdef");
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(CALOAD);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I", false);
        methodVisitor.visitInsn(ICONST_4);
        methodVisitor.visitInsn(ISHL);
        methodVisitor.visitLdcInsn("0123456789abcdef");
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IADD);
        methodVisitor.visitInsn(CALOAD);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I", false);
        methodVisitor.visitInsn(IOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        Label label10 = new Label();
        methodVisitor.visitLabel(label10);
        methodVisitor.visitLineNumber(57, label10);
        methodVisitor.visitIincInsn(5, 1);
        methodVisitor.visitJumpInsn(GOTO, label6);
        methodVisitor.visitLabel(label7);
        methodVisitor.visitLineNumber(61, label7);
        methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
        methodVisitor.visitVarInsn(ILOAD, 1);
        methodVisitor.visitIntInsn(SIPUSH, key2);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitVarInsn(ISTORE, 5);
        Label label11 = new Label();
        methodVisitor.visitLabel(label11);
        methodVisitor.visitLineNumber(63, label11);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitIntInsn(SIPUSH, key3);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        Label label12 = new Label();
        methodVisitor.visitLabel(label12);
        methodVisitor.visitLineNumber(64, label12);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ISTORE, 6);
        Label label13 = new Label();
        methodVisitor.visitLabel(label13);
        methodVisitor.visitLineNumber(65, label13);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(ISTORE, 7);
        Label label14 = new Label();
        methodVisitor.visitLabel(label14);
        methodVisitor.visitFrame(Opcodes.F_APPEND, 3, new Object[]{Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ARRAYLENGTH);
        Label label15 = new Label();
        methodVisitor.visitJumpInsn(IF_ICMPGE, label15);
        Label label16 = new Label();
        methodVisitor.visitLabel(label16);
        methodVisitor.visitLineNumber(66, label16);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        Label label17 = new Label();
        methodVisitor.visitLabel(label17);
        methodVisitor.visitLineNumber(67, label17);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ISTORE, 6);
        Label label18 = new Label();
        methodVisitor.visitLabel(label18);
        methodVisitor.visitLineNumber(65, label18);
        methodVisitor.visitIincInsn(7, 1);
        methodVisitor.visitJumpInsn(GOTO, label14);
        methodVisitor.visitLabel(label15);
        methodVisitor.visitLineNumber(70, label15);
        methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, "java/lang/String");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitLdcInsn("UTF-8");
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/lang/String;)V", false);
        methodVisitor.visitLabel(label1);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(71, label2);
        methodVisitor.visitFrame(Opcodes.F_FULL, 2, new Object[]{"java/lang/String", Opcodes.INTEGER}, 1, new Object[]{"java/lang/Exception"});
        methodVisitor.visitVarInsn(ASTORE, 2);
        Label label19 = new Label();
        methodVisitor.visitLabel(label19);
        methodVisitor.visitLineNumber(72, label19);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false);
        Label label20 = new Label();
        methodVisitor.visitLabel(label20);
        methodVisitor.visitLineNumber(74, label20);
        methodVisitor.visitLdcInsn("");
        methodVisitor.visitInsn(ARETURN);
        Label label21 = new Label();
        methodVisitor.visitLabel(label21);
        methodVisitor.visitLocalVariable("pos", "I", null, label9, label10, 6);
        methodVisitor.visitLocalVariable("i", "I", null, label6, label7, 5);
        methodVisitor.visitLocalVariable("i", "I", null, label14, label15, 7);
        methodVisitor.visitLocalVariable("length", "I", null, label3, label2, 2);
        methodVisitor.visitLocalVariable("hexChars", "[C", null, label4, label2, 3);
        methodVisitor.visitLocalVariable("stringByte", "[B", null, label5, label2, 4);
        methodVisitor.visitLocalVariable("keyXor", "B", null, label11, label2, 5);
        methodVisitor.visitLocalVariable("temp", "B", null, label13, label2, 6);
        methodVisitor.visitLocalVariable("e", "Ljava/lang/Exception;", null, label19, label20, 2);
        methodVisitor.visitLocalVariable("ciphertext", "Ljava/lang/String;", null, label0, label21, 0);
        methodVisitor.visitLocalVariable("key1", "I", null, label0, label21, 1);
        methodVisitor.visitMaxs(7, 8);
        methodVisitor.visitEnd();
    }

}
