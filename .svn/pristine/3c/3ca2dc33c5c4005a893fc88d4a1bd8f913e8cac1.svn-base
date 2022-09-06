package com.osp.npo.common.util.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class DBConfig
{

    public DBConfig()
    {
    }

    public static boolean isDatabaseEnabled()
    {
        return databaseEnabled;
    }

    public static void loadProperties()
        throws IOException
    {


            prop.loadFromXML(new FileInputStream(System.getProperty("user.dir")+"/config.xml"));



        db_DriverClassName = prop.getProperty("driver");
        db_URL = prop.getProperty("dbName");
        db_user = prop.getProperty("userName");
        db_password = prop.getProperty("password");

        db_MaxConnections = Integer.parseInt(prop.getProperty("db_max_connections"));

    }

    static byte getByteProperty(String propName, byte defaultValue)
    {
        return Byte.parseByte(prop.getProperty(propName, Byte.toString(defaultValue)).trim());
    }

    static int getIntProperty(String propName, int defaultValue)
    {
        return Integer.parseInt(prop.getProperty(propName, Integer.toString(defaultValue)).trim());
    }

    public static String db_DriverClassName = "";
    public static String db_URL = "";
    public static String db_user = "";
    public static String db_password = "";
    public static int db_MaxConnections = 100;
    public static boolean databaseEnabled = true;
    private static Properties prop = new Properties();

}
