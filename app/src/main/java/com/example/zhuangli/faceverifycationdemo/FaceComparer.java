package com.example.zhuangli.faceverifycationdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by zhuangli on 2016/5/10.
 */
public class FaceComparer {

    public interface CallBack{
        void success(JSONObject result);

        void error(FaceppParseException exception);
    }

    public static void compare(final String filePath1,final String filePath2,final CallBack callBack){
       new Thread(new Runnable() {
           @Override
           public void run() {

               HttpRequests httpRequests=new HttpRequests(Constant.KEY,Constant.SECRET,true,true);
               try {
                   //获取第一张图片的信息
                   byte[] array1 = imageProcessing(filePath1);
                   JSONObject result1 = httpRequests.detectionDetect(new PostParameters().setImg(array1));
                   String face1=result1.getJSONArray("face").getJSONObject(0).getString("face_id");
                   System.out.println("face1的id=" + face1);
                   //获取第二张图片的信息
                   byte[] array2 = imageProcessing(filePath2);
                   JSONObject result2 = httpRequests.detectionDetect(new PostParameters().setImg(array2));
                   String face2 = result2.getJSONArray("face").getJSONObject(0).getString("face_id");
                   System.out.println("face2的id=" + face2);

                   System.out.println("开始比较：");
                   //对比两张人脸的相似程度
                   JSONObject Compare = httpRequests.recognitionCompare(new PostParameters().setFaceId1(face1).setFaceId2(face2));
                   if (callBack!=null) {
                       callBack.success(Compare);
                   }
                  // final Double smilar = Double.valueOf(Compare.getString("similarity"));

                   }catch (NumberFormatException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
               } catch (FaceppParseException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
                   if (callBack!=null) {
                       callBack.error(e);
                   }
               } catch (JSONException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
               }
               }

       }) .start();
    }
    // 处理图片
    private static byte[] imageProcessing(final String Path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
       BitmapFactory.decodeFile(Path, options);
        options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max((double) options.outWidth / 1024f,(double) options.outHeight / 1024f)));

        options.inJustDecodeBounds = false;
        Bitmap img = BitmapFactory.decodeFile(Path, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        float scale = Math.min(1,Math.min(600f / img.getWidth(), 600f / img.getHeight()));
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(),img.getHeight(), matrix, false);

        imgSmall.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] array = stream.toByteArray();
        return array;
    }


}
