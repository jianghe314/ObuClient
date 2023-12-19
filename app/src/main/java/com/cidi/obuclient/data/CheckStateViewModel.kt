package com.cidi.obuclient.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cidi.obuclient.MyApplication
import com.cidi.obuclient.utils.NettyUDPSocket
import kotlinx.coroutines.*
import org.json.JSONException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 *Created by CIDI zhengxuan on 2023/11/22
 *QQ:1309873105
 */
class CheckStateViewModel : ViewModel(),NettyUDPSocket.ConnectState{

    val PORT: Int = 8888
    val IP: String = "192.168.1.1"
    //UDP连接性
    var isConnect = MutableLiveData<Boolean>(true)

    //方案选择
    var plan = MutableLiveData<String>("")

    //是否显示
    var isShow = MutableLiveData<Boolean>(true)

    //刷新帧率
    var refresh = MutableLiveData<Int>(1)

    //是否语音
    var isVoice = MutableLiveData<Boolean>(false)

    //是否上锁
    var isLock = MutableLiveData<Boolean>(false)

    //OBU data实体
    var obuData = MutableLiveData<ReceiveData>()

    var avgSpeedList30 = ArrayList<Float>()
    var speedTList = ArrayList<Float>()
    var disTList = ArrayList<Float>()
    var timeTList = ArrayList<Float>()

    private  var isWork: Boolean = false;
    companion object{
        private var T = 5
        private var sumSpeed = 0.0f
        private var sumDis = 0.0f
        private var sumTime = 0.0f
        private var currentTime = 0L
        private lateinit var byteBuffer: ByteBuffer
        private var speedLists = ArrayList<Float>()
        private var disLists = ArrayList<Float>()
        private var timeList = ArrayList<Float>()
        private lateinit var myData: ReceiveData

    }


    var msg: String? = null

    @OptIn(DelicateCoroutinesApi::class)
    val job by lazy { GlobalScope }

    init {
        NettyUDPSocket.initNettyUdpSocket()
        NettyUDPSocket.ConnectStateListener(this)
        NettyUDPSocket.connect(IP,PORT)
        job.launch {
            toPostValue()
        }
        job.launch {
            checkConnect()
        }
    }


    fun setPlan(which: String){
        plan.value = which
    }
    fun getPlan():String?{
        return plan.value
    }
    fun setShow(show:Boolean){
        isShow.value = show
    }
    fun isShow():Boolean?{
        return isShow.value
    }
    fun setRefresh(times:String){
        refresh.value= RefreshTimes().get(times)
    }
    fun getRefresh():Int?{
        return refresh.value
    }
    fun setVoice(voice:Boolean){
        isVoice.value = voice
    }
    fun isVoice():Boolean?{
        return isVoice.value
    }
    fun setLock(lock:Boolean){
        isLock.value = lock
    }
    fun isLock():Boolean?{
        return isLock.value
    }

    private suspend fun toPostValue(){
        while (true){
            Log.e("ViewModel","---toPostValue---")
            if(isWork){
                //在这里进行数据赋值
                Log.e("ViewModel","---toPostValue--数据赋值--")
                obuData.postValue(myData)
                T = MyApplication.getTvalues()?.toInt()!!
            }
            delay(getRefresh()!!*1000L )
        }
    }


    private suspend fun checkConnect(){
        while (true){
            if(System.currentTimeMillis() - currentTime > 4000){
                //判断连接断开了
                if(isWork){
                    isWork = false
                    obuData.postValue(ReceiveData(0, 0.0f, 0.0f, 0, 0, 0, AvgSpeedList = speedLists, AvgDisList = disLists, AvgTimeList = timeList))
                    isConnect.postValue(false)
                }
            }else{
                isConnect.postValue(true)
            }
            delay(1000)
        }
    }


    override fun connectSuccess(result: ByteArray?, port: Int) {
        try {
            if(port == PORT){
                byteBuffer = result?.let { ByteBuffer.wrap(it) }!!
                //车辆数
                val num = byteBuffer.short
                Log.e("UDP","num--->${num}")
                //自车速度
                val speed = byteBuffer.short
                Log.e("UDP","speed--->${speed}")
                //自车加速度
                val accSpeed = byteBuffer.short
                Log.e("UDP","accSpeed--->${accSpeed}")
                //GPS状态
                val gpsState = byteBuffer.get()
                Log.e("UDP","gpsState--->${gpsState}")
                //车辆ID
                val carId = byteBuffer.getInt()
                Log.e("UDP","carId--->${carId}")
                //时间戳
                val time = byteBuffer.long
                Log.e("UDP","time--->${time}")
                //解析平均速度
                speedLists.clear()
                for (i in 1..9){
                    val avgSpeed = byteBuffer.short
                    speedLists.add(avgSpeed*0.01f)
                    Log.e("UDP","avgSpeed--->${avgSpeed}")
                }
                //解析平均距离
                disLists.clear()
                for (i in 1..9){
                    val avgDis = byteBuffer.short
                    disLists.add(avgDis*0.01f)
                    Log.e("UDP","avgDis--->${avgDis}")
                }
                //解析平均时距
                timeList.clear()
                for(i in 1..9){
                    val avgTime = byteBuffer.short
                    timeList.add(avgTime*0.1f)
                    Log.e("UDP","avgTime--->${avgTime}")
                }
                Log.e("UDP","timeStemp--->${System.currentTimeMillis()}")
                myData = ReceiveData(
                    carNum = num*1,
                    carSpeed = speed*0.01f,
                    carAccSpeed = accSpeed*0.01f,
                    gpsState = gpsState*1,
                    carId = carId,
                    times = System.currentTimeMillis() - time,
                    AvgSpeedList = speedLists,
                    AvgDisList = disLists,
                    AvgTimeList = timeList
                )
                currentTime = System.currentTimeMillis()
                isWork = true
            }else{
                isWork = false
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }


}

