package com.cidi.obuclient.data

/**
 *Created by CIDI zhengxuan on 2023/12/5
 *QQ:1309873105
 */
data class ReceiveData(
    //车辆数 2字节
    var carNum: Int = 0,
    //自车速度 2字节
    var carSpeed: Float = 0.0f,
    //自车加速度 2字节
    var carAccSpeed: Float = 0.0f,
    //GPS状态 1字节
    var gpsState: Int = 0,
    //自车ID 4字节
    var carId: Int = 0,
    //时间戳 8字节
    var times: Long = 0,
    //平均车速 18字节
    var AvgSpeedList: List<Float>,
    //平均距离 18字节
    var AvgDisList: List<Float>,
    //平均时距 18字节
    var AvgTimeList: List<Float>,
    //ID列表
    var DeviceIdList: List<String>,
    //在T秒内的平均速度、平均间距、平均时距
    var speedTValue: Float = 0.0f,
    var disTValue: Float = 0.0f,
    var timeTValue: Float = 0.0f

)
