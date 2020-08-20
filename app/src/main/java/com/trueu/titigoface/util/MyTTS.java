package com.trueu.titigoface.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Colin
 * on 2020/8/19
 * E-mail: hecanqi168@gmail.com
 */
public class MyTTS extends UtteranceProgressListener implements TextToSpeech.OnInitListener  {
    private static final String TAG = MyTTS.class.getSimpleName();

    private static MyTTS mInstance;

    public static MyTTS getInstance() {
        if (mInstance == null) {
            synchronized (MyTTS.class) {
                if (mInstance == null) {
                    mInstance = new MyTTS();
                }
            }
        }
        return mInstance;
    }

    private MyTTS() {
        super();
    }

    private TextToSpeech mTextToSpeech;

    public void init(Context context) {
        mTextToSpeech = new TextToSpeech(context, this);
        mTextToSpeech.setOnUtteranceProgressListener(this);
    }

    @SuppressLint("NewApi")
    public void speak(String text) {
        if (mTextToSpeech != null)
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
    }

    public void release() {
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && mTextToSpeech != null) {
            int result = mTextToSpeech.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                    || result == TextToSpeech.ERROR) {
                Log.d(TAG, "onInit 数据丢失或语言不支持");
            }
            if (result == TextToSpeech.LANG_AVAILABLE) {
                Log.d(TAG, "onInit 支持该语言");
            }
            Log.d(TAG, "onInit ok");
        }
    }

    @Override
    public void onStart(String utteranceId) {
        Log.d(TAG, "onStart " + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.d(TAG, "onDone " + utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        Log.d(TAG, "onError " + utteranceId);
    }

}
