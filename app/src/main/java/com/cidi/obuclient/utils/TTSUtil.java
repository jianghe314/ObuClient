package com.cidi.obuclient.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.cidi.obuclient.MyApplication;

import java.util.Locale;

/**
 * Created by CIDI zhengxuan on 2019/9/7
 * QQ:1309873105
 */
public class TTSUtil {

    //语速
    private static final float VOICE_SPEED = 1.0f;
    //语调
    private static final float VOICE_PITCH = 1.0f;
    //播报模式
    private static final int SPEECH_MODE_ADD = 1;
    private static final int SPEECH_MODE_FLUSH = 0;

    private static TextToSpeech textToSpeech;


    public static void initTTS(Context context,TTSListener ttsListener){
        textToSpeech = new TextToSpeech(context,ttsListener,"com.iflytek.speechcloud");
        textToSpeech.setSpeechRate(VOICE_SPEED);
        textToSpeech.setPitch(VOICE_PITCH);
    }

    public synchronized static void speech(String text,int speechMode){
        textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,"123");
    }

    public synchronized static void speech(String text){
        textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,"123");
    }

    public static void onDestory(){
        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public static class  TTSListener implements TextToSpeech.OnInitListener{

        @Override
        public void onInit(int status) {
            if(status == TextToSpeech.SUCCESS){
                int supported = textToSpeech.setLanguage(Locale.CHINESE);
                if((supported != TextToSpeech.LANG_AVAILABLE)&&(supported != TextToSpeech.LANG_COUNTRY_AVAILABLE)){
                    Toast.makeText(MyApplication.Companion.getMyApplicationContext(),"不支持当前语言离线合成",Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(MyApplication.Companion.getMyApplicationContext(),"语音初始化失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

}



