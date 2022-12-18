# 字符串混淆插件

因为插件没有发布到远程仓库，所以不能使用maven依赖。不要问为什么不发布到远程，问就是因为懒。

### 使用

下载[插件jar](https://github.com/wuzuchang/StringObfuscatedPlugin/raw/master/plugin_libs/StringObfuscatePlugin-1.0.0.jar)文件，复制到项目中的plugin_libs目录下(没有就新增目录)

```java
├── plugin_libs
│   └── StringObfuscatePlugin-1.0.0.jar
├── app
│   ├── ..
│   └── ..
└── module
│   ├── ..
│   └── ..
```

在项目根目录`build.gradle`中添加以下代码

```groovy
buildscript {
    repositories {
        ...
        flatDir {
            dirs 'plugin_libs'
        }
    }
    dependencies {
        ...
        classpath "com.wzc.string.obfuscate:StringObfuscatePlugin:1.0.0"
    }
}

allprojects {
    repositories {
        ...
        flatDir {
            dirs 'plugin_libs'
        }
    }
}
```

在app module下的`build.gradle`中添加以下代码

```groovy
plugins {
    id 'com.android.application'
    // 字符串混淆插件
    id 'com.wzc.string.obfuscate'
}
stringObfuscate{
    openLog = true
    // 添加需要混淆的包名
    packageName = ["com.wzc.gradle.plugin","com.wzc.test"] 
}
```