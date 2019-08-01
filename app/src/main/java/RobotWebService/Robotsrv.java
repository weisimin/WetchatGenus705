/*
 * ************************************************************
 * 文件：Robotsrv.java  模块：app  项目：trunk
 * 当前修改时间：2019年07月26日 14:19:10
 * 上次修改时间：2019年07月26日 14:19:09
 * 作者：大路
 * Copyright (c) 2019
 * ************************************************************
 */

package RobotWebService;

import org.ksoap2.SoapEnvelope;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;

import org.ksoap2.serialization.SoapSerializationEnvelope;

import org.ksoap2.transport.HttpTransportSE;

import java.net.PortUnreachableException;
import java.util.UUID;

import de.robv.android.xposed.XposedBridge;

public class Robotsrv {
    public static String Jusrpar = "";

    public static String WebServiceUrl = "http://192.168.5.230/WEBSERVICE.ASMX";

    public static String UserLogin(String UserName, String Password) {
        SoapObject request = new SoapObject("http://13828081978.zicp.vip/", "UserLogInUsrpar");
        request.addProperty("UserName", UserName);
        request.addProperty("Password", Password);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        (new MarshalBase64()).register(envelope);
        HttpTransportSE ht = new HttpTransportSE(WebServiceUrl, 60000);

        try {
            ht.call("http://13828081978.zicp.vip/UserLogInUsrpar", envelope);
            if (envelope.bodyIn.getClass() == SoapFault.class) {
                SoapFault res = (SoapFault) envelope.bodyIn;
                return "错误:" + res.getMessage();
            }
            SoapObject res = (SoapObject) envelope.bodyIn;
            Jusrpar = res.getProperty("UserLogInUsrparResult").toString();
            return Jusrpar;
        } catch (Exception anyerror) {
            return "错误:" + anyerror.toString();
        }

    }//fun end

    public static String My_Wechatid = "My_Wechatid";
    public static String My_Wechatencryptname = "My_Wechatencryptname";
    public static String My_playername = "My_playername";

    public static String MessageRobotDo(String RawContent, String WX_SourceType, String UserNameOrRemark, String FromUserNameTEMPID, String ToUserNameTEMPID, String JavaMsgTime, String msgType, Boolean IsTalkGroup, String MyUserTEMPID, String Jusrpar) {
        SoapObject request = new SoapObject("http://13828081978.zicp.vip/", "MessageRobootDo");

        request.addProperty("RawContent", RawContent);
        request.addProperty("WX_SourceType", WX_SourceType);
        request.addProperty("UserNameOrRemark", UserNameOrRemark);
        request.addProperty("FromUserNameTEMPID", FromUserNameTEMPID);
        request.addProperty("ToUserNameTEMPID", ToUserNameTEMPID);
        request.addProperty("JavaMsgTime", JavaMsgTime);
        request.addProperty("msgType", msgType);
        request.addProperty("IsTalkGroup", IsTalkGroup);
        request.addProperty("MyUserTEMPID", MyUserTEMPID);
        request.addProperty("Jusrpar", Jusrpar);


        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        (new MarshalBase64()).register(envelope);
        HttpTransportSE ht = new HttpTransportSE(WebServiceUrl, 60000);

        try {
            ht.call("http://13828081978.zicp.vip/MessageRobootDo", envelope);
            if (envelope.bodyIn.getClass() == SoapFault.class) {
                SoapFault res = (SoapFault) envelope.bodyIn;
                return "MessageRobotDo_Call获取错误:" + res.getMessage();
            }
            SoapObject res = (SoapObject) envelope.bodyIn;

            String resstr = res.getProperty("MessageRobootDoResult").toString();
            if (resstr.equals("anyType{}")) {
                return "";
            } else {
                return resstr;
            }

        } catch (Exception anyerror) {
            return "MessageRobotDo_Call之前错误:" + anyerror.toString();
        }

    }

    public static String GetLastGamePeriod(String JShiShiCaiMode) {
        SoapObject request = new SoapObject("http://13828081978.zicp.vip/", "GetLastGamePeriod");

        request.addProperty("JShiShiCaiMode", JShiShiCaiMode);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        (new MarshalBase64()).register(envelope);
        HttpTransportSE ht = new HttpTransportSE(WebServiceUrl, 60000);

        try {
            ht.call("http://13828081978.zicp.vip/GetLastGamePeriod", envelope);
            SoapObject res = (SoapObject) envelope.bodyIn;

            return res.getProperty("GetLastGamePeriodResult").toString();
        } catch (Exception anyerror) {
            return "错误:" + anyerror.toString();
        }

    }

    public static String GetLastGamePIC(String JShiShiCaiMode, String JPICType) {

        SoapObject request = new SoapObject("http://13828081978.zicp.vip/", "GetLastGamePIC");


        request.addProperty("JShiShiCaiMode", JShiShiCaiMode);
        request.addProperty("JPICType", JPICType);
        request.addProperty("Jusrpar", Jusrpar);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        (new MarshalBase64()).register(envelope);
        HttpTransportSE ht = new HttpTransportSE(WebServiceUrl, 60000);

        try {
            ht.call("http://13828081978.zicp.vip/GetLastGamePIC", envelope);
            SoapObject res = (SoapObject) envelope.bodyIn;

            return res.getProperty("GetLastGamePICResult").toString();
        } catch (Exception anyerror) {
            return "错误:" + anyerror.toString();
        }
    }

    public static String GetGameOpenResult() {
        return "功能未完成";

    }

    public static String UploadContacts(String Jcontacts, String Jusrpar, String WX_SourceType) {

        SoapObject request = new SoapObject("http://13828081978.zicp.vip/", "UploadContacts");

        request.addProperty("Jcontacts", Jcontacts);
        request.addProperty("Jusrpar", Jusrpar);
        request.addProperty("WX_SourceType", WX_SourceType);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        (new MarshalBase64()).register(envelope);
        HttpTransportSE ht = new HttpTransportSE(WebServiceUrl, 60000);

        try {
            ht.call("http://13828081978.zicp.vip/UploadContacts", envelope);
            if (envelope.bodyIn.getClass() == SoapFault.class) {
                SoapFault res = (SoapFault) envelope.bodyIn;
                return "UploadContacts_call获取错误:" + res.toString();
            }
            SoapObject res = (SoapObject) envelope.bodyIn;

            return res.getProperty("UploadContactsResult").toString();
        } catch (Exception anyerror) {
            return "UploadContacts_Call之前错误:" + anyerror.toString();
        }
    }//fun end

    public static String GetSendJobs(String WX_Sourcetype, String Jusrpar) {

        SoapObject request = new SoapObject("http://13828081978.zicp.vip/", "GetSendJobs");

        request.addProperty("WX_Sourcetype", "安微");
        request.addProperty("Jusrpar", Jusrpar);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        (new MarshalBase64()).register(envelope);
        HttpTransportSE ht = new HttpTransportSE(WebServiceUrl, 60000);

        try {
            ht.call("http://13828081978.zicp.vip/GetSendJobs", envelope);
            if (envelope.bodyIn.getClass() == SoapFault.class) {
                SoapFault res = (SoapFault) envelope.bodyIn;
                return "GetSendJobs call获取错误:" + res.toString();
            }
            SoapObject res = (SoapObject) envelope.bodyIn;

            return res.getProperty("GetSendJobsResult").toString();
        } catch (Exception anyerror) {
            return "GetSendJobs Call之前错误:" + anyerror.toString();
        }
    }//fun end

    public static String UpdateSendJobs(String WX_Sourcetype, UUID Userid, int Jobid) {

        SoapObject request = new SoapObject("http://13828081978.zicp.vip/", "UpdateSendJobs");

        request.addProperty("WX_Sourcetype", WX_Sourcetype);
        request.addProperty("Userid", Userid);
        request.addProperty("Jobid", Jobid);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        (new MarshalBase64()).register(envelope);
        HttpTransportSE ht = new HttpTransportSE(WebServiceUrl, 60000);

        try {
            ht.call("http://13828081978.zicp.vip/UpdateSendJobs", envelope);
            if (envelope.bodyIn.getClass() == SoapFault.class) {
                SoapFault res = (SoapFault) envelope.bodyIn;
                return "UpdateSendJobs call获取错误:" + res.toString();
            }
            SoapObject res = (SoapObject) envelope.bodyIn;

            return res.getProperty("UpdateSendJobsResult").toString();
        } catch (Exception anyerror) {
            return "UpdateSendJobs Call之前错误:" + anyerror.toString();
        }
    }//fun end

    public static   Thread Thread_GetJendJob=null;

}//class end
