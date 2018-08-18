package com.example.gz.testmp3;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gz.testmp3.R;

import java.io.File;
import java.io.IOException;

import jaygoo.library.converter.MP3Converter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "MainActivity";
    private Button startButton;
    private Button stopButton;
    private Button startConverter;
    private Button obtainV;
    private TextView textVoice;
    private TextView addressVoice;
    private Button playMav;
    private Button playMp3;
    private final int PERMISSION_REQUEST = 0xa00;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.startBut);
        stopButton = findViewById(R.id.stopBut);
        startConverter = findViewById(R.id.startConverter);
        obtainV = findViewById(R.id.obtainV);
        textVoice = findViewById(R.id.textVoice);
        addressVoice = findViewById(R.id.addressVoice);
        playMav = findViewById(R.id.playMav);
        playMp3 = findViewById(R.id.playMp3);
        init();
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        startConverter.setOnClickListener(this);
        obtainV.setOnClickListener(this);
        playMp3.setOnClickListener(this);
        playMav.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST);
        }
        setSupportActionBar(toolbar);
    }

    /**
     * 权限
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_REQUEST:
                if(grantResults!=null && permissions!=null){
                    for(int i =0;i<grantResults.length;i++){
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(MainActivity.this,"读写文件没有权限",Toast.LENGTH_SHORT).show();
                        }
                        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                            Toast.makeText(MainActivity.this,"录音权限没有允许",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        }
    }
    MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.obtainV:
                String str = MP3Converter.getLameVersion();
                Log.e(TAG, "onClick: "+str);
                Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT);
                break;
            case R.id.startBut:
                Log.e(TAG, "onClick: 开始录音");
                recode(1);
                break;
            case R.id.stopBut:
                Log.e(TAG, "onClick: 停止录音");
                stop();
                break;
            case R.id.startConverter:
                Log.e(TAG, "onClick: 开始转码");
                startConverterMP3();
                break;
            case R.id.playMav:
                Log.e(TAG, "onClick: 播放mav文件" );
                String wavPath = AudioFileFuncWav.getDiskCachePath(MainActivity.this)+"/FinalAudio.wav";
                File file = new File(wavPath);
                if(file.exists()){
                    try {
                        if(!mediaPlayer.isPlaying())
                        {
                            mediaPlayer.setDataSource(file.getPath()); //为播放器设置mp3文件的路径
                            mediaPlayer.prepare(); //做好准备
                            mediaPlayer.start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.playMp3:
                Log.e(TAG, "onClick: 播放MP3文件" );
                final String mp3Path = AudioFileFuncWav.getDiskCachePath(MainActivity.this)+"/converter.mp3";
                File mp3File = new File(mp3Path);
                if(mp3File.exists()){
                    try {
                        if(!mediaPlayer.isPlaying())
                        {
                            mediaPlayer.setDataSource(mp3File.getPath()); //为播放器设置mp3文件的路径
                            mediaPlayer.prepare(); //做好准备
                            mediaPlayer.start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
    //注销
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    /**
     * 将录音文件wav转为MP3
     */
    long fileSize;
    long bytes = 0;
    private void startConverterMP3() {
        final String wavPath = AudioFileFuncWav.getDiskCachePath(MainActivity.this)+"/FinalAudio.wav";
        final String mp3Path = AudioFileFuncWav.getDiskCachePath(MainActivity.this)+"/converter.mp3";
        MP3Converter.init(44100,1,0,44100,96,9);
        fileSize = new File(wavPath).length();
        Log.e("fileSize",fileSize+"  大小");
        new Thread(new Runnable() {
            @Override
            public void run() {
                MP3Converter.convertMp3(wavPath,mp3Path);
            }
        }).start();
        handlerMP3.postDelayed(runnableMP3, 500);
    }
    Handler handlerMP3 = new Handler();
    Runnable runnableMP3 = new Runnable() {
        @Override
        public void run() {
            bytes = MP3Converter.getConvertBytes();
            float progress = (100f * bytes / fileSize);
            if (bytes == -1){
                progress = 100;
            }
            Log.e(TAG, "run: 转码" );
            if (handlerMP3 != null && progress != 100){
                Toast.makeText(MainActivity.this,"转码结束"+AudioFileFuncWav.getDiskCachePath(MainActivity.this)+"/converter.mp3",Toast.LENGTH_LONG).show();
                handlerMP3.postDelayed(this, 1000);
            }else{
                handlerMP3.removeCallbacksAndMessages(null);
            }
        }
    };

    private int mState = -1; //-1:没再录制，0：录制wav
    private final static int CMD_RECORDING_TIME = 2000;
    private final static int CMD_RECORDFAIL = 2001;
    private final static int CMD_STOP = 2002;
    private UIHandler uiHandler;
    private UIThread uiThread;
    /**
     * 开始录音
     * @param mFlag
     */
    public void recode(int mFlag){
        if(mState != -1){
            Log.e(mState+"1", "mState: "+mState );
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd",CMD_RECORDFAIL);
            b.putInt("msg", ErrorCode.E_STATE_RECODING);
            msg.setData(b);
            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            return;
        }
        int mResult = -1;
        AudioRecorderWav mRecord_1 = AudioRecorderWav.getInstance();
        mResult = mRecord_1.startRecordAndFile(MainActivity.this);
        if(mResult == ErrorCode.SUCCESS){
            mState = mFlag;
            uiThread = new UIThread();
            new Thread(uiThread).start();
        }else{
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd",CMD_RECORDFAIL);
            b.putInt("msg", mResult);
            msg.setData(b);
            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
        }
    }
    private void init(){
        uiHandler = new UIHandler();
    }
    private void stop(){
        if(mState != -1){
            AudioRecorderWav mRecord_1 = AudioRecorderWav.getInstance();
            mRecord_1.stopRecordAndFile();
        }
        if(uiThread != null){
            uiThread.stopThread();
        }
        if(uiHandler != null)
            uiHandler.removeCallbacks(uiThread);
        Message msg = new Message();
        Bundle b = new Bundle();// 存放数据
        b.putInt("cmd",CMD_STOP);
        b.putInt("msg", mState);
        msg.setData(b);
        uiHandler.sendMessageDelayed(msg,1000); // 向Handler发送消息,更新UI
        mState = -1;
    }

    class UIHandler extends Handler {
        public UIHandler() {
        }
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.d("MyHandler", "handleMessage......");
            super.handleMessage(msg);
            Bundle b = msg.getData();
            int vCmd = b.getInt("cmd");
            Log.e("vCmd", ""+vCmd);
            switch(vCmd)
            {
                case CMD_RECORDING_TIME:
                    int vTime = b.getInt("msg");
                    MainActivity.this.textVoice.setText("正在录音中，已录制："+vTime+" s");
                    Log.e("MyHandler", "正在录音中，已录制："+vTime+" s");
                    break;
                case CMD_RECORDFAIL:
                    int vErrorCode = b.getInt("msg");
                    String vMsg = ErrorCode.getErrorInfo(MainActivity.this, vErrorCode);
                    Log.e("MyHandler", "录音失败："+vMsg);
                    Toast.makeText(MainActivity.this,"录音失败："+vMsg,Toast.LENGTH_LONG).show();
                    break;
                case CMD_STOP:
                    AudioRecorderWav mRecord = AudioRecorderWav.getInstance();
                    long mSize = mRecord.getRecordFileSize();
                    Log.e("MyHandler", "录音已停止.录音文件:"+ AudioFileFuncWav.getWavFilePath(MainActivity.this)+"文件大小："+mSize);
                    Toast.makeText(MainActivity.this,"录音已停止.录音文件:"+AudioFileFuncWav.getWavFilePath(MainActivity.this)+"文件大小："+mSize,Toast.LENGTH_LONG).show();
                    addressVoice.setText("录音已停止.录音文件:"+AudioFileFuncWav.getWavFilePath(MainActivity.this));
                default:
                    break;
            }
        }
    }
    class UIThread implements Runnable {
        int mTimeMill = 0;
        boolean vRun = true;
        public void stopThread(){
            vRun = false;
        }
        public void run() {
            while(vRun){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mTimeMill ++;
                Message msg = new Message();
                Bundle b = new Bundle();// 存放数据
                b.putInt("cmd",CMD_RECORDING_TIME);
                b.putInt("msg", mTimeMill);
                Log.e(TAG+"2", "stop: "+mTimeMill );
                msg.setData(b);
                uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            }
        }
    }
}
