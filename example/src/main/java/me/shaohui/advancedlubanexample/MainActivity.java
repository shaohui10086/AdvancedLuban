package me.shaohui.advancedlubanexample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;
import me.shaohui.advancedluban.OnMultiCompressListener;


@SuppressLint("CheckResult")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LubanExample";

    private static final int REQUEST_CODE = 1;

    private List<File> mFileList;

    private List<ImageView> mImageViews;

    private RadioGroup mMethodGroup;

    private RadioGroup mGearGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFileList = new ArrayList<>();

        mImageViews = new ArrayList<>();
        mImageViews.add((ImageView) findViewById(R.id.image_1));
        mImageViews.add((ImageView) findViewById(R.id.image_2));
        mImageViews.add((ImageView) findViewById(R.id.image_3));
        mImageViews.add((ImageView) findViewById(R.id.image_4));
        mImageViews.add((ImageView) findViewById(R.id.image_5));
        mImageViews.add((ImageView) findViewById(R.id.image_6));
        mImageViews.add((ImageView) findViewById(R.id.image_7));
        mImageViews.add((ImageView) findViewById(R.id.image_8));
        mImageViews.add((ImageView) findViewById(R.id.image_9));

        mMethodGroup = (RadioGroup) findViewById(R.id.method_group);
        mGearGroup = (RadioGroup) findViewById(R.id.gear_group);

        findViewById(R.id.select_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiImageSelector.create().start(MainActivity.this, REQUEST_CODE);
            }
        });
        findViewById(R.id.compress_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compressImage();
            }
        });
    }

    private void compressImage() {
        int gear;
        switch (mGearGroup.getCheckedRadioButtonId()) {
            case R.id.custom_gear:
                gear = Luban.THIRD_GEAR;
                break;
            case R.id.third_gear:
                gear = Luban.THIRD_GEAR;
                break;
            case R.id.first_gear:
                gear = Luban.FIRST_GEAR;
                break;
            default:
                gear = Luban.THIRD_GEAR;
        }
        switch (mMethodGroup.getCheckedRadioButtonId()) {
            case R.id.method_listener:
                if (mFileList.size() == 1) {
                    compressSingleListener(gear);
                } else {
                    compressMultiListener(gear);
                }
                break;
            case R.id.method_rxjava:
                if (mFileList.size() == 1) {
                    compressSingleRxJava(gear);
                } else {
                    compressMultiRxJava(gear);
                }
                break;
            default:
        }
    }


    private void compressSingleRxJava(int gear) {
        if (mFileList.isEmpty()) {
            return;
        }
        printfFileInfo("压缩前", mFileList.get(0));
        Luban.compress(mFileList.get(0), getFilesDir())
                .putGear(gear)
                .ignoreBy(500)
                .asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.functions.Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        printfFileInfo("压缩后", file);
                        Log.i("TAG", file.getAbsolutePath());
                        mImageViews.get(0).setImageURI(Uri.fromFile(file));
                    }
                }, new io.reactivex.functions.Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private void compressMultiRxJava(int gear) {
        if (mFileList.isEmpty()) {
            return;
        }
        printfFileInfo("压缩前", mFileList);
        Luban.compress(this, mFileList)
                .putGear(gear)
                .ignoreBy(500)
                .asListObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.functions.Consumer<List<File>>() {
                    @Override
                    public void accept(List<File> files) {
                        printfFileInfo("压缩后", files);
                        int size = files.size();
                        while (size-- > 0) {
                            mImageViews.get(size).setImageURI(Uri.fromFile(files.get(size)));
                        }
                    }
                }, new io.reactivex.functions.Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * 单张图片压缩
     *
     * @param gear
     */
    private void compressSingleListener(int gear) {
        if (mFileList.isEmpty()) {
            return;
        }
        printfFileInfo("压缩前", mFileList.get(0));

        Luban.compress(mFileList.get(0), getFilesDir())
                .putGear(gear)
                .launch(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.i(TAG, "start");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.i("TAG", file.getAbsolutePath());
                        printfFileInfo("压缩后", file);
                        mImageViews.get(0).setImageURI(Uri.fromFile(file));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void compressMultiListener(int gear) {
        if (mFileList.isEmpty()) {
            return;
        }
        printfFileInfo("压缩前", mFileList);
        Luban.compress(this, mFileList)
                .putGear(gear)
                .launch(new OnMultiCompressListener() {
                    @Override
                    public void onStart() {
                        Log.i(TAG, "start");
                    }

                    @Override
                    public void onSuccess(List<File> fileList) {
                        printfFileInfo("压缩后", fileList);
                        int size = fileList.size();
                        while (size-- > 0) {
                            mImageViews.get(size).setImageURI(Uri.fromFile(fileList.get(size)));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && data != null) {
            mFileList.clear();
            List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            for (String str : path) {
                mFileList.add(new File(str));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void printfFileInfo(String prefix, File file) {
        Log.e(TAG, prefix + "FilePath:" + file.getAbsolutePath());
        Log.e(TAG, prefix + "FileSize:" + (file.length() / 1024));
    }

    private void printfFileInfo(String prefix, List<File> files) {
        for (int i = 0; i < files.size(); i++) {
            Log.e(TAG, prefix + "FilePath:" + files.get(i).getAbsolutePath());
            Log.e(TAG, prefix + "FileSize:" + (files.get(i).length() / 1024));
        }
    }
}
