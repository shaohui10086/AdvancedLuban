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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

import static me.shaohui.advancedluban.Preconditions.checkNotNull;

public class Luban {

    public static final int FIRST_GEAR = 1;
    public static final int THIRD_GEAR = 3;
    public static final int CUSTOM_GEAR = 4;

    private static final String TAG = "Luban";
    private static String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";

    private static volatile Luban INSTANCE;

    private final File mCacheDir;

    private OnCompressListener compressListener;

    private File mFile;

    private List<File> mFileList;

    private int gear = THIRD_GEAR;

    private String filename;

    private int mMaxSize;
    private int mMaxHeight;
    private int mMaxWidth;

    protected Luban(File cacheDir) {
        mCacheDir = cacheDir;
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store
     * retrieved media and thumbnails.
     *
     * @param context A context.
     * @see #getPhotoCacheDir(Context, String)
     */
    private static File getPhotoCacheDir(Context context) {
        return getPhotoCacheDir(context, Luban.DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store
     * retrieved media and thumbnails.
     *
     * @param context A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getPhotoCacheDir(Context)
     */
    private static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    public static Luban get(Context context) {
        if (INSTANCE == null) INSTANCE = new Luban(Luban.getPhotoCacheDir(context));

        return INSTANCE;
    }

    public Luban clearCache() {
        if (mCacheDir.exists()) {
            deleteFile(mCacheDir);
        }
        return this;
    }

    @Deprecated
    public Subscription launch() {
        checkNotNull(mFile,
                "the image file cannot be null, please call .load() before this method!");

        return Observable.just(mFile)
                .map(new Func1<File, File>() {
                    @Override
                    public File call(File file) {
                        return compressImage(gear, file);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<File>empty())
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        compressListener.onStart();
                    }
                })
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        if (compressListener != null) compressListener.onSuccess(file);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        compressListener.onError(throwable);
                    }
                });
    }

    // for single file
    public Subscription launch(final OnCompressListener listener) {
        checkNotNull(listener, "the listener cannot be null !");

        if (mFile == null) {
            if (mFileList != null && !mFileList.isEmpty()) {
                mFile = mFileList.get(0);
            } else {
                throw new NullPointerException(
                        "the image file cannot be null, please call .load() before this method!");
            }
        }

        return Observable.just(mFile)
                .map(new Func1<File, File>() {
                    @Override
                    public File call(File file) {
                        return compressImage(gear, file);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<File>empty())
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        listener.onStart();
                    }
                })
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        listener.onSuccess(file);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        listener.onError(throwable);
                    }
                });
    }

    // for multi file
    public Subscription launch(final OnMultiCompressListener listener) {
        checkNotNull(listener, "the listener cannot be null !");

        if (mFileList == null) {
            if (mFile != null) {
                mFileList = new ArrayList<>();
                mFileList.add(mFile);
            } else {
                throw new NullPointerException(
                        "the file list cannot be null, please call .load() before this method!");
            }
        }

        List<Observable<File>> observables = new ArrayList<>();
        for (File file : mFileList) {
            observables.add(Observable.just(file).map(new Func1<File, File>() {
                @Override
                public File call(File file) {
                    return compressImage(gear, file);
                }
            }));
        }

        return Observable.zip(observables, new FuncN<List<File>>() {
            @Override
            public List<File> call(Object... args) {
                List<File> images = new ArrayList<>();
                for (Object o : args) {
                    if (o instanceof File) {
                        images.add((File) o);
                    }
                }
                return images;
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        listener.onStart();
                    }
                })
                .subscribe(new Action1<List<File>>() {
                    @Override
                    public void call(List<File> fileList) {
                        listener.onSuccess(fileList);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        listener.onError(throwable);
                    }
                });
    }

    public Luban load(File file) {
        mFile = file;
        return this;
    }

    public Luban load(List<File> fileList) {
        mFileList = fileList;
        return this;
    }

    @Deprecated
    public Luban setCompressListener(OnCompressListener listener) {
        compressListener = listener;
        return this;
    }

    public Luban putGear(int gear) {
        this.gear = gear;
        return this;
    }

    public Luban setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public Luban setMaxSize(int size) {
        this.mMaxSize = size;
        return this;
    }

    public Luban setMaxWidth(int width) {
        this.mMaxWidth = width;
        return this;
    }

    public Luban setMaxHeight(int height) {
        this.mMaxHeight = height;
        return this;
    }

    public Observable<File> asObservable() {
        return asObservable(Schedulers.computation());
    }

    public Observable<File> asObservable(Scheduler scheduler) {
        return asObservableCurrentThread().subscribeOn(scheduler);
    }

    public Observable<File> asObservableCurrentThread() {
        checkNotNull(mFile,
                "the image file cannot be null, please call .load() before this method!");

        return Observable.fromCallable(new Callable<File>() {
            @Override
            public File call() throws Exception {
                return compressImage(gear, mFile);
            }
        });
    }

    public Observable<List<File>> asListObservable() {
        return asListObservable(Schedulers.computation());
    }

    public Observable<List<File>> asListObservable(Scheduler scheduler) {
        return asListObservableCurrentThread().subscribeOn(scheduler);
    }

    public Observable<List<File>> asListObservableCurrentThread() {
        checkNotNull(mFileList,
                "the image list cannot be null, please call .load() before this method!");

        List<Observable<File>> observables = new ArrayList<>();
        for (File file : mFileList) {
            observables.add(Observable.just(file).map(new Func1<File, File>() {
                @Override
                public File call(File file) {
                    return compressImage(gear, file);
                }
            }));
        }
        return Observable.zip(observables, new FuncN<List<File>>() {
            @Override
            public List<File> call(Object... args) {
                List<File> images = new ArrayList<>();
                for (Object o : args) {
                    if (o instanceof File) {
                        images.add((File) o);
                    }
                }
                return images;
            }
        });
    }

    private File compressImage(int gear, File file) {
        switch (gear) {
            case THIRD_GEAR:
                return thirdCompress(file);
            case CUSTOM_GEAR:
                return customCompress(file);
            case FIRST_GEAR:
                return firstCompress(file);
            default:
                return file;
        }
    }

    private File thirdCompress(@NonNull File file) {
        String thumb = getCacheFilePath();

        double size;
        String filePath = file.getAbsolutePath();

        int angle = getImageSpinAngle(filePath);
        int width = getImageSize(filePath)[0];
        int height = getImageSize(filePath)[1];
        boolean flip = width > height;
        int thumbW = width % 2 == 1 ? width + 1 : width;
        int thumbH = height % 2 == 1 ? height + 1 : height;

        width = thumbW > thumbH ? thumbH : thumbW;
        height = thumbW > thumbH ? thumbW : thumbH;

        double scale = ((double) width / height);

        if (scale <= 1 && scale > 0.5625) {
            if (height < 1664) {
                if (file.length() / 1024 < 150) return file;

                size = (width * height) / Math.pow(1664, 2) * 150;
                size = size < 60 ? 60 : size;
            } else if (height >= 1664 && height < 4990) {
                thumbW = width / 2;
                thumbH = height / 2;
                size = (thumbW * thumbH) / Math.pow(2495, 2) * 300;
                size = size < 60 ? 60 : size;
            } else if (height >= 4990 && height < 10240) {
                thumbW = width / 4;
                thumbH = height / 4;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbW = width / multiple;
                thumbH = height / multiple;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (height < 1280 && file.length() / 1024 < 200) return file;

            int multiple = height / 1280 == 0 ? 1 : height / 1280;
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = (thumbW * thumbH) / (1440.0 * 2560.0) * 400;
            size = size < 100 ? 100 : size;
        } else {
            int multiple = (int) Math.ceil(height / (1280.0 / scale));
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = ((thumbW * thumbH) / (1280.0 * (1280 / scale))) * 500;
            size = size < 100 ? 100 : size;
        }

        return compress(filePath, thumb, flip ? thumbH : thumbW, flip ? thumbW : thumbH, angle,
                (long) size);
    }

    private File firstCompress(@NonNull File file) {
        int minSize = 60;
        int longSide = 720;
        int shortSide = 1280;

        String thumbFilePath = getCacheFilePath();
        String filePath = file.getAbsolutePath();

        long size = 0;
        long maxSize = file.length() / 5;

        int angle = getImageSpinAngle(filePath);
        int[] imgSize = getImageSize(filePath);
        int width = 0, height = 0;
        if (imgSize[0] <= imgSize[1]) {
            double scale = (double) imgSize[0] / (double) imgSize[1];
            if (scale <= 1.0 && scale > 0.5625) {
                width = imgSize[0] > shortSide ? shortSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = minSize;
            } else if (scale <= 0.5625) {
                height = imgSize[1] > longSide ? longSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = maxSize;
            }
        } else {
            double scale = (double) imgSize[1] / (double) imgSize[0];
            if (scale <= 1.0 && scale > 0.5625) {
                height = imgSize[1] > shortSide ? shortSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = minSize;
            } else if (scale <= 0.5625) {
                width = imgSize[0] > longSide ? longSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = maxSize;
            }
        }

        return compress(filePath, thumbFilePath, width, height, angle, size);
    }

    private File customCompress(@NonNull File file) {
        String thumbFilePath = getCacheFilePath();
        String filePath = file.getAbsolutePath();

        int angle = getImageSpinAngle(filePath);
        long fileSize =
                mMaxSize > 0 && mMaxSize < file.length() / 1024 ? mMaxSize : file.length() / 1024;

        int[] size = getImageSize(filePath);
        int width = size[0];
        int height = size[1];

        if (mMaxSize > 0 && mMaxSize < file.length() / 1024f) {
            // find a suitable size
            float scale = (float) Math.sqrt(file.length() / 1024f / mMaxSize);
            width = (int) (width / scale);
            height = (int) (height / scale);
        }

        // check the width&height
        if (mMaxWidth > 0) {
            width = Math.min(width, mMaxWidth);
        }
        if (mMaxHeight > 0) {
            height = Math.min(height, mMaxHeight);
        }
        float scale = Math.min((float) width / size[0], (float) height / size[1]);
        width = (int) (size[0] * scale);
        height = (int) (size[1] * scale);

        // 不压缩
        if (mMaxSize > file.length() / 1024f && scale == 1) {
            return file;
        }

        return compress(filePath, thumbFilePath, width, height, angle, fileSize);
    }

    private String getCacheFilePath() {
        String name;
        if (TextUtils.isEmpty(filename)) {
            name = System.currentTimeMillis() + ".jpg";
        } else {
            name = filename;
        }
        return mCacheDir.getAbsolutePath() + File.separator + name;
    }

    private void deleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File file : fileOrDirectory.listFiles()) {
                deleteFile(file);
            }
        }
        fileOrDirectory.delete();
    }

    /**
     * obtain the image's width and height
     *
     * @param imagePath the path of image
     */
    public static int[] getImageSize(String imagePath) {
        int[] res = new int[2];

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);

        res[0] = options.outWidth;
        res[1] = options.outHeight;

        return res;
    }

    /**
     * obtain the thumbnail that specify the size
     *
     * @param imagePath the target image path
     * @param width the width of thumbnail
     * @param height the height of thumbnail
     * @return {@link Bitmap}
     */
    private Bitmap compress(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int outH = options.outHeight;
        int outW = options.outWidth;
        int inSampleSize = 1;

        if (outH > height || outW > width) {
            int halfH = outH / 2;
            int halfW = outW / 2;

            while ((halfH / inSampleSize) > height && (halfW / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;

        options.inJustDecodeBounds = false;

        int heightRatio = (int) Math.ceil(options.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(options.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio;
            } else {
                options.inSampleSize = widthRatio;
            }
        }
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imagePath, options);
    }

    /**
     * obtain the image rotation angle
     *
     * @param path path of target image
     */
    private int getImageSpinAngle(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 指定参数压缩图片
     * create the thumbnail with the true rotate angle
     *
     * @param largeImagePath the big image path
     * @param thumbFilePath the thumbnail path
     * @param width width of thumbnail
     * @param height height of thumbnail
     * @param angle rotation angle of thumbnail
     * @param size the file size of image
     */
    private File compress(String largeImagePath, String thumbFilePath, int width, int height,
            int angle, long size) {
        Bitmap thbBitmap = compress(largeImagePath, width, height);

        thbBitmap = rotatingImage(angle, thbBitmap);

        return saveImage(thumbFilePath, thbBitmap, size);
    }

    /**
     * 旋转图片
     * rotate the image with specified angle
     *
     * @param angle the angle will be rotating 旋转的角度
     * @param bitmap target image               目标图片
     */
    private static Bitmap rotatingImage(int angle, Bitmap bitmap) {
        //rotate image
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        //create a new image
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
    }

    /**
     * 保存图片到指定路径
     * Save image with specified size
     *
     * @param filePath the image file save path 储存路径
     * @param bitmap the image what be save   目标图片
     * @param size the file size of image   期望大小
     */
    private File saveImage(String filePath, Bitmap bitmap, long size) {
        checkNotNull(bitmap, TAG + "bitmap cannot be null");

        File result = new File(filePath.substring(0, filePath.lastIndexOf("/")));

        if (!result.exists() && !result.mkdirs()) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int options = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);

        while (stream.size() / 1024 > size && options > 6) {
            stream.reset();
            options -= 6;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
        }
        bitmap.recycle();

        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(filePath);
    }
}