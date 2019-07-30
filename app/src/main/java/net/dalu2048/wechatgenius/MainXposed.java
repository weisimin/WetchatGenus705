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

import android.content.ContentValues;

import android.content.RestrictionEntry;
import android.database.Cursor;
import android.util.Base64;


import net.dalu2048.wechatgenius.xposed.WechatUtils;

import java.lang.reflect.Array;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import android.content.Intent;

import java.util.LinkedList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
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
                             printInsertLog(tableName, (String) param.args[1], contentValues, (Integer) param.args[3]);
                            return;
                        }
                        //打印出日志
                        printInsertLog(tableName, (String) param.args[1], contentValues, (Integer) param.args[3]);

                        //提取消息内容
                        //1：表示是自己发送的消息
                        int isSend = contentValues.getAsInteger("isSend");
                        //消息内容
                        String strContent = contentValues.getAsString("content");
                        //XposedBridge.log("收到消息："+strContent);
                        // PrintTrack();
                        //说话人ID
                        String strSayTalker = contentValues.getAsString("talker");

                        String strcreateTime = contentValues.getAsString("createTime");

                        String msgType = contentValues.getAsString("type");

                        if (isSend == 1 && Robotsrv.My_Wechatid == "") {
                            Robotsrv.My_Wechatid = strSayTalker;
                        }
                        //XposedBridge.log("taleris:"+strTalker);
                        // XposedBridge.log("content:"+strContent);
                        //收到消息，进行回复（要判断不是自己发送的、不是群消息、不是公众号消息，才回复）
                        if (//isSend != 1 && !strSayTalker.endsWith("@chatroom") &&
                                !strSayTalker.startsWith("gh_")&&Robotsrv.Jusrpar!="") {

                            // XposedBridge.log((DBData.OpenAndQuery("EnMicroMsg","select * from rcontact where username='"+strTalker+"'")));
                            Cursor cur = DBData.OpenAndQueryCursor("EnMicroMsg", "select username,encryptUsername,nickname, conRemark  from rcontact where username='" + strSayTalker + "'");
                            cur.moveToFirst();
                            String encryptUserNmae = cur.getString(1);
                            String nickname = cur.getString(2);
                            String conRemark = cur.getString(3);
                            String talkerid = contentValues.getAsString("talkerid");
                            cur = DBData.OpenAndQueryCursor("EnMicroMsg", "select rowid,username,encryptUsername,nickname, conRemark  from rcontact where rowid='" + talkerid + "'");
                            cur.moveToFirst();
                            String ConversationTalkerusername = cur.getString(1);

                            String ToUserNameTEMPID = isSend == 1 ? ConversationTalkerusername : strSayTalker;
                            String FromUserNameTEMPID = isSend == 1 ? strSayTalker : ConversationTalkerusername;

                            String Res = Robotsrv.MessageRobotDo(strContent, "安微", (conRemark == null ? nickname : conRemark), FromUserNameTEMPID, ToUserNameTEMPID, strcreateTime, msgType, false, Robotsrv.My_Wechatid, Robotsrv.Jusrpar);
                            if (Res != "") {
                                SendWXContentByID(loadPackageParam, strSayTalker, encryptUserNmae, Res);
                            }
                        }

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


    static void SendWXContent(final XC_LoadPackage.LoadPackageParam loadPackageParam, String NickNameOrConRemark, String Content) {

        Cursor mCursor = DBData.OpenAndQueryCursor("EnMicroMsg"
                , "select username,encryptUsername,nickname,conRemark from rcontact where nickname = '" + NickNameOrConRemark.replace("'", "''") + "' or conReamrk = '" + NickNameOrConRemark.replace("'", "''") + "'"
        );

        if (mCursor != null) {
            mCursor.moveToFirst();
            do {
                String Username = mCursor.getString(0);
                String EncryptUserName = mCursor.getString(1);
                SendWXContentByID(loadPackageParam, Username, EncryptUserName, Content);

            } while (mCursor.moveToNext());
        }// cursor if
// touser,content,1,0
    }

    static void SendWXContentByID(final XC_LoadPackage.LoadPackageParam loadPackageParam, String UserName, String EncryptUserName, String Content) {
        Class<?> typ_h = XposedHelpers.findClassIfExists("com.tencent.mm.modelmulti.h", loadPackageParam.classLoader);

        //touser,content,1,0
        Object newh = XposedHelpers.newInstance(typ_h, new Class[]{String.class, String.class, int.class, int.class, Object.class}, UserName, Content, 1, 0, null);

        Class<?> typ_y = XposedHelpers.findClassIfExists("com.tencent.mm.ui.chatting.c.y", loadPackageParam.classLoader);
        Object yinstacne = XposedHelpers.newInstance(typ_y, new Class[]{}, new Object[]{});
        Field cyl = XposedHelpers.findField(typ_y, "cyL");
        try {
            cyl.set(yinstacne, EncryptUserName);
        } catch (Exception e) {
            XposedBridge.log("写入encryid失败");
        }
        Method mtd_g = XposedHelpers.findMethodExactIfExists(typ_y, "g", typ_h);
        try {
            mtd_g.invoke(yinstacne, newh);
        } catch (Exception e) {
            XposedBridge.log("执行y方法失败");
        }
        Class<?> typ_aw = XposedHelpers.findClassIfExists("com.tencent.mm.model.aw", loadPackageParam.classLoader);
        Method mtd_RC = XposedHelpers.findMethodExactIfExists(typ_aw, "Rc");
        Class<?> typ_p = XposedHelpers.findClassIfExists("com.tencent.mm.ai.p", loadPackageParam.classLoader);
        Class<?> typ_m = XposedHelpers.findClassIfExists("com.tencent.mm.ai.m", loadPackageParam.classLoader);

        Method mtd_a = XposedHelpers.findMethodExactIfExists(typ_p, "a", typ_m, int.class);

        if (mtd_RC == null) {
            XposedBridge.log("找不到mtd_RC方法");
            return;
        }
        if (mtd_a == null) {
            XposedBridge.log("找不到mtd_a方法");
            return;
        }

        Object InstanceP = null;

        try {
            InstanceP = mtd_RC.invoke(null, new Object[]{});
            if (InstanceP == null) {
                XposedBridge.log("实例aw.Rc()返回空白");
                return;
            }
        } catch (Exception e) {
            XposedBridge.log("执行mtd_RC方法失败" + e.getMessage());
        }
        try {
            mtd_a.invoke(InstanceP, new Object[]{newh, 0});
        } catch (Exception e) {
            XposedBridge.log("执行mtd_a方法失败" + e.getMessage());
        }
        //aw.Rc().a((m)h, 0);
    }

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
