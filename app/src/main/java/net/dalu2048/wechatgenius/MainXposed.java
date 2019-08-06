/*
 * ************************************************************
 * 文件：MainXposed.java  模块：app  项目：WeChatGenius
 * 当前修改时间：2018年08月19日 17:06:09
 * 上次修改时间：2018年08月19日 17:06:09
 * 作者：大路
 * Copyright (c) 2018
 * ************************************************************
 */

package net.dalu2048.wechatgenius;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;

import android.content.Context;
import android.content.RestrictionEntry;
import android.database.Cursor;
import android.os.Environment;
import android.os.Looper;
import android.util.Base64;


import net.dalu2048.wechatgenius.ui.AboutActivity;
import net.dalu2048.wechatgenius.ui.user.LoginActivity;
import net.dalu2048.wechatgenius.util.RegexUtils;
import net.dalu2048.wechatgenius.xposed.WechatUtils;

import java.lang.reflect.Array;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import RobotWebService.ContentProcessRunable;
import RobotWebService.ThreadSendJobRunable;
import RobotWebService.UserParam;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import android.content.Intent;

import java.util.LinkedList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

import android.app.Activity;
import android.widget.EditText;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIKeyboardHelper;

import static de.robv.android.xposed.XposedBridge.getXposedVersion;
import static de.robv.android.xposed.XposedBridge.invokeOriginalMethod;

import RobotWebService.Robotsrv;

public final class MainXposed implements IXposedHookLoadPackage {
    //微信数据库包名称
    private static final String WECHAT_DATABASE_PACKAGE_NAME = "com.tencent.wcdb.database.SQLiteDatabase";
    //聊天精灵客户端包名称
    private static final String WECHATGENIUS_PACKAGE_NAME = "net.dalu2048.wechatgenius";
    //微信主进程名
    private static final String WECHAT_PROCESS_NAME = "com.tencent.mm";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // XposedBridge.log("hook加载:"+lpparam.packageName);
        //region hook模块是否激活
        if (lpparam.packageName.equals(WECHATGENIUS_PACKAGE_NAME)) {

            //hook客户端APP的是否激活返回值。替换为true。
            Class<?> classAppUtils = XposedHelpers.findClassIfExists(WECHATGENIUS_PACKAGE_NAME + ".util.AppUtils", lpparam.classLoader);
            if (classAppUtils != null) {
                XposedHelpers.findAndHookMethod(classAppUtils,
                        "isModuleActive",
                        XC_MethodReplacement.returnConstant(true));
                // XposedBridge.log("成功hook住net.xxfeng.cc.util.AppUtils的isModuleActive方法。");
            }
            return;
        }
        //endregion

        if (!lpparam.processName.equals(WECHAT_PROCESS_NAME)) {
            return;
        }
        UserParam.RefreshUserparamBuf();
        // XposedBridge.log("进入微信进程：" + lpparam.processName);
        //调用 hook数据库插入。
        hookDatabaseInsert(lpparam);
        //abstract可能勾不住
        //openhelperhook(lpparam);

    }


    //hook数据库插入操作
    private void hookDatabaseInsert(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        final Class<?> classDb = XposedHelpers.findClassIfExists(WECHAT_DATABASE_PACKAGE_NAME, loadPackageParam.classLoader);

        // Class<?> classDb =null;
        try {
            // classDb=Class.forName(WECHAT_DATABASE_PACKAGE_NAME,false, loadPackageParam.classLoader);
            if (classDb == null) {

                // XposedBridge.log("hook数据库insert操作：未找到类" + WECHAT_DATABASE_PACKAGE_NAME);
                return;
            }

        } catch (Exception error) {
            //XposedBridge.log("ForName hook数据库insert操作：未找到类" + WECHAT_DATABASE_PACKAGE_NAME);
            return;

        }

        XposedHelpers.findAndHookMethod(classDb,
                "insertWithOnConflict",
                String.class, String.class, ContentValues.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String tableName = (String) param.args[0];
                        //XposedBridge.log("插入表："+tableName);
                        ContentValues contentValues = (ContentValues) param.args[2];
                        if (tableName == null || tableName.length() == 0 || contentValues == null) {
                            return;
                        }
                        //过滤掉非聊天消息
                        if (!tableName.equals("message")) {
                            //printInsertLog(tableName, (String) param.args[1], contentValues, (Integer) param.args[3]);
                            return;
                        }
                        //打印出日志
                        //printInsertLog(tableName, (String) param.args[1], contentValues, (Integer) param.args[3]);

                        //提取消息内容
                        //1：表示是自己发送的消息
                        int isSend = contentValues.getAsInteger("isSend");
                        //消息内容
                        String strContent = contentValues.getAsString("content");
                        //XposedBridge.log("收到消息："+strContent);
                        // PrintTrack();
                        //说话人ID
                        String strSayTalker = contentValues.getAsString("talker");

                        if ( Robotsrv.My_Wechatid.equals("My_Wechatid")) {
                            Cursor cur = DBData.OpenAndQueryCursor("EnMicroMsg", "select username,encryptUsername,nickname, conRemark  from rcontact where type='1'");
                            cur.moveToFirst();

                            String SayTalker= cur.getString(0);
                            String SayencryptUserNmae = cur.getString(1);
                            String Saynickname = cur.getString(2);
                            String SayconRemark = cur.getString(3);
                            String SayPlayerName=(SayconRemark == null || SayconRemark .equals( "") ? Saynickname : SayconRemark);

                            Robotsrv.My_Wechatid = SayTalker;
                            Robotsrv.My_Wechatencryptname = SayencryptUserNmae;
                            Robotsrv.My_playername = (SayconRemark == null || SayconRemark .equals( "") ? Saynickname : SayconRemark);
                            try
                            {
                                UserParam.GetUserparamBuf();
                            }
                            catch  (Exception e)
                            {


                            }

                        }
                        if (Robotsrv.Thread_GetJendJob==null)
                        {
                            Robotsrv.Thread_GetJendJob = new Thread(new ThreadSendJobRunable(loadPackageParam));
                            Robotsrv.Thread_GetJendJob.start();
                        }

                        if ((strContent.startsWith("*") || strContent.startsWith("错误")) && Robotsrv.My_Wechatid != "My_Wechatid") {
                            return;
                        }

                        new Thread(new ContentProcessRunable(loadPackageParam, contentValues)).start();

                    }
                });//find hook end
// public static SQLiteDatabase openDatabase(String paramString, byte[] paramArrayOfByte, SQLiteCipherSpec paramSQLiteCipherSpec, CursorFactory paramCursorFactory, int paramInt, DatabaseErrorHandler paramDatabaseErrorHandler)
//  public static SQLiteDatabase openDatabase(String paramString, byte[] paramArrayOfByte, SQLiteCipherSpec paramSQLiteCipherSpec, CursorFactory paramCursorFactory, int paramInt1, DatabaseErrorHandler paramDatabaseErrorHandler, int paramInt2)

        final Class<?> SQLiteCipherSpec = XposedHelpers.findClassIfExists("com.tencent.wcdb.database.SQLiteCipherSpec", loadPackageParam.classLoader);

        XposedHelpers.findAndHookMethod(classDb,
                "open",
                byte[].class, SQLiteCipherSpec, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (((byte[]) param.args[0]) != null) {
                            byte[] pwdsb = ((byte[]) param.args[0]);
                            //SXposedBridge.log("pwd:"+new String(pwdsb));


                            // XposedBridge.log("pwd:" + Base64.encodeToString(((byte[]) param.args[0]), Base64.NO_WRAP));
                            //XposedBridge.log("object" + param.thisObject);
                            Class<?> class_SQLiteDatabase = XposedHelpers.findClassIfExists("com.tencent.wcdb.database.SQLiteDatabase", loadPackageParam.classLoader);
                            Method methodA = XposedHelpers.findMethodExactIfExists(class_SQLiteDatabase, "getPath"
                                    , new Object[]{});
                            if (methodA == null) {
                                XposedBridge.log("找不到方法getPath");

                            }
                            String newDbPath = "";
                            //调用发消息方法
                            try {
                                Object res = XposedBridge.invokeOriginalMethod(methodA, param.thisObject, null);
                                newDbPath = res.toString();
                                if (newDbPath.contains("EnMicroMsg")) {
                                    // String jsontables= DBData.getRecordsStr(classDb,"select rcontact.username,rcontact.encryptUsername,rcontact.nickname,rcontact.conRemark  from rcontact ", param.thisObject);
                                    //  XposedBridge.log("**********"+jsontables);


                                }
                                // XposedBridge.log(res.toString());
                                //XposedBridge.log("invokeOriginalMethod()执行成功");
                            } catch (Exception e) {
                                XposedBridge.log("调用微信消息回复方法异常");
                                XposedBridge.log(e);
                            }

                            //Dbs.add(new DBData(((byte[])param.args[0]),param.thisObject) );*/
                            DBData newdb = new DBData(
                                    ((byte[]) param.args[0])
                                    , param.thisObject
                                    , newDbPath
                                    , param.args[1]
                            );
                            newdb.Typ_SQLiteCipherSpec = SQLiteCipherSpec;
                            newdb.Typ_SQLiteDataBase = classDb;
                            DBData.JoinDBPath(newdb);

                        }//after hook end
                    }//param end

                });//find hook end


    }//fun end


    static String ObjectToString(Object param) {
        if (param == null) {
            return "null";

        } else {
            return param.toString();

        }

    }

    public ArrayList<DBData> Dbs = new ArrayList();

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = "0x" + Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    //输出插入操作日志
    private void printInsertLog(String tableName, String nullColumnHack, ContentValues contentValues, int conflictValue) {
        String[] arrayConflicValues =
                {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};
        if (conflictValue < 0 || conflictValue > 5) {
            return;
        }
        XposedBridge.log("Hook数据库insert。table：" + tableName
                + "；nullColumnHack：" + nullColumnHack
                + "；CONFLICT_VALUES：" + arrayConflicValues[conflictValue]
                + "；contentValues:" + contentValues);
    }

}
