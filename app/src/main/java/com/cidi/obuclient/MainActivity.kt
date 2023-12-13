package com.cidi.obuclient


import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.ViewModelProvider
import com.cidi.obuclient.data.CheckStateViewModel
import com.cidi.obuclient.ui.theme.*
import com.cidi.obuclient.utils.NettyUDPSocket
import com.cidi.obuclient.utils.TTSUtil
import com.cidi.obuclient.utils.ThreadPoolUtils
import com.cidi.obuclient.view.CustomEdit
import com.cidi.obuclient.view.SpeederMeter
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

class MainActivity : ComponentActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ObuClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = content_color
                ){
                    Content()
                }
            }
        }
    }

    @Composable
    fun Content(){
        var checkStateViewModel = ViewModelProvider(this@MainActivity).get(CheckStateViewModel::class.java)
        checkStateViewModel.isConnect.observe(this@MainActivity){
            if(!it){
                Toast.makeText(this@MainActivity,"数据连接断开",Toast.LENGTH_SHORT).show()
            }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Title(checkStateViewModel)
            ChoicePlan(checkStateViewModel)
            MyChoiceBox(checkStateViewModel)
            ShowData(checkStateViewModel)
        }
    }


    @Composable
    fun Title(checkStateViewModel: CheckStateViewModel){
        var deviceId by remember{ mutableStateOf("123456789") }
        var delayTime by remember{ mutableStateOf(999) }
        checkStateViewModel.obuData.observe(this@MainActivity){
            deviceId = it.carId.toString()
            delayTime = it.times.toInt()
            Log.e("delayTime","---->${delayTime}")
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(title_color)
            .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modifier = Modifier
                .align(Alignment.CenterVertically)
                .width(200.dp), text = "当前设备:${deviceId}",color = Color.White, fontSize = 15.sp, overflow = TextOverflow.Clip)
            Text(modifier = Modifier.align(Alignment.CenterVertically), text = "延迟:${delayTime}ms", color = Color.White, fontSize = 15.sp)
        }
    }
    
    @Composable
    fun ChoicePlan(checkStateViewModel: CheckStateViewModel){
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(title_color)) {
            ChoicePlanContent(checkStateViewModel)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChoicePlanContent(checkStateViewModel: CheckStateViewModel){
        val options = listOf( getString(R.string.plan_1),getString(R.string.plan_2),getString(R.string.plan_3),getString(R.string.plan_4),getString(R.string.plan_5),
            getString(R.string.plan_6))
        var expanded by remember { mutableStateOf(false) }
        var selectedOptionText by remember { mutableStateOf("") }
        var widthHeightSize  by remember { mutableStateOf(Size.Zero) }
        var icon = if(expanded){ painterResource(id = R.drawable.arrow_up) }else{
           painterResource(id = R.drawable.arrow_down)
        }

        Column() {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .onGloballyPositioned { it ->
                        widthHeightSize = it.size.toSize()
                    }, textStyle = TextStyle(fontSize = 16.sp),
                        value = selectedOptionText,
                        onValueChange = {selectedOptionText = it},
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            disabledBorderColor = Color.White,
                            disabledTextColor = Color.White,
                            disabledTrailingIconColor = Color.White
                        ),
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                icon,
                                "arrow",
                                modifier = Modifier.clickable { if(!checkStateViewModel.isLock()!!) expanded=!expanded else { false } })
                        },
                        label = {
                            Text(text = "方案选择", color = Color.White)
                        })
                        DropdownMenu(
                            modifier = Modifier.width(with(LocalDensity.current){widthHeightSize.width.toDp()}),
                            offset = DpOffset(10.dp,0.dp),
                            expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            options.forEach {
                                DropdownMenuItem(
                                    text = { Text(text = it, color = Color.Gray) },
                                    onClick = { selectedOptionText = it
                                                expanded = false
                                                Log.e("TAG","setPlan---->$it")
                                                checkStateViewModel.setPlan(it)
                                    }
                                )
                            }
                        }

        }

    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyChoiceBox(checkStateViewModel: CheckStateViewModel){
        var isShow by remember { mutableStateOf(true) }
        var isVoice by remember { mutableStateOf(false) }
        var isLock by remember { mutableStateOf(false) }
        var drapMenuExpanded by remember { mutableStateOf(false) }
        var menuItems = listOf(getString(R.string.rate_1),getString(R.string.rate_2),getString(R.string.rate_3),getString(R.string.rate_4),getString(R.string.rate_5),
                getString(R.string.rate_6),getString(R.string.rate_7),getString(R.string.rate_8))
        var itemChoiceText by remember { mutableStateOf( menuItems[0]) }
        var textWidthSize by remember { mutableStateOf( Size.Zero ) }
        var textValue by remember { mutableStateOf("N=k=M=${getSaveValue()[0]},T=${getSaveValue()[1]}") }

        Column(modifier = Modifier
            .background(title_color)
            .padding(horizontal = 15.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = getString(R.string.show), color = Color.White)
                    Checkbox(checked = isShow,
                        enabled = !checkStateViewModel.isLock()!!,
                        onCheckedChange = {
                            isShow = it
                            checkStateViewModel.setShow(it)
                                          },
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = Color.White,
                            checkedColor = check_color
                        ))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = getString(R.string.refresh_rate), color = Color.White)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(fontSize = 14.sp,
                        text = itemChoiceText,
                        color = if(checkStateViewModel.isLock() == true) Color.Gray else{ Color.White },
                        modifier = Modifier
                            .border(width = 1.dp, color = Color.White, shape = RectangleShape)
                            .height(22.dp)
                            .width(60.dp)
                            .clickable {
                                if (!checkStateViewModel.isLock()!!) {
                                    drapMenuExpanded = !drapMenuExpanded
                                } else {
                                    false
                                }

                            }
                            .onGloballyPositioned { textWidthSize = it.size.toSize() },
                        textAlign = TextAlign.Center
                    )
                    DropdownMenu(modifier = Modifier.width(60.dp)
                        , expanded = drapMenuExpanded
                        , offset = DpOffset(72.dp,0.dp)
                        , onDismissRequest = { drapMenuExpanded = false }) {
                        menuItems.forEach {
                            DropdownMenuItem(
                                modifier = Modifier.height(30.dp),
                                text = { Text(modifier = Modifier.wrapContentHeight(), fontSize = 10.sp, text = it,color = Color.Gray) },
                                onClick = { itemChoiceText = it
                                            drapMenuExpanded = false
                                            checkStateViewModel.setRefresh(it)
                                })
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.align(Alignment.CenterVertically),
                    verticalAlignment = Alignment.CenterVertically){
                    Text(text = getString(R.string.vioce),color = Color.White )
                    Checkbox(checked = isVoice,
                        enabled = !checkStateViewModel.isLock()!!,
                        onCheckedChange = {
                            isVoice = it
                            checkStateViewModel.setVoice(it)
                            if(it){
                                Toast.makeText(MyApplication.getMyApplicationContext(),"开启语音",Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(MyApplication.getMyApplicationContext(),"关闭语音",Toast.LENGTH_SHORT).show()
                            }

                                          },
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = Color.White,
                            checkedColor = check_color,

                        ))
                }

                Row(modifier = Modifier.align(Alignment.CenterVertically),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = getString(R.string.isLock), color = Color.White)
                    Switch(
                        modifier = Modifier
                            .width(70.dp)
                            .padding(10.dp, 0.dp, 0.dp, 0.dp),
                        colors = SwitchDefaults.colors(
                            checkedBorderColor = Color.White,
                            uncheckedBorderColor = Color.White,
                            uncheckedThumbColor = Color.White,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray,
                            checkedTrackColor = switch_track_color

                        ),
                        checked = isLock,
                        onCheckedChange = {
                            isLock = !isLock
                            checkStateViewModel.setLock(isLock)
                            if (isLock){
                                Toast.makeText(MyApplication.getMyApplicationContext(),"已上锁",Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(MyApplication.getMyApplicationContext(),"已解锁",Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                CustomEdit(text = textValue,
                    onValueChange = {
                        textValue = it
                                    },
                    onSave = {
                        if(!checkStateViewModel.isLock()!!){
                            if(!textValue.contains(",")){
                                Toast.makeText(this@MainActivity,"值与值用,号隔开",Toast.LENGTH_SHORT).show()
                            }else{
                                val str = textValue.split(",")
                                textValue = "NKM = ${str[0]},T = ${str[1]}"
                                saveValue(str[0],str[1])
                                Toast.makeText(this@MainActivity,"保存成功",Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(color = Color.White, shape = RoundedCornerShape(10))
                        .padding(start = 10.dp),
                    enabled = !checkStateViewModel.isLock()!!,
                    hint = " 请依次输入N=M=K，T的值",
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Gray)
                )
            }
        }
    }


    //显示各种结果
    @Composable
    fun ShowData(checkStateViewModel: CheckStateViewModel){
        var plan by remember{ mutableStateOf("") }
        var show by remember{ mutableStateOf(true) }
        var nkm by remember { mutableStateOf(getSaveValue()[0]) }
        var t by remember { mutableStateOf(getSaveValue()[1]) }

        //本车速度
        var myCarSpeed by remember { mutableStateOf(0) }
        //本车加速度
        var myCarAccSpeed by remember { mutableStateOf(0.0f) }
        //本车间距
        var myCarDis by remember{ mutableStateOf(0.0f) }
        //本车时距
        var myCarTime by remember{ mutableStateOf(0.0f) }
        //本车与第一辆车速度差
        var myCarSpeed0 by remember { mutableStateOf(0.0f) }
        //前5台车平均速度
        var avgFiveSpeeds by remember{ mutableStateOf(0.0f) }
        //前5台车平均间距
        var avgFiveDis by remember { mutableStateOf(0.0f) }
        //是否语音
        var isVoice by remember { mutableStateOf(false) }

        //前N台车平均间距
        var avgNDis by remember { mutableStateOf(0.0f) }
        //前N台车平均时距
        var avgNTime by remember { mutableStateOf(0.0f) }
        //前N台车平均速度
        var avgNSpeeds by remember { mutableStateOf(0.0f) }

        //前5台车在30S的平均速度
        var avgFiveSpeedsFor_30 by remember { mutableStateOf(0.0f) }
        checkStateViewModel.plan.observe(this@MainActivity){
            if(it != ""){
                plan = it.substring(0,1)
            }
        }
        checkStateViewModel.isShow.observe(this@MainActivity){
            show = it
        }
        checkStateViewModel.isVoice.observe(this@MainActivity){
            isVoice = it
        }
        checkStateViewModel.obuData.observe(this@MainActivity){
            myCarSpeed = it.carSpeed.toInt()
            myCarAccSpeed = it.carAccSpeed
            myCarDis = it.AvgDisList[0]
            myCarTime = it.AvgTimeList[0]
            myCarSpeed0 = it.AvgSpeedList[0]
            avgFiveSpeeds = (it.AvgSpeedList[0] + it.AvgSpeedList[1] + it.AvgSpeedList[2] + it.AvgSpeedList[3] + it.AvgSpeedList[4])/5
            avgFiveDis = (it.AvgDisList[0] +it.AvgDisList[1] +it.AvgDisList[2] +it.AvgDisList[3] +it.AvgDisList[4])/5
            val nkmValue = nkm?.toInt()!!
            var sumNSpeed = 0.0f
            var sumNDis = 0.0f
            var sumNTime = 0.0f
            for (i in 0..nkmValue){
                sumNSpeed += it.AvgSpeedList[i]
                sumNDis += it.AvgDisList[i]
                sumNTime += it.AvgTimeList[i]
            }
            avgNSpeeds = sumNSpeed/nkmValue
            avgNDis = sumNDis/nkmValue
            avgNTime = sumNTime/nkmValue

        }
        when(plan){
            "A"->{
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(10.dp)) {
                    if(show){
                        SpeedContent(myCarSpeed)
                        GetMyCarDistance("本车间距：", "${myCarDis}m")
                        GetMyCarDistance("本车时距：", "${myCarTime}s")
                        if(isVoice){
                            TTSUtil.speech("本车间距、时距分别为${myCarDis}、${myCarTime}")
                        }
                    }else{
                        Box(modifier = Modifier.fillMaxSize()){
                            Text(text = getString(R.string.hide),
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 26.sp,
                            color = Color.LightGray)
                        }
                    }
                }
            }
            "B"->{
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(10.dp)) {
                    if(show){
                        GetMyCarDistance("前5辆车平均速度：","${avgFiveSpeeds}KM/H")
                        if(isVoice){
                            TTSUtil.speech("前5辆车平均速度${avgFiveSpeeds}")
                        }
                    }else{
                        Box(modifier = Modifier.fillMaxSize()){
                            Text(text = getString(R.string.hide),
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 26.sp,
                                color = Color.LightGray)
                        }
                    }

                }
            }
            "C"->{
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(10.dp)) {
                    if(show){
                        SpeedContent(myCarSpeed)
                        GetMyCarDistance("前5辆车平均速度：","${avgFiveSpeeds}KM/H")
                        val advise = getDirverAdvise(myCarSpeed,myCarAccSpeed,myCarTime,myCarSpeed0)
                        DirverAdvise(advise)
                        if(isVoice){
                            TTSUtil.speech("前5辆车平均速度${avgFiveSpeeds}、行驶建议${advise}")
                        }
                    }else{
                        Box(modifier = Modifier.fillMaxSize()){
                            Text(text = getString(R.string.hide),
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 26.sp,
                                color = Color.LightGray)
                        }
                    }


                }
            }
            "D"->{
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(10.dp)) {
                    if(show){
                        GetMyCarDistance("前5辆车平均速度：","${avgFiveSpeeds}KM/H")
                        GetMyCarDistance("前5辆车平均间距：","${avgFiveDis}m")
                        if(isVoice){
                            TTSUtil.speech("前5辆车平均车速、平均间距分别为${avgFiveSpeeds}、${avgFiveSpeeds}")
                        }
                    }else{
                        Box(modifier = Modifier.fillMaxSize()){
                            Text(text = getString(R.string.hide),
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 26.sp,
                                color = Color.LightGray)
                        }
                    }
                }
            }
            "E"->{
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(10.dp)) {
                    if(show){
                        SpeedContent(myCarSpeed)
                        GetMyCarDistance("前N辆车平均速度：","${avgNSpeeds}KM/H")
                        GetMyCarDistance("前M辆车平均间距：","${avgNDis}m")
                        GetMyCarDistance("前K辆车平均时距：","${avgNTime}s")
                        val advise = getDirverAdvise(myCarSpeed,myCarAccSpeed,myCarTime,myCarSpeed0)
                        DirverAdvise(advise)
                        if(isVoice){
                            TTSUtil.speech("前${nkm}辆车平均速度、间距、时距分别为${avgNSpeeds}、${avgNDis}、${avgNTime}、行驶建议${advise}")
                        }
                    }else{
                        Box(modifier = Modifier.fillMaxSize()){
                            Text(text = getString(R.string.hide),
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 26.sp,
                                color = Color.LightGray)
                        }
                    }
                }
            }
            "F"->{
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(10.dp)) {
                    if(show){
                        val a = 0
                        GetMyCarDistance("前5辆车在30秒内平均速度：","${avgFiveSpeeds}KM/H")
                        if(isVoice){
                            TTSUtil.speech("前5辆车在30秒内平均速度${avgFiveSpeeds}千米每小时")
                        }
                    }else{
                        Box(modifier = Modifier.fillMaxSize()){
                            Text(text = getString(R.string.hide),
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 26.sp,
                                color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }


    //速度或者平均速度
    @Composable
    fun SpeedContent(mytargetValue: Int){
        val progressInt: Int by animateIntAsState(targetValue = mytargetValue)
        Column(modifier = Modifier
            .background(color = Color.White)
            .fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally) {
            val speedNum:Double = progressInt*1.5
            SpeederMeter(speedNum.toInt())

        }
    }

    //距离时距速度
    @Composable
    fun GetMyCarDistance(name:String,values: String){
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 10.dp)) {
            Text(
                text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Gray)){
                            append(name)
                        }
                        withStyle(style = SpanStyle(color = Color.Red, fontSize = 20.sp,fontWeight = FontWeight(1000))){
                            append("$values")

                        }
                }
            )
        }
    }

    //行驶建议
    @Composable
    fun DirverAdvise(values: String){
        val isLaunch: Boolean
        val adviseColor: Color
        var timer = Timer()
        var isVisible by remember { mutableStateOf(true) }
        if(values == "请加速"){
            isLaunch = false
            adviseColor = Color.Green
        }else if(values == "请减速"){
            isLaunch = true
            adviseColor = Color.Red
        }else{
            isLaunch = false
            adviseColor = Color.Black
        }
        LaunchedEffect(isLaunch){
            if(isLaunch){
                timer.scheduleAtFixedRate(timerTask{
                    isVisible = !isVisible
                },0,500L)
            }else{
                timer.cancel()
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(top = 10.dp)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)){
                        append(getString(R.string.dirverAdvise))
                    }
                }
            )
            AnimatedVisibility(visible = isVisible, enter = fadeIn(), exit = fadeOut()) {
               Text(text = values,color = adviseColor, fontSize = 20.sp,fontWeight = FontWeight(1000))
            }
        }
    }

    private fun getDirverAdvise(speed:Int,accSpeed:Float,avgSpeed0:Float,avgDis0:Float): String{
        val result = 0.3*(avgDis0 - speed*1.5 - 7)+0.3*avgSpeed0
        val values =  if(result > accSpeed + 0.1){
            "请加速"
        }else if(result <= accSpeed - 0.1){
            "请减速"
        }else{
            "请保持"
        }
        return values
    }


    private fun saveValue(nkm: String, t: String){
        val  sp = this@MainActivity.getSharedPreferences("NMKT", MODE_PRIVATE)
        val edit = sp.edit()
        edit.putString("nkm",nkm)
        edit.putString("t",t)
        edit.commit()
    }

    private fun getSaveValue(): Array<String?> {
        val sp = this@MainActivity.getSharedPreferences("NMKT", MODE_PRIVATE)
        val nkm = sp.getString("nkm","5")
        val t = sp.getString("t","5")
        return arrayOf(nkm,t)
    }


    override fun onDestroy() {
        super.onDestroy()
        NettyUDPSocket.onClose()
        ThreadPoolUtils.clearAllThread()
        TTSUtil.onDestory()
        exitProcess(0)
    }



}


