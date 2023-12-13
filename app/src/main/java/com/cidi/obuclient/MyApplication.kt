package com.cidi.obuclient

import android.app.Application
import android.content.Context
import com.cidi.obuclient.utils.TTSUtil
import com.cidi.obuclient.utils.ThreadPoolUtils
import com.google.gson.Gson

/**
 *Created by CIDI zhengxuan on 2023/11/21
 *QQ:1309873105
 */
class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        context = this
        ThreadPoolUtils.InitThreadPool()
        TTSUtil.initTTS(this,TTSUtil.TTSListener())
    }

    companion object{
        private lateinit var context: Context
        fun getMyApplicationContext(): Context{
            return context
        }

        fun getGson(): Gson{
            return Gson()
        }
    }



}