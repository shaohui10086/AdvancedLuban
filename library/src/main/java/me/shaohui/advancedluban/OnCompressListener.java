/*Copyright 2016 Zheng Zibin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package me.shaohui.advancedluban;

import java.io.File;

public interface OnCompressListener {

    /**
     * Fired when the compression is started, override to handle in your own code
     */
    void onStart();

    /**
     * Fired when a compression returns successfully, override to handle in your own code
     */
    void onSuccess(File file);

    /**
     * Fired when a compression fails to complete, override to handle in your own code
     */
    void onError(Throwable e);

}
