package com.cidi.obuclient.data


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

/**
 *Created by CIDI zhengxuan on 2023/11/21
 *QQ:1309873105
 */
data class CheckState(
    private var plan:String = "",
    private var isShow:Boolean = false,
    private var refresh:Int = 1,
    private var isVoice:Boolean =false,
    private var isLock:Boolean = false
){
    fun setPlan(which: String){
        plan = which
    }
    fun getPlan():String{
        return plan
    }
    fun setShow(show:Boolean){
        isShow = show
    }
    fun isShow():Boolean{
        return isShow
    }
    fun setRefresh(times:Int){
        refresh = times
    }
    fun getRefresh():Int{
        return refresh
    }
    fun setVoice(voice:Boolean){
        isVoice = voice
    }
    fun isVoice():Boolean{
        return isVoice
    }
    fun setLock(lock:Boolean){
        isLock = lock
    }
    fun isLock():Boolean{
        return isLock
    }
}
