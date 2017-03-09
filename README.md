# AdvancedLuban
[![build](https://img.shields.io/badge/build-1.3.5-brightgreen.svg?maxAge=2592000)](https://bintray.com/shaohui/maven/AdvancedLuban)
[![license](https://img.shields.io/badge/license-Apache%202-blue.svg?maxAge=2592000)](https://github.com/shaohui10086/AdvancedLuban/blob/master/LICENSE)


[中文版](/README_ZH.md)

![sketch](/image/sketch_map.png)


`AdvancedLuban` —— Is a convenient simple `Android` image compression tool library.Provides multiple compression strategies.Different calling methods，Custom compression,Multi-Image synchronous compression and so on,Focus on a better picture compression experience

Image Count | Origin Total size | Compressed Total size | Time Cost
--- | --- | --- | ---
1 | 4.3Mb | 85Kb | 0.23s
4 | 14.22Mb | 364Kb | 1.38s
9 | 36.23Mb | 745Kb | 4.43s


## Import

Maven

    <dependency>
      <groupId>me.shaohui.advancedluban</groupId>
      <artifactId>library</artifactId>
      <version>1.3.5</version>
      <type>pom</type>
    </dependency>

    
or Gradle

	compile 'me.shaohui.advancedluban:library:1.3.5'

## Usage


### `Listener` mode

`Advanced Luban` internal` Computation` thread for image compression, external calls simply set the Listener can be:

    Luban.compress(context, file)
        .putGear(Luban.THIRD_GEAR)      // set the compress mode, default is : THIRD_GEAR
        .launch(listener);              // start compression and set the listener

### `RxJava` mode

`RxJava` call the same default` Computation` thread to compress, you can also define any thread, can be observed in any thread:

    Luban.compress(context, file)                          
            .putGear(Luban.CUSTOM_GEAR)                 
            .asObservable()                             // generate Observable
            .subscribe(successAction, errorAction)      // subscribe the compress result

### Compression mode

    
#### 1. CUSTOM_GEAR

compress image file according to the restrictions you set, you can limit: the width, height or file size of the image file 
    
        Luban.compress(context, file)
                .setMaxSize(500)                // limit the final image size（unit：Kb）
                .setMaxHeight(1920)             // limit image height
                .setMaxWidth(1080)              // limit image width
                .putGear(Luban.CUSTOM_GEAR)     // use CUSTOM GEAR compression mode
                .asObservable()

#### 2. THIRD_GEAR 

Using custom algorithms, according to the picture aspect ratio, the picture is compressed quickly, the resulting image size is about 100Kb, for general compression, no file size limit and picture width limit

#### 3. FIRST_GEAR

The simplified version of `THIRD GEAR`, the compressed image resolution is less than 1280 x 720, the final file is less than 60Kb. suitable for fast compression, regardless of the final picture quality

## Multi-Image synchronous compression

If you choose to call the way `Listener`:

        Luban.get(this)
                .putGear(Luban.CUSTOM_GEAR)             
                .load(fileList)                     // load all images
                .launch(multiCompressListener);     // passing an OnMultiCompress Listener

or the `RxJava` way to use:

        Luban.get(this)
                .putGear(Luban.CUSTOM_GEAR)             
                .load(fileList)                     // load all images
                .asListObservable()                 // Generates Observable <List<File>. Returns the result of all the images compressed successfully

## About OOM

If you use a multi-map compression, we must take into account the risk of OOM, recommend you use CUSTOM_GEAR, and then customize the compression index, to a large extent reduce the risk of OOM, the current test did not find the problem of OOM

## ChangeLog

#### 1.3.5
- Add custom compression cache directory

#### 1.3.4
- update RxJava to RxJava2

#### 1.3.3
- Set the default observed in the main thread

#### 1.3.2
- Optimize memory usage

#### 1.3.1
- Added custom compression format
- COMO_GEAR used to solve the problem of OOM
- Refactored most of the code
- To support WebP, the minimum supported version is increased to 14

## Issue
    
You can according to your needs to choose a different compression mode and call mode ! ｂ（￣▽￣）ｄ ！Finally, I welcome the Issue

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
	
 
