package com.cidi.obuclient.data

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cidi.obuclient.MyApplication
import com.cidi.obuclient.utils.FTPUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPFile
import java.io.File

/**
 *Created by CIDI zhengxuan on 2023/12/22
 *QQ:1309873105
 */
class FileViewModel: ViewModel() {
    private val ftpName = "ftp"
    private val ftpPsd = "ftp"
    private val IP = "192.168.2.10"
    private val port = 2121

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    var FilesList = MutableLiveData<ArrayList<FileData>>()
    private var FTPFileList = ArrayList<FileData>()

    init {
        coroutineScope.launch {
            Log.e("FTP","---->加载init")
            getFTPFileLists()
        }
    }

    fun createFileDir(){
        coroutineScope.launch {
            val path = Environment.getExternalStorageDirectory().absolutePath + "/com.cidi.data"
            val dir = File(path)
            if(!dir.exists()){
                dir.mkdir()
            }
            MyApplication.setSavePath(dir.absolutePath)
            Log.e("path","--->${dir.absolutePath}")
        }
    }

     fun getFTPFileLists(){
         val ftpFiles: Array<FTPFile> = FTPUtils.getInstance().getFilesInfo(ftpName,ftpPsd,IP,port,"/")
         if(ftpFiles.isNotEmpty()){
             FTPFileList.clear()
             for (ftpItem in ftpFiles){
                 Log.e("FTP","--->${ftpItem.name}---${ftpItem.link}")
                 FTPFileList.add(FileData(ftpItem.name,"/"))
             }
             FilesList.postValue(FTPFileList)
         }
    }

    fun refreshData(){
        coroutineScope.launch {
            getFTPFileLists()
        }
    }

    //下载选中的文件
    fun downLoadFile(fileName: String){
        //在这里申请权限
        coroutineScope.launch {
            val path = MyApplication.getPath()
            val isSuccess = FTPUtils.getInstance().downLoadFiles(ftpName,ftpPsd,IP,port,"$fileName",path)
            withContext(Dispatchers.Main){
                if(isSuccess){
                    MyApplication.MyToast("下载成功，保存在${path}")
                }else{
                    MyApplication.MyToast("下载失败，请检查网络！")
                }
            }
        }
    }

    //删除文件
    fun deleteFile(fileName: String){
        coroutineScope.launch {
            val isSuccess = deletaFTPfile(fileName,"/$fileName")
            withContext(Dispatchers.Main){
                if(isSuccess){
                    MyApplication.MyToast("删除成功")
                }else{
                    MyApplication.MyToast("删除失败，请检查网络！")
                }
            }
        }

    }

    private suspend fun deletaFTPfile(fileName: String,filePath: String): Boolean{
        return  FTPUtils.getInstance().deleteFiles(ftpName,ftpName,IP,port,fileName,filePath)
    }



}