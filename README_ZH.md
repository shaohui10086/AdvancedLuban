# AdvancedLuban
[![build](https://img.shields.io/badge/build-1.3.4-brightgreen.svg?maxAge=2592000)](https://bintray.com/shaohui/maven/AdvancedLuban)
[![license](https://img.shields.io/badge/license-Apache%202-blue.svg?maxAge=2592000)](https://github.com/shaohui10086/AdvancedLuban/blob/master/LICENSE)

![sketch](/image/sketch_map.png)

`AdvancedLuban` —— 是一个方便简约的 `Android` 图片压缩工具库，提供多种压缩策略（包括`Luban`原有的压缩策略），多种调用方式，自定义压缩，多图压缩，专注更好的图片压缩使用体验


照片数量 | 原图总大小 | 压缩后总大小 | 总耗时
--- | --- | --- | ---
1 | 4.3Mb | 85Kb | 0.23s
4 | 14.22Mb | 364Kb | 1.38s
9 | 36.23Mb | 745Kb | 4.43s

## Import

Maven

    <dependency>
      <groupId>me.shaohui.advancedluban</groupId>
      <artifactId>library</artifactId>
      <version>1.3.4</version>
      <type>pom</type>
    </dependency>

    
or Gradle

	compile 'me.shaohui.advancedluban:library:1.3.4'

## Usage


### `Listener`方式

`AdvancedLuban`内部采用`Computation`线程进行图片压缩，外部调用只需设置好结果监听即可：

    Luban.compress(context, file)       // 初始化Luban，并传入要压缩的图片
        .putGear(Luban.THIRD_GEAR)      // 设定压缩模式，默认 THIRD_GEAR
        .launch(listener);              // 启动压缩并设置监听

### `RxJava`方式

`RxJava`调用方式同样默认`Computation`线程进行压缩, 主线程处理结果

    Luban.compress(context, file)                           
            .putGear(Luban.CUSTOM_GEAR)                 
            .asObservable()                             // 生成Observable
            .subscribe(successAction, errorAction)      // 订阅压缩事件

### 压缩模式

    
#### 1. CUSTOM_GEAR

`AdvancedLuban`增加的个性化压缩，根据限制要求对图片进行压缩，可以限制：图片的宽度、高度以及图片文件的大小
    
        Luban.compress(context, file)
                .setMaxSize(500)                // 限制最终图片大小（单位：Kb）
                .setMaxHeight(1920)             // 限制图片高度
                .setMaxWidth(1080)              // 限制图片宽度
                .setCompressFormat()            // 自定义压缩图片格式，目前只支持：JEPG和WEBP，因为png不支持压缩图片品质
                .putGear(Luban.CUSTOM_GEAR)     // 使用 CUSTOM_GEAR 压缩模式
                .asObservable()

#### 2. THIRD_GEAR 

主要使用`Luban`的算法，提供了类似微信的压缩效果，适用于普通压缩，没有文件大小限制以及图片的宽高限制

#### 3. FIRST_GEAR

`THIRD_GEAR`的简化版本，压缩之后的图片分辨率小于 1280 x 720, 文件最后小于60Kb，特殊情况下，小于原图片的1/5，适用于快速压缩，不计较最终图片品质

## 多图压缩

如果你选择的调用方式的是`Listener`方式:

        Luban.compress(context, fileList)           // 加载多张图片
                .putGear(Luban.CUSTOM_GEAR)             
                .launch(multiCompressListener);     // 传入一个 OnMultiCompressListener 

`RxJava` 方式：

        Luban.compress(context, fileList)           // 加载多张图片
                .putGear(Luban.CUSTOM_GEAR)             
                .asListObservable()                 // 生成Observable<List> 返回压缩成功的所有图片结果
               
> 为什么不是多图并行压缩的？之前曾经尝试过多图并行压缩，但是因为在压缩的过程中，需要占用一定的内存，如果同时压缩9张5Mb左右大小的图片，很容易导致OOM，所以决定在解决OOM问题之前，多图压缩都是串行的。

## 关于OOM

如果用的是多图压缩，一定要考虑到OOM的风险，推荐大家使用 CUSTOM_GEAR, 然后自定义压缩指标，能够很大程度上降低OOM的风险，目前测试暂时没有发现过OOM的问题

## ChangeLog 

#### 1.3.4
- 更新RxJava2

#### 1.3.3
- 设置默认观察在主线程

#### 1.3.2 
- 优化内存占用

#### 1.3.1
- 增加自定义压缩格式
- 用CUSTOM_GEAR 解决了OOM的问题
- 重构了大部分代码
- 为了支持WebP，最低支持版本提高到14

## Issue
    
大家可以根据自己的需求选择不同的压缩模式以及调用方式 ｂ（￣▽￣）ｄ ！最后，欢迎大家提Issue

## TODO

- 取消旋转
- 增加各类compresser： JPEG，PNG，GIF，WEBP

## Thanks For
- https://github.com/Curzibn/Luban
- https://github.com/ReactiveX/Rxjava
- https://github.com/ReactiveX/RxAndroid

## License

    Copyright 2016 shaohui10086

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
	
 
