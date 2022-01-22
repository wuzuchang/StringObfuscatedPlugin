package com.wzc.gradle.plugin;

import org.objectweb.asm.FieldVisitor;

/**
 * FieldVisitor：访问具体的类成员
 * API: https://asm.ow2.io/javadoc/org/objectweb/asm/FieldVisitor.html
 */
public class MyFieldVisitor extends FieldVisitor {

    public MyFieldVisitor(int api) {
        super(api);
    }

    public MyFieldVisitor(int api, FieldVisitor fieldVisitor) {
        super(api, fieldVisitor);
    }


}
