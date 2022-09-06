package com.osp.npo.task;
import com.osp.npo.common.util.Util;
import com.osp.npo.common.util.database.DBPool;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.sql.*;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
/**
 * Created by QUYENLC on 12/07/2021.
 */
public class BackUpFileDataThread implements Runnable{
    private Connection connection;
    private String startTime;
    private String endTime;
    private int partition;
    private String data_directory;
    private static final Logger logger = Logger.getLogger(BackUpFileDataThread.class.getName());
    public void run() {
        try {
            connection = DBPool.makeDBConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Thời gian backup dữ liệu từ -> hiện tại
//        timeBackUp = timeBackUpDataTo();
        try{
            Properties prop = new Properties();
            prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
            startTime = prop.getProperty("startTime");
            endTime = prop.getProperty("endTime");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        startTime += " 00:00:00";
        endTime += " 23:59:59";
        boolean checkDateFrom = Util.validateDate(startTime);
        boolean checkToDate = Util.validateDate(endTime);
        int statusCheckDate = 0;
        String fromTime = startTime.replaceAll("-", "/");
        String toTime = endTime.replaceAll("-", "/");
        if (checkDateFrom && checkToDate) {
            Date from = new Date(fromTime);
            Date to = new Date(toTime);
            if (from.getTime() <= to.getTime()) {
                statusCheckDate = 1;
            } else {
                logger.warning("Vui lòng thử lại thời gian từ ngày phải nhỏ hơn hoặc bằng đến ngày");
                statusCheckDate = 0;
            }
        }
        else {
            statusCheckDate = 0;
        }
        if(statusCheckDate == 1){
            // Tạo mới bảng lưu trữ dữ liệu dự phòng
            createTableBackUp();
            int threads = 10;
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            List<Future<Boolean>> list = new ArrayList<Future<Boolean>>();
            for(int i=0;i<100;i++){
                BackUpFileDataCallable callable = new BackUpFileDataCallable(i,startTime,endTime);
                Future<Boolean> future = pool.submit(callable);
                list.add(future);
                this.partition=partition+1;
            }
//            for(Future<Boolean> fut : list){
//                try {
//                    //print the return value of Future, notice the output delay in console
//                    // because Future.get() waits for task to get completed
//                    logger.info(new Date()+ "::"+fut.get());
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
            pool.shutdown();
            // Sửa đổi tên bảng thành bảng backup
//        reNameTableFiledata();
            // Sửa đổi tên bảng
//            try {
//                reNameTable();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
        }
        else {
        }
    }

//    private String timeBackUpDataTo(){
//        String threesYearAgo = "";
//        java.util.Date datenow = new Date();
//        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
//        String dateNow = formatter.format(datenow);
//        String[] words = dateNow.split("-");
//        String year = words[0];
//        String month = words[1];
//        String day = words[2];
//        int yearNow = Integer.parseInt(year);
//        int threeYearAgo= yearNow - 3;
//        threesYearAgo = String.valueOf(threeYearAgo) + "-" + month + "-" + day;
//        return threesYearAgo;
//    }
    //Xóa bảng lưu trữ tạm thời nếu tồn tại
    private void dropTable(){
        String sql = "DROP TABLE IF EXISTS phpviet_file_data_temp";
        Statement stmt = null;
        try{
            connection = DBPool.makeDBConnection();
            if (connection != null) {
                stmt = connection.createStatement();
                stmt.executeUpdate(sql);
            }
            else {
                logger.warning("Không kết nối được đến database");
            }
        }
        catch (SQLException e) {
            logger.warning("Có lỗi xảy ra:"+e);
            e.printStackTrace();
        }
    }
    private void createTableBackUp() {
        try {
         //   dropTable();
            Util util = new Util();
            util.createFolderBU();
            StringBuilder myTableName = new StringBuilder();
            myTableName.append("CREATE TABLE IF NOT EXISTS phpviet_file_data_temp (\n" +
                    "  `file_id` varchar(50) NOT NULL DEFAULT '0',\n" +
                    "  `partition_id` int(10) NOT NULL DEFAULT 0,\n" +
                    "  `data` longblob,\n" +
                    "  `html` longtext,\n" +
                    "  PRIMARY KEY (`file_id`,`partition_id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8\n" +
                    "PARTITION BY HASH (partition_id)\n");
            myTableName.append("(");
            try{
                Properties prop = new Properties();
                prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
                data_directory = prop.getProperty("data_directory");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            for(int i=0;i<=99;i++){
                if(i == 99){
                    myTableName.append("PARTITION `p"+i+"` DATA DIRECTORY = '"+data_directory+"'");
                    myTableName.append(");");
                }
                else {
                    myTableName.append("PARTITION `p"+i+"` DATA DIRECTORY = '"+data_directory+"',\n");
                }
            }
            if (connection != null) {
                Statement statement = connection.createStatement();
                statement.executeUpdate(String.valueOf(myTableName));
            } else {
                logger.warning("Không kết nối được đến database");
            }
        } catch (SQLException e) {
            logger.warning("Có lỗi xảy ra:"+e);
            e.printStackTrace();
        }
    }

//    private void reNameTableFiledata(){
//        Date datenow = new Date();
//        Format formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
//        String now = formatter.format(datenow);
//        String sql = "RENAME TABLE phpviet_file_data TO phpviet_file_data_backup_"+now+"";
//        Statement stmt = null;
//        try{
//            if (connection != null) {
//                stmt = connection.createStatement();
//                stmt.executeUpdate(sql);
//            }
//            else {
//  				logger.warning("Không kết nối được đến database");
//            }
//        } catch (SQLException e) {
//  				logger.warning("Có lỗi xảy ra:"+e);
//            e.printStackTrace();
//        }
//    }
    private void reNameTable() throws SQLException {
        Date datenow = new Date();
        Format formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String now = formatter.format(datenow);
        String sql = "RENAME TABLE phpviet_file_data_temp TO phpviet_file_data_backup_"+now+"";
        Statement stmt = null;
        try{
            if (connection != null) {
                stmt = connection.createStatement();
                stmt.executeUpdate(sql);
            }
            else {
                logger.warning("Không kết nối được đến database");
            }
        } catch (SQLException e) {
            logger.warning("Có lỗi xảy ra:"+e);
            e.printStackTrace();
        }
        finally {
            connection.close();
        }
    }
}
