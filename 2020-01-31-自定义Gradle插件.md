---
layout:     post
title:      Gradle的插件开发
subtitle:   Gradle插件打包用于可重用的构建逻辑片段，可用于许多不同的项目和构建
date:       2020-01-31
author:     Yuriy
header-img: img/home-bg-o.jpg
catalog: true
tags:
    - 效率
    - 源码
---
# 深入理解Android编译原理—自定义Gradle的插件

## Andodid的编译过程
首先需要了解一下Android的打包过程，他是如何从一个代码(kotlin/java)，变成一个可以运行的apk的呢？如下图所示：

![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg1itn3wfj30fo0edt8w.jpg)

简述上图：
1. 打包资源文件，生成R.java文件。
2. 处理aidl文件，生成相应的Java文件。
3. 编译工程源代码，生成相应的class文件。
4. 转换所有的class文件，生成classes.dex文件。
5. 打包生成APK文件。

## Gradle的插件用
gradle插件可以在.class->.dex 对代码进行操作。

可以用来加快Gradle插件打包，可以用于可重用的构建逻辑片段，可以用于做代码注入，可用于许多不同的项目和构建。

## 目标
Gradle官方提供了各式各样的插件，常见的就是apply plugin: 'com.android.application' 和 apply plugin: 'com.android.library'，分别用于构建application和library。除了官方提供的插件，Gradle也提供了自定义插件的机制，让我们可以通过插件来实现特定的构建逻辑，并可以打包分享给其他人使用。
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg2533ajvj30w80aaaad.jpg)
我们要实现一个类似于**上图**apply plugin: 'com.android.application'的apply plugin: 'com.android.yuriypikachu.application'如**下图**，并且在控制台输出配置。
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg23anprbj30uq08kq37.jpg)

## 怎么做？
实现语言：Groovy、Java、Kotlin等都可以用于实现插件，本文使用Groovy语言实现。
打包方式：Gradle的插件有三种打包方式:
* 构建脚本(pluginlibrary)
插件逻辑写在build.gradle中，适用于逻辑简单的任务，但是该方式实现的插件在该构建脚本之外是不可见的，只能用于当前脚本。
* buildSrc项目
根据语言插件代码放在rootProjectDir/buildSrc/src/main/groovy目录中或者rootProjectDir/buildSrc/src/main/java或者rootProjectDir/buildSrc/src/main/kotlin，该插件对于该项目中构建使用的每个构建脚本都是可见的，适用于逻辑复杂但又不需要对外可见的插件。
* 独立项目(app)
一个独立的Groovy/Java/Kotlin项目，该项目生成并发布一个jar然后可以在多个版本中使用它并与他人共享，并且此jar包可能包含一些插件，或者将多个相关的任务类捆绑到单个库中。
 
## 创建pluginlibrary的module模块
经过如下几个步奏：
1、新建module，名称可根据实际情况声明。
2、删除module里的内容，只保留build.gradle和src/main目录。
3、由于gradle是基于Groovy，所以main目录下创建groovy目录，并在groovy目录下新建包。
4、删除build.gradle中的配置，并改为如下配置。
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg2g5rgjsj30m40k6jrv.jpg)
5、编写Groovy脚本。
在我们上面建好的src/main/groovy/io.github.yuriypikachu.plugindemo中创建并编写Groovy脚本，在这里我们继续使用我们上面建好的类，类文件以.groovy文件名结尾：
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg26x1qc0j31040j8dh5.jpg)
6、扩展插件
多数插件需要从构建脚本中获取一些配置来实现更多功能，执行此操作的一种方法是使用扩展对象，我们可以向目标对象添加“命名空间”DSL扩展，可以实现动态传参，并在DSL作用域中重新赋值，扩展对象只是一个Java Bean兼容类。
基本使用如下：
定义一个简单的扩展对象：
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg399dchsj30jy0byq34.jpg)
有关Extension的相关信息详见官方文档:[https://docs.gradle.org/current/javadoc/org/gradle/api/plugins/ExtensionContainer.html](https://docs.gradle.org/current/javadoc/org/gradle/api/plugins/ExtensionContainer.html)

添加扩展对象
```java
def extension = project.extensions.create("fakeAndroid", FakeAndroid)
```
注意，以上代码中构建脚本(fakeAndroid)中的闭包块名称需要与扩展对象名称匹配，当执行闭包时，扩展对象上的字段将根据标准Groovy闭包委托功能映射到闭包内的变量，且以上代码只能放在apply plugin: 'com.android.yuriypikachu.application'之后，同时，以上代码块是不是感觉很熟悉？没错，当我们构建Android项目时，会出现如下配置
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg2533ajvj30w80aaaad.jpg)
该配置也是Android DSL 闭包的一种实现，关于DSL格式详见官方文档
[https://guides.gradle.org/implementing-gradle-plugins/#modeling_dsl_like_apis](https://guides.gradle.org/implementing-gradle-plugins/#modeling_dsl_like_apis)
7、buildSrc
复杂的构建逻辑通常是封装为自定义任务或二进制插件插件的良好候选者，自定义任务和插件实现不应该存在于脚本中。只要代码不需要在多个独立项目之间共享，就可以非常方便地将buildSrc用于此目的。对buildSrc方式感兴趣的同学可以查看官方文档[https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources)。
8、添加资源文件，存放用于标识gradle插件的元数据，路径如下：resources/META-INF/gradle-plugins/com.android.yuriypikachu.application.properties，其中properties这个文件名可以随意起，但是在其他build.gradle中使用apply plugin: 'xx.xx.xx'的时候要根据文件名匹配，例如：
```java
apply plugin: 'com.android.yuriypikachu.application'
```
Gradle使用此文件来确定哪个类实现了该Plugin接口，文件配置如下：
```java
implementation-class = io.github.yuriypikachu.plugindemo.PrintFakeAndroid
```
最终module结构如下
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg2nw46rvj30oa0euaab.jpg)
9、打包发布
此处我们发布到本地，由于我们刚刚已经重新配置了gradle，此时我们可以在Gradle面板中发现一个uploadArchives task，执行该task，会将刚刚的插件发布到本地目录：
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg2ppydjzj314u0k6t9q.jpg)
10、使用
在主项目中进行如下配置
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg2wtb46aj31aa0ncwg6.jpg)
11、以上代码创建了一个简单的插件，创建完成后，同步下Gradle，会发现在Gradle面板中app->Tasks->other出现了一个printFakeAndroid的task，执行该task，会在Run面板中输出我们定义的内容
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gbg33sdj4ij31dp0u0wi4.jpg)
我们也可以在控制台输入如下命令来执行：
```java
./gradlew -q pluginTest
```
关于命令行中的-q详见官方文档[https://docs.gradle.org/current/userguide/logging.html#sec:choosing_a_log_level](https://docs.gradle.org/current/userguide/logging.html#sec:choosing_a_log_level)

## Demo 地址：[https://github.com/YuriyPikachu/PluginDemo](https://github.com/YuriyPikachu/PluginDemo)

## 了解更移动开发知识，欢迎关注公众号：
![](https://tva1.sinaimg.cn/large/006tNbRwgy1gayiubsiuaj309k09kdfn.jpg)         
* 头条：[Android开发加油站](https://www.toutiao.com/c/user/1789857904/#mid=1581788092440589)
* 微博：[Android开发加油站](http://weibo.com/2648402234/profile?rightmod=1&wvr=6&mod=personinfo&is_all=1)
* 公众号：[Android开发加油站]()
* QQ技术交流群：389274438
* 博客：[https://YuriyPikachu.github.io](https://YuriyPikachu.github.io)
* 简书：[YuriyPikachu](https://www.jianshu.com/u/1df4d713a12c)
* 知乎：[YuriyPikachu](https://www.zhihu.com/people/YuriyPikachu)
* csdn：[https://blog.csdn.net/pjingying](https://blog.csdn.net/pjingying)
* github：[https://github.com/YuriyPikachu](https://github.com/YuriyPikachu)
* 邮箱：[YuriyPikachu@163.com](YuriyPikachu@163.com)
