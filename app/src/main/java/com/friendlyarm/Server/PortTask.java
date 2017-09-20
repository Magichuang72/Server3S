package com.friendlyarm.Server;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.friendlyarm.AndroidSDK.HardwareControler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.Executor;

/**
 * Created by magichuang on 17-9-14.
 */
public class PortTask extends TimerTask {
    int start = -1;
    Handler handler;
    int devfd;
    byte[] frame1 = new byte[26];
    int index = 0;
    private Executor executorService;
    Socket server_socket;
    int token;
    ArrayList<String> ips;
    ArrayList<Integer> ports;


    public PortTask(Executor executorService, Socket server_socket, int devfd, Handler handler, int token) {
        this.executorService = executorService;
        this.server_socket = server_socket;
        this.devfd = devfd;
        this.handler = handler;
        this.token = token;
        ips = new ArrayList<>();
        ports = new ArrayList<>();
        initSwitch();
        initServerSocket();
    }

    private void initServerSocket() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server_socket = new Socket("192.168.1.103", 3335);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    /**
     * @param b 需要向上位机发送的字节数组
     */
    private void send(final byte[] b) {
        if (server_socket==null){
            try {
                server_socket = new Socket("192.168.1.103", 3335);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (server_socket != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        server_socket = new Socket("192.168.1.103", 3335);
                        OutputStream outputStream = server_socket.getOutputStream();
                        outputStream.write(b);
                        outputStream.flush();
                        Log.i("SSSSSSSSSSSSSS", "ss");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            server_socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            executorService.execute(runnable);
        }
    }


    @Override
    public void run() {
        if (HardwareControler.select(devfd, 0, 0) == 1) {
            /**
             * 串口一接收到信息之后做的相关处理 现在是可见光
             * */
            byte[] temp = new byte[26];
            int read = HardwareControler.read(devfd, temp, 26);
            if (start == -1) {
                for (int i = 0; i < read - 1; i++) {
                    if (temp[i] == 0X0a && temp[i + 1] == 0x0d) {
                        start = i;
                        for (int j = start; j < read; j++) {
                            frame1[index] = temp[j];
                            index++;
                        }
                        break;
                    }
                }

            } else {
                for (int i = 0; i < read; i++) {

                    if (index == 26) {
                        index = 0;
//                        Message message = new Message();
//                        message.what = token;
//                        message.obj = frame1;
//                        handler.sendMessage(message);
                        byte[] frame = createFrame();
                        frame[30] = (byte) (3 * token - 1);
                        frame[50] = frame1[13];
                        frame[51] = frame1[14];
                        send(frame);
                        byte[] frame2send = createFrame();
                        frame2send[30] = (byte) (3 * token - 2);
                        frame2send[50] = frame1[11];
                        frame2send[51] = frame1[12];
                        send(frame2send);
                        sendToSwitch(3, frame);
                        sendToSwitch(1,frame2send);
                    }
                    frame1[index] = temp[i];
                    index++;
                }
            }
        }
    }

    private void sendToSwitch(final int what, final byte[] frame) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int token = what - 1;
                Socket socket = null;
                for (int i = 0; i < 3; i++) {
                    try {
                        socket = new Socket(ips.get(token), ports.get(token));
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(frame);
                        outputStream.flush();
                        Log.i("Send to Switch", String.valueOf(ips.get(token)));
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        token++;
                        if (token == 3) {
                            token = 0;
                        }
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
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void initSwitch() {
        ips.add("192.168.1.101");
        ports.add(3332);
        ips.add("192.168.1.102");
        ports.add(3331);
        ips.add("192.168.1.108");
        ports.add(3330);
    }

    public byte[] createFrame() {
        byte[] result = new byte[53];
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        String dateNowStr = sdf.format(d);
        char[] chars = dateNowStr.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            result[i] = (byte) chars[i];
        }

        result[34] = (byte) 192;
        result[35] = (byte) 168;
        result[36] = (byte) 1;
        result[37] = (byte) 110;

        result[52] = '\0';
        return result;
    }
}


