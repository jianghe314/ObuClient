package com.cidi.obuclient

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.cidi.obuclient.utils.TTSUtil
import com.cidi.obuclient.utils.ThreadPoolUtils
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope

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
        private var path = ""

        fun MyToast(msg: String){
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
        }

        fun getMyApplicationContext(): Context{
            return context
        }

        fun setSavePath(filePath: String){
            path = filePath
        }

        fun getPath(): String{
            return path
        }


        fun getTvalues(): String? {
            val sp = context.getSharedPreferences("NMKT", MODE_PRIVATE)
            val t = sp.getString("t","5")
            return t
        }
    }





}