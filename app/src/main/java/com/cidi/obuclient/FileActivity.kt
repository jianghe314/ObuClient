package com.cidi.obuclient

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.TypedArrayUtils
import androidx.lifecycle.ViewModelProvider
import com.cidi.obuclient.data.FileData
import com.cidi.obuclient.data.FileViewModel
import com.cidi.obuclient.ui.theme.ObuClientTheme
import com.cidi.obuclient.ui.theme.content_color
import com.cidi.obuclient.ui.theme.title_color
import com.google.accompanist.permissions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.Permission
import java.util.jar.Manifest

/**
 *Created by CIDI zhengxuan on 2023/12/22
 *QQ:1309873105
 */
class FileActivity: ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ObuClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = content_color
                ){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
                        getPermission()
                    }else{
                        ListContent()
                    }

                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun getPermission(){
        val permissionList = listOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE)
        val multiplePermissionsState = rememberMultiplePermissionsState(permissions = permissionList)
        var showPermission by remember { mutableStateOf(true) }
        if(multiplePermissionsState.allPermissionsGranted){
            ListContent()
        }else{
            PermissionsRequired(
                multiplePermissionsState = multiplePermissionsState,
                permissionsNotGrantedContent = {
                    if(showPermission){
                        AlertDialog(
                            onDismissRequest = { showPermission = false },
                            title = { Text(text = "授权", fontSize = 20.sp)},
                            text = { Text(text = "需要获取存取权限才能下载文件", fontSize = 16.sp)},
                            confirmButton = { Text(text = "确定", modifier = Modifier
                                .padding(bottom = 25.dp, end = 25.dp)
                                .clickable {
                                    multiplePermissionsState.launchMultiplePermissionRequest()
                                    Log.e("PPP", "---->授权")
                                    showPermission = false
                                })}

                        )
                    }
                                               },
                permissionsNotAvailableContent = {
                    if(showPermission){
                        AlertDialog(
                            onDismissRequest = { showPermission = false },
                            title = { Text(text = "授权",fontSize = 20.sp)},
                            text = { Text(text = "需要获取存取权限才能下载文件",fontSize = 16.sp)},
                            confirmButton = { Text(text = "确定", modifier = Modifier
                                .padding(bottom = 25.dp, end = 25.dp)
                                .clickable {
                                    multiplePermissionsState.launchMultiplePermissionRequest()
                                    Log.e("PPP", "---->授权")
                                    showPermission = false
                                })}

                        )
                    }
                },
                content = {
                    ListContent()
                })
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ListContent(){
        val fileViewModel = ViewModelProvider(this@FileActivity).get(FileViewModel::class.java)
        val refreshScope = rememberCoroutineScope()
        var refreshing by remember { mutableStateOf(false) }
        var list by remember { mutableStateOf(ArrayList<FileData>()) }
        var showDialog = remember { mutableStateOf(false) }
        var itemFile by remember { mutableStateOf(FileData("","")) }
        var itemIndex by remember { mutableStateOf(0) }
        fun refresh() = refreshScope.launch(Dispatchers.IO) {
            refreshing = true
            fileViewModel.getFTPFileLists()
            refreshing = false
        }
        val state = rememberPullRefreshState(refreshing, ::refresh, refreshThreshold = 100.dp,refreshingOffset = 100.dp)
        fileViewModel.FilesList.observe(this@FileActivity){
            list = it
        }
        fileViewModel.createFileDir()
        ShowDialog(itemIndex = itemIndex ,itemFile = itemFile, isShow =showDialog ,fileViewModel)
        Box(Modifier.pullRefresh(state)) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp)) {
                val size = list.size
                items(size){
                    Text(modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp)
                        .fillMaxWidth()
                        .clickable {
                            //点击下载删除
                            showDialog.value = true
                            itemFile = list[it]
                            itemIndex = it
                        }
                        .height(35.dp), color = Color.Gray, text = list[it].fileName, fontSize = 16.sp)
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray))
                }
            }

            PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
            Title()
        }


    }

    @Composable
    fun ShowDialog(itemIndex: Int,itemFile:FileData, isShow: MutableState<Boolean>,fileViewModel: FileViewModel){
        if(isShow.value){
            AlertDialog(
                onDismissRequest = { isShow.value = false },
                title = { Text(text = getString(R.string.file_choice), fontSize = 20.sp)},
                shape = RoundedCornerShape(20.0f),
                buttons = {  },
                text = {
                    Column(modifier = Modifier
                        .width(200.dp)
                        .height(60.dp)) {
                        Text( text = getString(R.string.file_download), fontSize = 18.sp, modifier = Modifier
                            .padding(top = 35.dp, start = 15.dp)
                            .fillMaxWidth()
                            .clickable {
                                //权限请求
                                fileViewModel.downLoadFile(itemIndex,itemFile.fileName)
                                MyApplication.MyToast("下载中，请稍后")
                                isShow.value = false
                            })
                        /*
                        Text(text = getString(R.string.file_delete),fontSize = 18.sp, modifier = Modifier
                            .padding(top = 15.dp, start = 15.dp)
                            .fillMaxWidth()
                            .clickable {
                                fileViewModel.deleteFile(itemIndex,itemFile.fileName)
                                isShow.value = false
                            })

                         */
                    }
                }
            )
        }

    }



    @Composable
    fun Title(){
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(title_color),verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Image(modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
                    .size(30.dp)
                    .clickable { finish() },painter = painterResource(id = R.drawable.back), contentDescription = "back")
                Text(modifier = Modifier
                    .weight(7f)
                    .wrapContentWidth(CenterHorizontally), text = getString(R.string.file_mang), fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }


}