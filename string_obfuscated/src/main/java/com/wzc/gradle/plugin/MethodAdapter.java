package com.wzc.gradle.plugin;

import com.wzc.gradle.plugin.utils.ConstantUtil;
import com.wzc.gradle.plugin.utils.StringEncryptionUtil;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashMap;
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
    private int key1;
    private int key2;
    private int key3;

    protected MethodAdapter(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, HashMap<String, String> staticFinalField, String owner, int key1, int key2, int key3) {
        super(api, methodVisitor, access, name, descriptor);
        mStaticFinalField = staticFinalField;
        mMethodName = name;
        mOwner = owner;
        this.key1 = key1;
        this.key2 = key2;
        this.key3 = key3;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if ("<clinit>".equals(mMethodName)) {
            Set<String> strings = mStaticFinalField.keySet();
            for (String field : strings) {
                if ("".equals(field)){
                    super.visitLdcInsn(field);
                    continue;
                }
                String value = mStaticFinalField.get(field);
                String encryption = StringEncryptionUtil.encryption(value, key1, key2, key3);
                mv.visitLdcInsn(encryption);
                mv.visitIntInsn(BIPUSH, key1);
                mv.visitMethodInsn(INVOKESTATIC, mOwner, ConstantUtil.STRING_DECRYPT_METHOD_NAME, "(Ljava/lang/String;I)Ljava/lang/String;", false);
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
        if (value instanceof String && !"".equals(value)) {
            String encryption = StringEncryptionUtil.encryption((String) value, key1, key2, key3);
            mv.visitLdcInsn(encryption);
            mv.visitIntInsn(BIPUSH, key1);
            mv.visitMethodInsn(INVOKESTATIC, mOwner, ConstantUtil.STRING_DECRYPT_METHOD_NAME, "(Ljava/lang/String;I)Ljava/lang/String;", false);
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
