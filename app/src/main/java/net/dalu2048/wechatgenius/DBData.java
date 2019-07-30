/*
 * ************************************************************
 * 文件：DBData.java  模块：app  项目：trunk
 * 当前修改时间：2019年07月18日 17:17:52
 * 上次修改时间：2019年07月18日 17:17:52
 * 作者：大路
 * Copyright (c) 2019
 * ************************************************************
 */

package net.dalu2048.wechatgenius;

import android.database.Cursor;

import android.util.Base64;
import android.view.ViewDebug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.PublicKey;
import java.util.ArrayList;

//import com.tencent.wcdb.database.SQLiteDatabase;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static android.content.Context.MODE_PRIVATE;

public class DBData {
    public  DBData(byte[] _Passwords ,Object _SqlLiteContainer,String _DbPath,Object _Dat_SQLiteCipherSpec)
    {
        DbPath=_DbPath;
        Passwords=_Passwords;
        SqlLiteContainer=    _SqlLiteContainer;
        Dat_SQLiteCipherSpec=_Dat_SQLiteCipherSpec;
    }


    public String DbPath="";


    public byte[] Passwords=null;
    public Object SqlLiteContainer=null;
    public Object Dat_SQLiteCipherSpec=null;
    public  Class<?> Typ_SQLiteCipherSpec=null;
    public  Class<?> Typ_SQLiteDataBase=null;
    public  Class<?> Typ_SQLiteCursor=null;
    public  Method mtd_RawQuery=null;

    public static ArrayList<DBData> Finddbs= new  ArrayList<DBData>();

    public  static void   JoinDBPath(DBData newdb ){
        //XposedBridge.log("队列长度" +String.valueOf(Finddbs.size()));
        if (newdb.Passwords!=null) {
           // XposedBridge.log("pwd:" + Base64.encodeToString(newdb.Passwords,Base64.NO_WRAP));
        }
       boolean NotFind=true;
        for (int i=0;i<Finddbs.size();i++ ) {
            if (Finddbs.get(i).DbPath==newdb.DbPath  )
            {
                NotFind=false;
            return;

            }

        }//for end
        Finddbs.add(newdb);
        }// join end

    public  static DBData Finddb(String DatabaseName)
    {

        for (int i=0;i<Finddbs.size();i++ ) {
            if (Finddbs.get(i).DbPath.toUpperCase().contains(DatabaseName.toUpperCase())  )
            {
                return  Finddbs.get(i);


            }

        }//for end
        return  null;
    }// finddb end
    public static String  OpenAndQuery(String DBName,String Sql)
    {
        DBData torun= Finddb(DBName);
        if (torun==null)
        {
            return "找不到数据库"+DBName;

        }
        //String Sql="select name from sqlite_master where type='table' order by name";
       return  cursorToString(OpenAndQueryCursor(DBName,Sql));
    }
    public static Cursor  OpenAndQueryCursor(String DBName,String Sql)
    {
        DBData torun= Finddb(DBName);
        //String Sql="select name from sqlite_master where type='table' order by name";
        String recordSet = null;

        Method rawQuery = XposedHelpers.findMethodExactIfExists(torun.Typ_SQLiteDataBase, "rawQuery" ,String.class,Object[].class);
        Cursor mCursor=null;
        try {
            mCursor = (Cursor) XposedBridge.invokeOriginalMethod(rawQuery, torun.SqlLiteContainer, new Object[]{Sql, null});
            XposedBridge.log(Sql);
            XposedBridge.log(torun.DbPath);
        }
        catch (Exception e)
        {
            XposedBridge.log("查询失败"+e.getMessage());
            e.printStackTrace();

        }
        //String[] colName = mCursor.getColumnNames();
        return  mCursor;
    }

    private static String cursorToString(Cursor crs) {
        JSONArray arr = new JSONArray();
        crs.moveToFirst();
        while (!crs.isAfterLast()) {
            int nColumns = crs.getColumnCount();
            JSONObject row = new JSONObject();
            for (int i = 0 ; i < nColumns ; i++) {
                String colName = crs.getColumnName(i);
                if (colName != null) {
                    String val = "";
                    try {
                        switch (crs.getType(i)) {
                            case Cursor.FIELD_TYPE_BLOB   : row.put(colName, crs.getBlob(i).toString()); break;
                            case Cursor.FIELD_TYPE_FLOAT  : row.put(colName, crs.getDouble(i))         ; break;
                            case Cursor.FIELD_TYPE_INTEGER: row.put(colName, crs.getLong(i))           ; break;
                            case Cursor.FIELD_TYPE_NULL   : row.put(colName, null)                     ; break;
                            case Cursor.FIELD_TYPE_STRING : row.put(colName, crs.getString(i))         ; break;
                        }
                    } catch (JSONException e) {
                        //PrintTrack();
                    }
                }
            }
            arr.put(row);
            if (!crs.moveToNext())
                break;
        }
        crs.close(); // close the cursor
        return arr.toString();
    }
    public  static  String getRecordsStr(Class<?> Typ_SQLiteDataBase,String selectQuery,Object database){
        String recordSet = null;
        Method rawQuery = XposedHelpers.findMethodExactIfExists(Typ_SQLiteDataBase, "rawQuery" ,String.class,Object[].class);
        Cursor mCursor=null;
        try {
            mCursor = (Cursor) XposedBridge.invokeOriginalMethod(rawQuery, database, new Object[]{selectQuery, null});
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }


        String[] colName = mCursor.getColumnNames();
        if (mCursor != null) {

            recordSet = "";
            mCursor.moveToFirst();

            do {
                StringBuilder sb = new StringBuilder();
                int columnsQty = mCursor.getColumnCount();

                for (int idx = 0; idx < columnsQty; ++idx) {
                        sb.append(colName[idx]);
                        sb.append("=");
                        sb.append(mCursor.getString(idx));
                        sb.append("@#@");


                }


                recordSet +=  sb.toString()+"$#$";

            } while (mCursor.moveToNext());

            recordSet += "";

            return recordSet;

        }// cursor if

        return  "null";
    }// funend


    public  static  void  PrintTrack()
    {
        XposedBridge.log("Dump Stack: "+ "---------------start----------------");
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {

                XposedBridge.log("Dump Stack"+i+": "+stackElements[i].getClassName()
                        +"----"+stackElements[i].getFileName()
                        +"----" + stackElements[i].getLineNumber()
                        +"----" +stackElements[i].getMethodName());
            }
        }
        XposedBridge.log("Dump Stack: "+"---------------over----------------");


    }//fun end
    //文件写入
    public static void writeFileData(String filename, String content){
        try {

            FileOutputStream fos =new FileOutputStream(filename,false);//获得FileOutputStream
            //将要写入的字符串转换为byte数组
            byte[]  bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.close();//关闭文件输出流

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //文件读取
    public static String readFileData(String fileName){
        String result="";
        try{
            FileInputStream fis = new FileInputStream(fileName);
            //获取文件长度
            int lenght = fis.available();
            byte[] buffer = new byte[lenght];
            fis.read(buffer);
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }
        }//class end




