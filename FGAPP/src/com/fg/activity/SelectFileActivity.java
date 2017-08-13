package com.fg.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * 说明：主要用于选择文件操作
 */

public class SelectFileActivity extends Activity implements OnClickListener{

    public static final int FILE_CODE = 0;
    /***
     * 使用照相机拍照获取图片
     */
    public static final int SELECT_DOC_SD = 1;
    /***
     * 使用相册中的图片
     */
    public static final int SELECT_PIC_SD = 2;

    /***
     * 从Intent获取图片路径的KEY
     */
    public static final String KEY_FILE_PATH = "photo_path";

    private static final String TAG = "SelectFileActivity";

    private LinearLayout dialogLayout;
    private Button pickDocBtn,pickPhotoBtn,cancelBtn;

    /**获取到的图片路径*/
    private String filePath;

    private Intent lastIntent ;

    private Uri fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_pic_layout);
        initView();
    }
    /**
     * 初始化加载View
     */
    private void initView() {
        dialogLayout = (LinearLayout) findViewById(R.id.dialog_layout);
        dialogLayout.setOnClickListener(this);
        pickDocBtn = (Button) findViewById(R.id.btn_pick_doc);
        pickDocBtn.setOnClickListener(this);
        pickPhotoBtn = (Button) findViewById(R.id.btn_pick_photo);
        pickPhotoBtn.setOnClickListener(this);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);

        lastIntent = getIntent();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_layout:
                finish();
                break;
            case R.id.btn_pick_doc:
                pickDoc();
                break;
            case R.id.btn_pick_photo:
                pickPhoto();
                break;
            default:
                finish();
                break;
        }
    }

    /**
     * 拍照获取图片
     */
    private void pickDoc() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,SELECT_DOC_SD);
    }

    /***
     * 从相册中取图片
     */
    private void pickPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_PIC_SD);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK)
        {
            doFile(requestCode,data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 选择图片后，获取图片的路径
     * @param requestCode
     * @param data
     */
    private void doFile(int requestCode,Intent data)
    {
        //从相册取图片，有些手机有异常情况，请注意
        if(requestCode == SELECT_PIC_SD )
        {
            if (data == null) {
                Toast.makeText(this, "选择文件出错", Toast.LENGTH_LONG).show();
                return;
            }
            fileUri = data.getData();
            if (fileUri == null) {
                Toast.makeText(this, "选择文件出错", Toast.LENGTH_LONG).show();
                return;
            }

            String[] pojo = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(fileUri, pojo, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
                cursor.moveToFirst();
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
            Log.i(TAG, "imagePath = " + filePath);
            if (filePath != null && (filePath.endsWith(".png") || filePath.endsWith(".PNG") ||
                                       filePath.endsWith(".jpg") || filePath.endsWith(".JPG")))
            {
                lastIntent.putExtra(KEY_FILE_PATH, filePath);
                setResult(Activity.RESULT_OK, lastIntent);
                finish();
            } else {
                Toast.makeText(this, "选择文件不正确", Toast.LENGTH_LONG).show();
            }
        }

        //选取文件
        else if(requestCode == SELECT_DOC_SD ){
            if (data == null) {
                Toast.makeText(this, "选择文件出错", Toast.LENGTH_LONG).show();
                return;
            }
            fileUri = data.getData();
            if (fileUri == null) {
                Toast.makeText(this, "选择文件出错", Toast.LENGTH_LONG).show();
                return;
            }

            String[] pojo = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(fileUri, pojo, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
                cursor.moveToFirst();
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
            Log.i(TAG, "docPath = " + filePath);
            if (filePath != null && ( filePath.endsWith(".txt") || filePath.endsWith(".TXT") ||
                                       filePath.endsWith(".pdf") || filePath.endsWith(".PDF") ||
                                       filePath.endsWith(".docx") || filePath.endsWith(".DOCX") ||
                                       filePath.endsWith(".doc") || filePath.endsWith(".DOC")))
            {
                lastIntent.putExtra(KEY_FILE_PATH, filePath);
                setResult(Activity.RESULT_OK, lastIntent);
                finish();
            } else {
                Toast.makeText(this, "选择文件不正确", Toast.LENGTH_LONG).show();
            }
        }
    }
}
