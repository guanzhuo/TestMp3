package com.example.gz.testmp3;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class AudioFileFuncWav {
    //音频输入-麦克风
    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public final static int AUDIO_SAMPLE_RATE = 44100;  //44.1KHz,普遍使用的频率
    //录音输出文件
    private final static String AUDIO_RAW_FILENAME = "RawAudio.raw";
    private final static String AUDIO_WAV_FILENAME = "FinalAudio.wav";

    /**
     * 判断是否有外部存储设备sdcard
     * @return true | false
     */
    public static boolean isSdcardExit(){
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }
    /**
     * 获取麦克风输入的原始音频流文件路径
     * @return
     */
    public static String getRawFilePath(Context context ){
        String mAudioRawPath = "";
        if(isSdcardExit()){
            File file = new File(getDiskCachePath(context)+"/"+AUDIO_RAW_FILENAME);
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mAudioRawPath = getDiskCachePath(context)+"/"+AUDIO_RAW_FILENAME;
        }
        return mAudioRawPath;
    }

    /**
     * 获取编码后的WAV格式音频文件路径
     * @return
     */
    public static String getWavFilePath(Context context){
        String mAudioWavPath = "";
        if(isSdcardExit()){
            File file = new File(getDiskCachePath(context)+"/"+AUDIO_WAV_FILENAME);
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mAudioWavPath = getDiskCachePath(context)+"/"+AUDIO_WAV_FILENAME;
        }
        return mAudioWavPath;
    }

    /**
     * 获取文件大小
     * @param path,文件的绝对路径
     * @return
     */
    public static long getFileSize(String path){
        File mFile = new File(path);
        if(!mFile.exists())
            return -1;
        return mFile.length();
    }

    //获取缓存的地址
    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir().getPath();
        } else {
            return context.getCacheDir().getPath();
        }
    }
}
