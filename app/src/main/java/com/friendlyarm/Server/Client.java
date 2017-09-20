package com.friendlyarm.Server;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magichuang.server.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;


public class Client extends AppCompatActivity {

    public static final int PLAY = 99;
    public static final int PRE = 98;
    public static final int PRE2 = 97;
    public static final int LOCAL = 96;
    public static final int LOCALEND = 95;
    public static final int OFFLOADINGEND = 94;

    public static final int GETDATA = 93;
    ArrayList<Bitmap> rawData;
    Button button;
    Button localButton;
    Button playButton;
    Button refreshButton;
    Button addButton;
    Button removeButton;
    ImageView imageView;
    ImageView imageView2;
    TextView localTextView;
    TextView offLoadingTextView;
    ListView listView;
    EditText editText;
    EditText editText2;
    EditText editText3;
    ExecutorService executorService;
    int index;
    ArrayList<ArrayList<Bitmap>> pieces;
    ProgressBar progressBar;
    ProgressBar progressBar2;
    ProgressBar progressBar3;
    private long localStartTime;
    private long offLoadingStartTime;
    private ArrayList<String> ip;
    private ArrayList<String> connectedIP;
    private int pieceSize = 0;
    private int inteval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        initUI();
        initIpList();


    }

    private void initIpList() {
        connectedIP = new ArrayList<>();
        connectedIP.add("192.168.1.110");
        connectedIP.add("192.168.1.106");
        connectedIP.add("192.168.1.102");
        listView.setAdapter(new MyAdapter(connectedIP, this));
    }


    private void initUI() {
        ip = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        button = (Button) findViewById(R.id.button);
        localButton = (Button) findViewById(R.id.button2);
        playButton = (Button) findViewById(R.id.button3);
        refreshButton = (Button) findViewById(R.id.reButton);
        addButton = (Button) findViewById(R.id.addButton);
        removeButton = (Button) findViewById(R.id.removeButton);
        playButton.setVisibility(View.GONE);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        localTextView = (TextView) findViewById(R.id.localTextView);
        offLoadingTextView = (TextView) findViewById(R.id.offLoadingTextView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar3 = (ProgressBar) findViewById(R.id.progressBar3);
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                HashMap<Integer, Boolean> isSelected = adapter.getIsSelected();
                for (Map.Entry<Integer, Boolean> entry : isSelected.entrySet()) {
                    System.out.println(entry.getKey() + " " + entry.getValue());
                    if (entry.getValue()) {
                        int index = entry.getKey();
                        connectedIP.remove(index);
                        System.out.println("remomve" + entry.getKey());
                    }
                }
                System.out.println(connectedIP);
                listView.setAdapter(new MyAdapter(connectedIP, Client.this));
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempIp = editText.getText().toString();
                connectedIP.add(tempIp);
                listView.setAdapter(new MyAdapter(connectedIP, Client.this));
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread initDataThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                });
                initDataThread.start();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAdapter myAdapter = (MyAdapter) listView.getAdapter();
                ip = new ArrayList<String>();
                HashMap<Integer, Boolean> isSelected = myAdapter.getIsSelected();
                for (Map.Entry<Integer, Boolean> entry : isSelected.entrySet()) {

                    if (entry.getValue()) {
                        ip.add((String) myAdapter.getItem(entry.getKey()));
                    }
                }
                System.out.println("ip----------------" + ip);
                //拆分任务
                ArrayList<ArrayList<Bitmap>> datas = new ArrayList<ArrayList<Bitmap>>();
                int addIndex = 0;
                int pieces = 0;
                //得到可用节点数目
                for (int i = 0; i < ip.size(); i++) {
                    if (!ip.get(i).isEmpty()) {
                        pieces++;
                    }
                }
                for (int i = 0; i < pieces; i++) {
                    ArrayList<Bitmap> temp = new ArrayList<Bitmap>();
                    for (int j = 0; j < rawData.size() / pieces; j++) {
                        temp.add(rawData.get(addIndex));
                        addIndex++;
                    }
                    datas.add(temp);
                }
                initPieces(pieces);
                offLoadingStartTime = new Date().getTime();
                offLoadingTextView.setText("Time:");
                progressBar2.setMax(rawData.size() * 2);
                progressBar2.setProgress(0);
                index = 0;
                Thread[] threads = new Thread[pieces];
                for (int i = 0; i < pieces; i++) {
                    threads[i] = new Thread(new Offloading(handler, ip.get(i), datas.get(i), i + 1));
                    threads[i].setPriority(10);
                    threads[i].start();
                }
            }
        });
        localButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread localThread = new Thread(new LocalComputing(handler, rawData));
                localStartTime = new Date().getTime();
                initPieces(1);
                index = 0;
                progressBar.setMax(rawData.size() * 2);
                progressBar.setProgress(0);
                localThread.start();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread playThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        play();
                    }
                });
                playThread.start();
            }
        });
    }

    private void initPieces(int pieces) {
        this.pieces = new ArrayList<>();
        for (int i = 0; i < pieces; i++) {
            ArrayList<Bitmap> temp = new ArrayList<>();
            this.pieces.add(temp);
        }
        pieceSize = pieces;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what >= 1 && msg.what <= pieceSize) {
                Bitmap addBitmap = (Bitmap) msg.obj;
                imageView.setImageBitmap(addBitmap);
                pieces.get(msg.what - 1).add(addBitmap);
                index++;
                System.out.println(index);
                progressBar2.setProgress(progressBar2.getProgress() + 1);
                if (index == rawData.size()) {
                    playButton.setVisibility(View.VISIBLE);

                }
            } else if (msg.what == LOCAL) {
                index++;
                System.out.println(index);
                Bitmap addBitmap = (Bitmap) msg.obj;
                imageView.setImageBitmap(addBitmap);
                pieces.get(0).add(addBitmap);
                progressBar.setProgress(progressBar.getProgress() + 1);
                if (index == rawData.size()) {
                    playButton.setVisibility(View.VISIBLE);
                    index = 0;
                }
            } else if (msg.what == PLAY) {
                Bitmap playBitmap = (Bitmap) msg.obj;
                imageView.setImageBitmap(playBitmap);
                imageView2.setImageBitmap(rawData.get(msg.arg1));
            } else if (msg.what == PRE) {
                progressBar.setProgress(progressBar.getProgress() + 1);
            } else if (msg.what == PRE2) {
                progressBar2.setProgress(progressBar2.getProgress() + 1);
            } else if (msg.what == LOCALEND) {
                long costTime = new Date().getTime() - localStartTime;
                costTime = costTime / 1000;
                localTextView.setText("Local Time: " + String.valueOf(costTime) + " S ");
            } else if (msg.what == OFFLOADINGEND) {
                long costTime = new Date().getTime() - offLoadingStartTime;
                costTime = costTime / 1000;
                int index = (int) msg.obj;
                offLoadingTextView.append(ip.get(index - 1) + ":" + String.valueOf(costTime) + " S " + "\r\n");
            } else if (msg.what == GETDATA) {
                Toast.makeText(Client.this, "get data complete", Toast.LENGTH_SHORT).show();
            }

        }
    };


    private void initData() {

        rawData = new ArrayList<>();
        String dataPath = Environment.getExternalStorageDirectory() + "/testVideo.mp4";
        System.out.println("---------------" + dataPath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(dataPath);
        // 得到每一秒时刻的bitmap比如第一秒,第二秒
        int sumSize = Integer.parseInt(editText2.getText().toString());
        progressBar3.setMax(sumSize);
        progressBar3.setProgress(0);
        for (int i = 0; i < sumSize; i++) {
            inteval = Integer.parseInt(editText3.getText().toString());
            Bitmap bitmap = retriever.getFrameAtTime(i * 1000 * inteval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            rawData.add(bitmap);
            progressBar3.setProgress(progressBar3.getProgress() + 1);
        }
        System.out.println("complete");
        Message message = new Message();
        message.what = GETDATA;
        handler.sendMessage(message);

    }

    private void play() {
        final ArrayList<Bitmap> play = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            ArrayList<Bitmap> bitmaps = pieces.get(i);
            if (!bitmaps.isEmpty()) {
                for (Bitmap bm : bitmaps) {
                    play.add(bm);
                }
            }
        }
        for (int i = 0; i < rawData.size(); i++) {
            Message message = new Message();
            message.what = PLAY;
            message.obj = play.get(i);
            message.arg1 = i;
            handler.sendMessage(message);
            try {
                Thread.sleep(inteval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
