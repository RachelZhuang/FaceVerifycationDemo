package com.example.zhuangli.faceverifycationdemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    private static final int PICK_PIC = 0x110;
    private static final int PICK_PIC2=0x111;
    private static final int MSG_SUCCESS =0x112 ;
    private static final int MSG_ERROR = 0x113;
    private static final int TAKE_PHOTO=1;
    private String  mCurImgPath;
    private String  mCurImgPath2;
    private Button mChoose;
    private Button mChoose2;
    private Button mCompare;
    private ImageView mPhoto;
    private ImageView mPhoto2;
    private Bitmap mPhotoImg;
    private TextView mSimil;
    private  MyHandler mHandler;
    private  Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();
        mHandler = new MyHandler(this);

    }

    private void initEvents() {
        mChoose.setOnClickListener(this);
        mChoose2.setOnClickListener(this);
        mCompare.setOnClickListener(this);
    }

    private void initViews() {

        mChoose= (Button) findViewById(R.id.id_choose1);
        mChoose2= (Button) findViewById(R.id.id_choose2);
        mPhoto= (ImageView) findViewById(R.id.id_photo);
        mPhoto2= (ImageView) findViewById(R.id.id_photo2);
        mCompare= (Button) findViewById(R.id.id_compare);
        mSimil= (TextView) findViewById(R.id.id_simil);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_choose1:
//                Intent intent=new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                startActivityForResult(intent,PICK_PIC);
                getFacePhoto();
                break;
            case R.id.id_choose2:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,PICK_PIC2);
                break;
            case R.id.id_compare:
                FaceComparer.compare(mCurImgPath, mCurImgPath2, new FaceComparer.CallBack() {
                    @Override
                    public void success(JSONObject result) {
                        Message msg = Message.obtain();
                        msg.what = MSG_SUCCESS;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message msg = Message.obtain();
                        msg.what = MSG_ERROR;
                        msg.obj = exception;
                        mHandler.sendMessage(msg);
                    }
                });

        }
    }

    private void getFacePhoto() {
        File outputImage=new File(Environment.getExternalStorageDirectory(),"tempImage.jpg");
        try{
            if (outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        imageUri=Uri.fromFile(outputImage);
        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PICK_PIC){
            if(data!=null){
                Uri uri=data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                int columnId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mCurImgPath= cursor.getString(columnId);
                cursor.close();
                resizePhoto();
               mPhoto.setImageBitmap(mPhotoImg);
            }
        }else if (requestCode==PICK_PIC2){
            if(data!=null){
                Uri uri=data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                int columnId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mCurImgPath2= cursor.getString(columnId);
                cursor.close();
                resizePhoto();
                mPhoto2.setImageBitmap(mPhotoImg);
        }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void resizePhoto() {

        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(mCurImgPath,options);

        float scaleWidth = ((float) 200) / options.outWidth;
        float scaleHeight = ((float) 200) / options.outHeight;

        Matrix mat=new Matrix();
        mat.postScale(scaleWidth,scaleHeight);

        options.inJustDecodeBounds=false;
        mPhotoImg=BitmapFactory.decodeFile(mCurImgPath,options);

        mPhotoImg = Bitmap.createBitmap(mPhotoImg, 0, 0, options.outWidth,options.outHeight
                , mat,true);

    }

    private static  class MyHandler extends Handler{
        private WeakReference<MainActivity> weakReference;

        public MyHandler( MainActivity mainActivity) {
            this.weakReference = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity=weakReference.get();
            switch (msg.what){
                case MSG_SUCCESS:
                    JSONObject jo= (JSONObject) msg.obj;
                    try {
                        final Double smilar = Double.valueOf(jo.getString("similarity"));
                        activity.mSimil.setText(smilar.toString());
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                    break;
                case MSG_ERROR:
                    break;
            }
            super.handleMessage(msg);

            String s=msg.toString();
        }
    }
}
