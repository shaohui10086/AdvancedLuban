# AdvancedLuban
[![build](https://img.shields.io/badge/build-1.1-brightgreen.svg?maxAge=2592000)](https://bintray.com/shaohui/maven/AdvancedLuban)
[![license](https://img.shields.io/badge/license-Apache%202-blue.svg?maxAge=2592000)](https://github.com/shaohui10086/AdvancedLuban/blob/master/LICENSE)


> [`Luban`](https://github.com/Curzibn/Luban)（鲁班） —— `Android`图片压缩工具，仿微信朋友圈压缩策略。

`AdvancedLuban` —— 在`Luban`的基础上根据一些需求, 进行了扩展, 增加了一些新特性，自定义压缩，多图同步压缩，专注更好的图片压缩体验

## Import

Maven

    <dependency>
      <groupId>me.shaohui.advancedluban</groupId>
      <artifactId>library</artifactId>
      <version>1.1</version>
      <type>pom</type>
    </dependency>

    
or Gradle

	compile 'me.shaohui.advancedluban:library:1.1'

## Usage


### `Listener`方式

`Luban`内部采用`IO`线程进行图片压缩，外部调用只需设置好结果监听即可：

    Luban.get(this)                     // 初始化Luban
        .load(File)                     // 传人要压缩的图片
        .putGear(Luban.THIRD_GEAR)      // 设定压缩模式，默认 THIRD_GEAR
        .launch(listener);              // 启动压缩并设置监听

### `RxJava`方式

`RxJava`调用方式同样默认`IO`线程进行压缩，可在任意线程观察：

    Luban.get(this)                                     
            .load(file)                               
            .putGear(Luban.CUSTOM_GEAR)                 
            .asObservable()                             // 生成Observable
            .subscribe(successAction, errorAction)      // 订阅压缩事件

### 压缩模式

    
#### 1. CUSTOM_GEAR

`AdvancedLuban`增加的个性化压缩，根据限制要求对图片进行压缩，可以限制：图片的宽度、高度以及图片文件的大小
    
        Luban.get(this)
                .load(mFile)
                .setMaxSize(500)                // 限制最终图片大小（单位：Kb）
                .setMaxHeight(1920)             // 限制图片高度
                .setMaxWidth(1080)              // 限制图片宽度
                .putGear(Luban.CUSTOM_GEAR)     // 使用 CUSTOM_GEAR 压缩模式
                .asObservable()

#### 2. THIRD_GEAR 

原`Luban`的主要功能，提供了类似微信的压缩效果，适用于普通压缩，没有文件大小限制以及图片的宽高限制

#### 3. FIRST_GEAR

`THIRD_GEAR`的简化版本，压缩之后的图片分辨率小于 1280 x 720, 文件最后小于60Kb，特殊情况下，小于原图片的1/5，适用于快速压缩，不计较最终图片品质

## 多图同步压缩

如果你选择的调用方式的是`Listener`方式:

        Luban.get(this)
                .putGear(Luban.CUSTOM_GEAR)             
                .load(fileList)                     // 加载所有图片
                .launch(multiCompressListener);     // 传入一个 OnMultiCompressListener 

`RxJava` 方式：

        Luban.get(this)
                .putGear(Luban.CUSTOM_GEAR)             
                .load(fileList)                     // 加载所有图片
                .asListObservable()                 // 生成Observable<List> 返回压缩成功的所有图片结果

## Issue
    
大家可以根据自己的需求选择不同的压缩模式以及调用方式 ｂ（￣▽￣）ｄ ！最后，欢迎大家提Issue

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
	
 
