/*
 * ************************************************************
 * 文件：UserParam.java  模块：app  项目：trunk
 * 当前修改时间：2019年07月29日 15:36:02
 * 上次修改时间：2019年07月29日 15:36:01
 * 作者：大路
 * Copyright (c) 2019
 * ************************************************************
 */

package RobotWebService;

import android.os.Environment;

import androidx.versionedparcelable.ParcelUtils;

import com.alibaba.fastjson.JSON;

import net.dalu2048.wechatgenius.DBData;

import java.io.File;
import java.util.UUID;

public class UserParam {
    public String UserName;
    public String Password;
    public String ASPXAUTH;
    public boolean LogInSuccess = false;
    public Object LoginCookie = new Object();

    public UUID UserKey = UUID.randomUUID();
    public UUID JobID = UUID.randomUUID();


    public String DataSourceName = "";

    public static String MemberSourceode = "";

    public String WebServerURL="";

    public aspnet_UsersNewGameResultSend Membersetting = null;

    public static UserParam RefreshUserparamBuf() {
        UserParam bufs = GetUserparamBuf();

        Robotsrv.UserLogin(bufs.UserName, bufs.Password);

        String Path = Environment.getExternalStorageDirectory() + "/app.dat";
        DBData.writeFileData(Path, Robotsrv.Jusrpar());
        return GetUserparamBuf();
    }

    public static UserParam GetUserparamBuf() {
        UserParam param = null;
        String Path = Environment.getExternalStorageDirectory() + "/app.dat";
        String JSon = DBData.readFileData(Path);
        param = JSON.parseObject(JSon, UserParam.class);
        return param;
    }
    public  static  boolean CleanUserParamBuf()
    {
        String Path = Environment.getExternalStorageDirectory() + "/app.dat";

        return  deleteFile(Path);
    }

    public static boolean deleteFile(String pathname) {
        boolean result = false;
        File file = new File(pathname);
        if (file.exists()) {
            file.delete();
            result = true;
            System.out.println("文件已经被成功删除");
        }
        return result;
    }
}

