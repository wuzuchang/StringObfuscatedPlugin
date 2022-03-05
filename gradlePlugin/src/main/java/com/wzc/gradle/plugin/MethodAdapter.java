package com.wzc.gradle.plugin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * 方法修改
 * AdviceAdapter API：https://asm.ow2.io/javadoc/org/objectweb/asm/commons/AdviceAdapter.html
 * AdviceAdapter：实现了MethodVisitor接口，主要访问方法的信息。用来对具体方法进行字节码操作；
 * FieldVisitor：访问具体的类成员；
 * AnnotationVisitor：访问具体的注解信息
 */
public class MethodAdapter extends AdviceAdapter {

    private HashMap<String, String> mStaticFinalField;
    private String mMethodName;
    private String mOwner;

    protected MethodAdapter(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, HashMap<String, String> staticFinalField, String owner) {
        super(api, methodVisitor, access, name, descriptor);
        mStaticFinalField = staticFinalField;
        mMethodName = name;
        mOwner = owner;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if ("<clinit>".equals(mMethodName)) {
            Set<String> strings = mStaticFinalField.keySet();
            for (String field : strings) {
                String value = mStaticFinalField.get(field);
                String encryption = StringEncryptionUtil.encryption(value);
                mv.visitLdcInsn(encryption);
                mv.visitIntInsn(BIPUSH, 11);
                mv.visitMethodInsn(INVOKESTATIC, mOwner, "stringDecrypt", "(Ljava/lang/String;I)Ljava/lang/String;", false);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, mOwner, field, "Ljava/lang/String;");
            }
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == ICONST_0 && "com/wzc/gradle/plugin/Test".equals(mOwner)) {
            mv.visitInsn(ICONST_1);
        } else
            super.visitInsn(opcode);
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof String) {
            String encryption = StringEncryptionUtil.encryption((String) value);
            mv.visitLdcInsn(encryption);
            mv.visitIntInsn(BIPUSH, 11);
            mv.visitMethodInsn(INVOKESTATIC, mOwner, "stringDecrypt", "(Ljava/lang/String;I)Ljava/lang/String;", false);
        } else {
            super.visitLdcInsn(value);
        }
    }


    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void endMethod() {
        super.endMethod();
    }
}
