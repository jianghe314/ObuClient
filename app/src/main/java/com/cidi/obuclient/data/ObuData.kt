package com.cidi.obuclient.data

/**
 *Created by CIDI zhengxuan on 2023/11/28
 *QQ:1309873105
 */
data class ObuData(
    val selfSpeed: String = "0",
    val selfAcceSpeed: String = "0",
    val avgSpeeds: List<AvgSpeeds>,
    val avgDistances: List<AvgDistances>,
    val avgTimes: List<AvgTimes>,
    val deviceId: String = "000000",
    val gpsSate: String = "正常",
    val delayTime: Int = 200,
)

data class AvgSpeeds(
     val speed: Float
)

data class AvgDistances(
    val dist: Float
)

data class AvgTimes(
    val time: Float
)