# AdvancedLuban
[`Luban`](https://github.com/Curzibn/Luban)（鲁班） —— `Android`图片压缩工具，仿微信朋友圈压缩策略。
而`AdvancedLuban` —— 是一个在`Luban`的基础上根据自己的一些业务需求, 进行了一些扩展, 完善了一些Luban的

## Feature
相较于`Luban`, `AdvancedLuban`做的最大的扩展就是提供了一个自定义的压缩, 可以根据自己的需求, 来限制图片的宽度、高度或者文件大小

        Luban.get(this)
                    .load(mFile)
                    .setMaxSize(500) // 单位是Kb
                    .setMaxHeight(1920)   // 三个限制条件可任意组合
                    .setMaxWidth(1080)
                    .putGear(Luban.CUSTOM_GEAR)
                    .asObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<File>() {
                        @Override
                        public void call(File file) {
                            // do anyting you want
                        }
                    });
至此 `AdvancedLuban` 已经有三种压缩方式可以选择:

        - CUSTOM_GEAR
        - THIRD_GEAR
        - FIRST_GEAR
        
如果你有明确的图片宽高或者文件大小限制的话, 那么选择`AdvancedLuban`的`CUSTOM_GEAR`是非常方便的, 反之, 你也可以选择`THIRD_GEAR` 或者`FIRST_GEAR`, 

具体后两种压缩模式的使用可以参考`Luban`项目下的使用说明

最大的欣慰就是这个`CUSTOM_GEAR`压缩相较于之前`Luban`, 时间效率上没有落后, 而且还有些许的提升

## Import

Maven

    <dependency>
      <groupId>me.shaohui.advancedluban</groupId>
      <artifactId>library</artifactId>
      <version>1.0</version>
      <type>pom</type>
    </dependency>

    
Gradle

	dependencies {
        compile 'me.shaohui.advancedluban:library:1.0'
     }


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
	
 
