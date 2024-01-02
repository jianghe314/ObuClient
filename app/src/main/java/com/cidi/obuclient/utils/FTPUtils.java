package com.cidi.obuclient.utils;


import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by CIDI zhengxuan on 2020/9/10
 * QQ:1309873105
 */
public class FTPUtils {
     private static FTPClient ftpClient = new FTPClient();
     private static FTPUtils ftpUtils;

     public static FTPUtils getInstance(){
         if(ftpUtils == null){
             synchronized (FTPUtils.class){
                 if(ftpUtils == null){
                     ftpUtils = new FTPUtils();
                 }
             }
         }
         return ftpUtils;
     }

    /**
     *
     * @param name 用户名
     * @param psd 用户密码
     * @param serverAddress 服务器地址
     * @param port  服务器端口
     * @param filesName 文件名称
     * @param filesPath 文件路径
     * @param savePath  保存文件路径
     * @return
     */
     public boolean upLoadFiles(String name, String psd, String serverAddress, int port, String filesName, String filesPath, String savePath){
         InputStream inputStream = null;
         try {
             boolean isLogin = initLogin(name,psd,serverAddress,port);
             if(isLogin){
                 //登录成功
                 ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                 ftpClient.makeDirectory(savePath);
                 inputStream = new FileInputStream(new File(filesPath));
                 boolean isUploadSuccess = ftpClient.storeFile(filesName,inputStream);
                 if(isUploadSuccess){
                     return true;
                 }else {
                     return false;
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }finally {
             if(ftpClient.isConnected()){
                 try {
                     ftpClient.logout();
                     ftpClient.disconnect();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             if(null != inputStream){
                 try {
                     inputStream.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         return false;
     }

    /**
     *
     * @param name
     * @param psd
     * @param serverAddress
     * @param port
     * @param downFilesDirectory 查询的FTP服务器给定路径
     * @return
     */
     public FTPFile[] getFilesInfo(String name, String psd, String serverAddress, int port, String downFilesDirectory){
         FTPFile[] ftpFiles = null;
         try {
             boolean isLogin = initLogin(name,psd,serverAddress,port);
             Log.e("FTP","getFilesInfo---->"+isLogin);
             if(isLogin){
                 ftpClient.changeWorkingDirectory(downFilesDirectory);
                 ftpFiles = ftpClient.listFiles();
                 ftpClient.logout();
             }
             return ftpFiles;
         } catch (IOException e) {
             e.printStackTrace();
         }
         return ftpFiles;
     }


    /**
     *
     * @param name 用户名
     * @param psd   用户密码
     * @param serverAddress 服务器地址
     * @param port  端口
     * @param downFilesName 下载文件名称
     * @param saveLocalDirectory  文件保存路径
     * @return
     */
     public boolean downLoadFiles(String name, String psd, String serverAddress, int port, String downFilesName, String saveLocalDirectory){
         OutputStream outputStream = null;
         try {
             boolean isLogin =initLogin(name,psd,serverAddress,port);
             Log.e("FTP","downLoadFiles---->"+isLogin);
             if(isLogin){
                 ftpClient.changeWorkingDirectory("/");
                 FTPFile[] ftpFiles = ftpClient.listFiles();
                 for (FTPFile file : ftpFiles) {
                     if(file.getName().equals(downFilesName)){
                         File localFile = new File(saveLocalDirectory+"/"+file.getName());
                         if(localFile.exists()){
                             localFile.delete();
                         }
                         outputStream = new FileOutputStream(localFile);
                         ftpClient.retrieveFile(file.getName(),outputStream);
                         outputStream.close();
                         return true;
                     }
                 }
                 ftpClient.logout();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }finally {
             if(ftpClient.isConnected()){
                 try {
                     ftpClient.disconnect();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             if(null != outputStream){
                 try {
                     outputStream.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         return false;
     }


    /**
     *
     * @param name
     * @param psd
     * @param serverAddress
     * @param port
     * @param deleteFileName 删除文件名称
     * @param deleteFilePath 删除文件路径
     * @return
     */
     public boolean deleteFiles(String name, String psd, String serverAddress, int port, String deleteFileName, String deleteFilePath){
         try {
             boolean isLogin = initLogin(name,psd,serverAddress,port);
             Log.e("FTP","deleteFiles---->"+isLogin);
             if(isLogin){
                 FTPFile[] ftpFiles = ftpClient.listFiles();
                 for (int i = 0; i < ftpFiles.length; i++) {
                     FTPFile file = ftpFiles[i];
                     if(file.getName().equals(deleteFileName)){
                         //new String(deleteFileName.getBytes("ISO-8859-1"), "utf-8")
                         ftpClient.changeWorkingDirectory("/");
                         boolean isDelete = ftpClient.deleteFile(deleteFileName);
                         Log.e("isDelete","--->"+isDelete);
                         return isDelete;
                     }
                 }
                 ftpClient.logout();
             }
             return false;
         } catch (IOException e) {
             e.printStackTrace();
         }
         return false;
     }


     private boolean initLogin(String name, String psd, String serverAddress, int port){
         boolean isLogin = false;
         try {
             ftpClient.connect(serverAddress,port);
             int replyCode = ftpClient.getReplyCode();
             Log.e("FTP","initLogin code:---->code"+replyCode);
             if(FTPReply.isPositiveCompletion(replyCode)){
                 isLogin =  ftpClient.login(name,psd);
                 //ftpClient.enterLocalActiveMode();
                 Log.e("FTP","initLogin---->"+isLogin);
             }
             return isLogin;
         } catch (IOException e) {
             e.printStackTrace();
         }
         return false;
     }

}
