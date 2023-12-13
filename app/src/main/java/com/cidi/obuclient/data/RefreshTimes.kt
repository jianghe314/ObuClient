package com.cidi.obuclient.data

/**
 *Created by CIDI zhengxuan on 2023/11/28
 *QQ:1309873105
 */
class RefreshTimes {
    private val times0: String = "0Hz"
    private val times1: String = "1Hz"
    private val times2: String = "1/5Hz"
    private val times3: String = "1/10Hz"
    private val times4: String = "1/20Hz"
    private val times5: String = "1/30Hz"
    private val times6: String = "1/40Hz"
    private val times7: String = "1/50Hz"
    private val times8: String = "1/60Hz"

    fun get(times: String): Int{
       return when(times){
            times1->1
            times2->5
            times3->10
            times4->20
            times5->30
            times6->40
            times7->50
            times8->60
           else -> {1}
       }
    }
}