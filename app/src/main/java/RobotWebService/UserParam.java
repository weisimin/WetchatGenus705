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

    public static String MemberSourceode="";

    public aspnet_UsersNewGameResultSend Membersetting =null;
}
