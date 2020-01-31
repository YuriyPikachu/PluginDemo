package io.github.yuriypikachu.plugindemo

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author YuLiang
 * update  2020-01-31
 * <a href="YuriyPikachu@163.com">Contact me</a>
 */
class PrintFakeAndroid implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create("fakeAndroid", FakeAndroid)
        project.task("printFakeAndroid"){
            doLast {
                println("输出自己定义的插件内容：---------------------------------")
                println("compileSdkVersion：${extension.compileSdkVersion}")
                println("applicationId：${extension.applicationId}")
                println("minSdkVersion：${extension.minSdkVersion}")
                println("targetSdkVersion：${extension.targetSdkVersion}")
                println("versionCode：${extension.versionCode}")
                println("versionName：${extension.versionName}")
                println("testInstrumentationRunner：${extension.testInstrumentationRunner}")
            }
        }
    }
}