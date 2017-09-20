package com.friendlyarm.Server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by magichuang on 17-9-5.
 */
public class Offloading implements Runnable {
    Handler handler;
    String ip;
    Socket socket;
    ArrayList<byte[]> dataList;
    ArrayList<Bitmap> rawData;
    int token;

    public Offloading(Handler handler, String ip, ArrayList<Bitmap> rawData, int token) {
        this.handler = handler;
        this.ip = ip;
        this.token = token;
        this.rawData = rawData;

    }

    private void encodeData(ArrayList<Bitmap> rawData) {
        dataList = new ArrayList<>();
        int i = 1;
        for (Bitmap bm : rawData) {
            Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(bm, 200, 200);
            dataList.add(bitMap2Byte(resizeBmp));
            System.out.println("encode " + i);
            i++;
            Message message = new Message();
            message.what = Client.PRE2;
            message.obj = i;
            System.out.println(i);
            handler.sendMessage(message);
        }
    }

    @Override
    public void run() {
        encodeData(rawData);
        openNetWork();
        sendData();
        receiveData();

    }


    private byte[] bitMap2Byte(Bitmap rawBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rawBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private Bitmap byte2Bitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void openNetWork() {
        try {
            System.out.println(ip);
            socket = new Socket(ip, 9999);
            System.out.println("socket establish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void receiveData() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            for (int i = 0; i < rawData.size(); i++) {
                byte[] result = (byte[]) in.readObject();
                Message message = new Message();
                message.what = token;
                Bitmap bitmap = byte2Bitmap(result);
                message.obj = bitmap;
                handler.sendMessage(message);
                System.out.println("rece " + i);
            }
            Message message = new Message();
            message.what = Client.OFFLOADINGEND;
            message.obj = token;
            handler.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendData() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            System.out.println("write start");
            out.writeObject(dataList);
            System.out.println("Data Size :" + dataList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
