package me.shaohui.advancedlubanexample;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;
import me.shaohui.advancedluban.OnMultiCompressListener;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    private File mFile;

    private ImageView mImageView;

    private ImageView image1;
    private ImageView image2;
    private ImageView image3;


    private TextView mTextView;

    private List<File> mFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFileList = new ArrayList<>();

        mImageView = (ImageView) findViewById(R.id.image_result);

        image1 = (ImageView) findViewById(R.id.image_1);
        image2 = (ImageView) findViewById(R.id.image_2);
        image3 = (ImageView) findViewById(R.id.image_3);

        mTextView = (TextView) findViewById(R.id.text_view);

        //findViewById(R.id.add_image).setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Log.i("TAG", "选择图片");
        //        Intent intent = new Intent(Intent.ACTION_PICK,
        //                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //        startActivityForResult(intent, REQUEST_CODE);
        //    }
        //});

        //findViewById(R.id.compress_image).setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        compressImage();
        //        //showImageView();
        //    }
        //});
        findViewById(R.id.compress_image2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //compressImage2();
                //compressImage3();
                //showImageView();
                //compressImageList();
                //testCompressMemory(mFileList.get(0));
                //checkCallback();
                compressImageList();
            }
        });

        findViewById(R.id.add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //compressImageList();
                //Intent intent = new Intent(Intent.ACTION_PICK,
                //        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(intent, REQUEST_CODE);
                MultiImageSelector.create().count(9).multi().start(MainActivity.this, REQUEST_CODE);
            }
        });

        //findViewById(R.id.clear_cache).setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Luban.get(MainActivity.this).clearCache();
        //        Toast.makeText(MainActivity.this, "清除成功", Toast.LENGTH_SHORT).show();
        //    }
        //});
    }

    private long start;

    private void compressImage() {
        int size = 500;
        Luban.compress(this, mFileList.get(0))
                .setMaxSize(size)
                .setMaxHeight(1920)
                .setMaxWidth(1080)
                .putGear(Luban.THIRD_GEAR)
                .asObservable()
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //Log.i("TAG:origin", Formatter.formatFileSize(MainActivity.this, mFile
                        // .length()));
                        start = System.currentTimeMillis();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        Log.i("TAG:result",
                                Formatter.formatFileSize(MainActivity.this, file.length()));
                        //Log.i("TAG:result", file.getAbsolutePath());
                        Log.i("TAG:result",
                                "运行时间:" + (System.currentTimeMillis() - start) / 1000f + "s");
                        mImageView.setImageURI(Uri.parse(file.getPath()));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private void compressImageList() {
        Luban.compress(this, mFileList)
                .setMaxSize(100)
                .putGear(Luban.CUSTOM_GEAR)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .asListObservable()
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //Log.i("TAG:origin", Formatter.formatFileSize(MainActivity.this, mFile
                        // .length()));
                        start = System.currentTimeMillis();
                    }
                })
                .subscribe(new Action1<List<File>>() {
                    @Override
                    public void call(List<File> files) {
                        Log.i("TAG:result",
                                Formatter.formatFileSize(MainActivity.this, files.get(0).length()));
                        //Log.i("TAG:result", file.getAbsolutePath());
                        Log.i("TAG:result",
                                "运行时间:" + (System.currentTimeMillis() - start) / 1000f + "s");
                        mImageView.setImageURI(Uri.parse(files.get(0).getPath()));
                        image1.setImageURI(Uri.parse(files.get(1).getPath()));
                        image2.setImageURI(Uri.parse(files.get(2).getPath()));
                        image3.setImageURI(Uri.parse(files.get(3).getPath()));

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            //if (data != null && data.getData() != null) {
            //    Uri uri = data.getData();
            //    getPath(uri);
            //}
            mFileList.clear();
            List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            for (String str : path) {
                mFileList.add(new File(str));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
