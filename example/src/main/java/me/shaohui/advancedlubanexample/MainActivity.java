package me.shaohui.advancedlubanexample;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.github.piasy.fresco.draweeview.shaped.ShapedDraweeView;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    private File mFile;

    private ImageView mImageView;

    private TextView mTextView;

    private List<File> mFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fresco.initialize(this);

        mFileList = new ArrayList<>();

        mImageView = (ImageView) findViewById(R.id.image_result);

        mTextView = (TextView) findViewById(R.id.text_view);

        findViewById(R.id.select_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        findViewById(R.id.compress_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compressImage();
                //showImageView();
            }
        });
        findViewById(R.id.compress_image2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //compressImage2();
                compressImage3();
                //showImageView();
                compressImageList();
            }
        });
        findViewById(R.id.add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compressImageList();
            }
        });
    }

    private void showImageView() {
        //ShapedDraweeView shapedDraweeView = (ShapedDraweeView) findViewById(R.id.show_image);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(
                        "file:///storage/emulated/0/Pictures/1476073000559.jpg"))
                .build();
        //shapedDraweeView.setController(controller);
    }

    private long start;

    private void compressImage() {
        int size = 500;
        Luban.get(this)
                .load(mFile)
                .setMaxSize(size)
                .setMaxHeight(1920)
                .setMaxWidth(1080)
                .putGear(Luban.CUSTOM_GEAR)
                .asObservable()
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //Log.i("TAG:origin", Formatter.formatFileSize(MainActivity.this, mFile.length()));
                        start = System.currentTimeMillis();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        Log.i("TAG:result", Formatter.formatFileSize(MainActivity.this, file.length()));
                        //Log.i("TAG:result", file.getAbsolutePath());
                        Log.i("TAG:result", "运行时间:" + (System.currentTimeMillis() - start) / 1000f + "s");
                        mImageView.setImageURI(Uri.parse(file.getPath()));
                    }
                });
    }

    private void compressImage2() {
        Luban.get(this)
                .load(mFile)
                .putGear(Luban.THIRD_GEAR)
                .asObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        start = System.currentTimeMillis();
                    }
                })
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        Log.i("TAG:result", Formatter.formatFileSize(MainActivity.this, file.length()));
                        Log.i("TAG:result", "运行时间:" + (System.currentTimeMillis() - start) / 1000f + "s");
                        //Log.i("TAG:result", file.getAbsolutePath());
                        mImageView.setImageURI(Uri.parse(file.getPath()));
                    }
                });
    }

    private void compressImage3() {
        Luban.get(this).load(mFile).putGear(Luban.THIRD_GEAR).launch(new OnCompressListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(File file) {
                mImageView.setImageURI(Uri.parse(file.getPath()));
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private void compressImageList() {
        Luban.get(this)
                .load(mFileList)
                .setMaxSize(500)
                .putGear(Luban.CUSTOM_GEAR)
                .asListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<File>>() {
                    @Override
                    public void call(List<File> fileList) {
                        String str = "";
                        for (File file : fileList) {
                            str = str + file.getPath() + "\n";
                        }
                        mTextView.setText(str);
                        mImageView.setImageURI(Uri.parse(fileList.get(fileList.size() - 1).getPath()));
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                getPath(uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            mFile = new File(path);
            mFileList.add(mFile);
            Log.i("TAG:origin", Formatter.formatFileSize(this, mFile.length()));
            cursor.close();
        }
    }
}
