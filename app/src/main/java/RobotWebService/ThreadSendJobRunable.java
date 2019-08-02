/*
 * ************************************************************
 * 文件：ThreadSendJobRunable.java  模块：app  项目：trunk
 * 当前修改时间：2019年07月31日 17:45:07
 * 上次修改时间：2019年07月31日 17:45:07
 * 作者：大路
 * Copyright (c) 2019
 * ************************************************************
 */

package RobotWebService;

import android.database.Cursor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import net.dalu2048.wechatgenius.DBData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static RobotWebService.ContentProcessRunable.SendWXContentByID;


public class ThreadSendJobRunable implements Runnable {

    public XC_LoadPackage.LoadPackageParam loadPackageParam=null;
    public ThreadSendJobRunable( XC_LoadPackage.LoadPackageParam _loadPackageParam) {
        loadPackageParam=_loadPackageParam;

    }

    @Override
    public void run() {
        while (true) {
            try {

               List<aspnet_UserSendJob> jobs=    JSON.parseArray(Robotsrv.GetSendJobs("安微",Robotsrv.Jusrpar),aspnet_UserSendJob.class);
              for (int i=0;i<jobs.size();i++)
              {
                  String nicknameorRemark=jobs.get(i).WX_UserName;

                  Cursor cur = DBData.OpenAndQueryCursor("EnMicroMsg", "select username,encryptUsername,nickname, conRemark  from rcontact where nickname='"
                          + nicknameorRemark
                          + "' or conRemark='"
                          +nicknameorRemark+"'");
                  cur.moveToFirst();
                  String SayPlayerName=cur.getString(0);
                  String SayencryptUserNmae=cur.getString(1);
                  Robotsrv.UpdateSendJobs("安微",UserParam.GetUserparamBuf().UserKey,jobs.get(i).Joibid);
                  SendWXContentByID(loadPackageParam, SayPlayerName, SayencryptUserNmae, "*"+jobs.get(i).ToSendMessage);


              }// for end
                Thread.sleep(3000);
            } catch (Exception e) {
                XposedBridge.log("循环获取发送任务失败" + e.toString());
                try {
                    Thread.sleep(3000);
                } catch (Exception e2) {
                }//sub catch
            }//catch
    }//while end
}//run end
}//class end


