package com.friendlyarm.Server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by magichuang on 17-9-5.
 */
public class LocalComputing implements Runnable {
    Handler handler;
    ArrayList<byte[]> dataList;
    ArrayList<Bitmap> rawData;

    public LocalComputing(Handler handler, ArrayList<Bitmap> rawData) {
        this.handler = handler;
        this.rawData = rawData;

    }

    private void encodeData(ArrayList<Bitmap> rawData) {
        dataList = new ArrayList<>();
        int i = 1;
        for (Bitmap bm : rawData) {
            Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(bm, 200, 200);
            byte[] bytes = bitMap2Byte(resizeBmp);
            Log.i("Size encode", String.valueOf(bytes.length));
            dataList.add(bytes);
            System.out.println("encode " + i);
            i++;
            Message message = new Message();
            message.what = Client.PRE;
            message.obj = i;
            System.out.println(i);
            handler.sendMessage(message);
        }
        System.out.println("encode complete");
    }

    @Override
    public void run() {
        encodeData(rawData);
        for (int i = 0; i < rawData.size(); i++) {
            byte[] result = dataList.get(i);
            Message message = new Message();
            message.what = Client.LOCAL;
            Bitmap bitmap = byte2Bitmap(result);
            message.obj = gray2Binary(bitmap, 200, 200);
            handler.sendMessage(message);
            System.out.println("binary " + i);
            bitMap2Byte(bitmap);//offloading time
        }
        Message message = new Message();
        message.what = Client.LOCALEND;
        handler.sendMessage(message);
    }

    private byte[] bitMap2Byte(Bitmap rawBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rawBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private Bitmap byte2Bitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private Bitmap gray2Binary(Bitmap graymap, int w, int h) {
        int width, height;
        height = graymap.getHeight();
        width = graymap.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(graymap, 0, 0, paint);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(bmpGrayscale, w, h);
        Log.i("SIZE", String.valueOf(resizeBmp.getByteCount()));
        return resizeBmp;
    }

}
