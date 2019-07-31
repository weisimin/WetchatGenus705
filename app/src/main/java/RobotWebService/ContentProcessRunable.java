/*
 * ************************************************************
 * 文件：ContentProcessThread.java  模块：app  项目：trunk
 * 当前修改时间：2019年07月31日 08:01:56
 * 上次修改时间：2019年07月31日 08:01:56
 * 作者：大路
 * Copyright (c) 2019
 * ************************************************************
 */

package RobotWebService;

import android.content.ContentValues;
import android.database.Cursor;

import net.dalu2048.wechatgenius.DBData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ContentProcessRunable implements Runnable {

    public String ToUserNameTEMPID = "";
    public String FromUserNameTEMPID = "";

    public String ToPlayerName = "";
    public String FromPlayerName = "";

    public String ToencryptUserName = "";
    public String FromencryptUserName = "";



    public String SayencryptUserNmae = "";



    public ContentValues contentValues = null;

    public String strcreateTime = "";

    public String strContent = "";

    public int isSend = 0;

    public XC_LoadPackage.LoadPackageParam loadPackageParam = null;

    public String SayconRemark = "";
    public String Saynickname = "";
    public String SayTalker = "";
    public String SayPlayerName = "";

    public String msgType = "";

    public String ConversationNickName = "";
    public String ConversationConRemark = "";

    public ContentProcessRunable(XC_LoadPackage.LoadPackageParam _loadPackageParam, ContentValues _contentValues) {
        super();
        loadPackageParam = _loadPackageParam;
        contentValues = _contentValues;
        //提取消息内容
        //1：表示是自己发送的消息
        isSend = contentValues.getAsInteger("isSend");
        //消息内容
        strContent = contentValues.getAsString("content");
        //XposedBridge.log("收到消息："+strContent);
        // PrintTrack();
        //说话人ID
        SayTalker = contentValues.getAsString("talker");

        strcreateTime = contentValues.getAsString("createTime");

        msgType = contentValues.getAsString("type");

        //XposedBridge.log("taleris:"+strTalker);
        // XposedBridge.log("content:"+strContent);
        //收到消息，进行回复（要判断不是自己发送的、不是群消息、不是公众号消息，才回复）

        // XposedBridge.log((DBData.OpenAndQuery("EnMicroMsg","select * from rcontact where username='"+strTalker+"'")));
        Cursor cur = DBData.OpenAndQueryCursor("EnMicroMsg", "select username,encryptUsername,nickname, conRemark  from rcontact where username='" + SayTalker + "'");
        cur.moveToFirst();
        SayencryptUserNmae = cur.getString(1);
        Saynickname = cur.getString(2);
        SayconRemark = cur.getString(3);
        SayPlayerName=(SayconRemark == null || SayconRemark .equals( "") ? Saynickname : SayconRemark);



       /* XposedBridge.log("ConversationTalkerusername:" + ConversationTalkerusername
                + "Conversationenencryptusername:" + Conversationenencryptusername
                + "ConversationNickName:" + ConversationNickName
                + "ConversationConRemark:" + ConversationConRemark

        );*/


        ToUserNameTEMPID = isSend == 1 ? SayTalker : Robotsrv.My_Wechatid;
        FromUserNameTEMPID = isSend == 1 ? Robotsrv.My_Wechatid : SayTalker;

        ToencryptUserName = isSend == 1 ? SayencryptUserNmae : Robotsrv.My_Wechatencryptname;
        FromencryptUserName = isSend == 1 ? Robotsrv.My_Wechatencryptname : SayencryptUserNmae;

        ToPlayerName = isSend == 1 ? SayPlayerName : Robotsrv.My_Wechatid;
        FromPlayerName = isSend == 1 ? Robotsrv.My_Wechatid : SayPlayerName;


        XposedBridge.log("My_Wechatid:" + Robotsrv.My_Wechatid + "SayPlayerName:" + SayPlayerName);

    }

    @Override
    public void run() {
        if (Robotsrv.My_Wechatid.equals("My_Wechatid")) {
            SendWXContentByID(loadPackageParam, SayPlayerName, SayencryptUserNmae, "*机器人启动");
            return;
        }
        if (strContent.equals("加") && isSend == 1) {
            String jcontacts = DBData.OpenAndQuery("EnMicroMsg", "select username,nickname, conRemark ,type from rcontact ");
            if (Robotsrv.Jusrpar.equals("")) {

                UserParam.RefreshUserparamBuf();
            }
            String Res = "*" + Robotsrv.UploadContacts(jcontacts, Robotsrv.Jusrpar, "安微");
            //XposedBridge.log("回复"+ConversationTalkerusername+"加密"+Conversationenencryptusername);
            SendWXContentByID(loadPackageParam, SayPlayerName, SayencryptUserNmae, Res);
            return;
        }
        if (strContent.equals("刷新设置") && isSend == 1) {
            UserParam.RefreshUserparamBuf();
            return;
        }
        if (//isSend != 1
            //&& !strSayTalker.endsWith("@chatroom") &&
                !SayTalker.startsWith("gh_") && Robotsrv.Jusrpar != ""
        ) {
            String Res = "*" + Robotsrv.MessageRobotDo(strContent, "安微", (SayconRemark == null || SayconRemark .equals( "" )? Saynickname : SayconRemark)
                    , FromPlayerName
                    , ToPlayerName, strcreateTime, msgType
                    , false, Robotsrv.My_Wechatid, Robotsrv.Jusrpar
            );
            if (Res != "") {
                SendWXContentByID(loadPackageParam, SayTalker, SayencryptUserNmae, Res);
            }

        }




    }//run end

    public static void SendWXContentByID(final XC_LoadPackage.LoadPackageParam loadPackageParam, String UserName, String EncryptUserName, String Content) {


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
    }//fun end

    public static void SendWXContent(final XC_LoadPackage.LoadPackageParam loadPackageParam, String NickNameOrConRemark, String Content) {

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
    } //fun end

}
