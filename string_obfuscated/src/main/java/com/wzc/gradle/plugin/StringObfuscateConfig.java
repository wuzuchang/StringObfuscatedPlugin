package com.wzc.gradle.plugin;

import java.util.List;

public class StringObfuscateConfig {
    private boolean openLog;
    private List<String> packageName;

    public boolean isOpenLog() {
        return openLog;
    }

    public void setOpenLog(boolean openLog) {
        this.openLog = openLog;
    }

    public List<String> getPackageName() {
        return packageName;
    }

    public void setPackageName(List<String> packageName) {
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return "StringObfuscateConfig{" +
                "openLog=" + openLog +
                ", packageName=" + packageName +
                '}';
    }
}
