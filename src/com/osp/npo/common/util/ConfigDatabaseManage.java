package com.osp.npo.common.util;

import com.osp.npo.common.util.database.DBPool;
import com.osp.npo.task.BackupThreadAuto;

import java.io.*;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by QUYENLC on 12/07/2021.
 */
public class ConfigDatabaseManage {
    private static final Logger logger = Logger.getLogger(BackupThreadAuto.class.getName());
    private static final String file_name_bat = "backup.bat";
    private String database;
    private String user;
    private String pass;
    private String host;
    private String port;
    private String system_backup_os;
    private String system_backup_database_folder;
    private String system_backupAll_database_folder;
    private String system_backupTable_database_folder;
    private String fileDataBUWithTime;
    private String system_mysql_mysqldump_folder;
    private String dateTime;
    private String latestBUTime;

//    public static String lastestBUTime = "2021-01-01";

    public void init() {
        try {
            Properties prop = new Properties();
            prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
            database = prop.getProperty("database");
            user = prop.getProperty("userName");
            pass = prop.getProperty("password");
            host = prop.getProperty("host");
            port = prop.getProperty("port");
            system_backup_os = prop.getProperty("system_backup_os");
            system_backup_database_folder = prop.getProperty("system_backup_database_folder");
            system_backupAll_database_folder =  prop.getProperty("system_backupAll_database_folder");
            system_backupTable_database_folder = prop.getProperty("system_backupTable_database_folder");
            system_mysql_mysqldump_folder = prop.getProperty("system_mysql_mysqldump_folder");
            fileDataBUWithTime = prop.getProperty("fileDataBUWithTime");
            dateTime = prop.getProperty("dateTime");
            latestBUTime = prop.getProperty("latestBUTime");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String createBackupFileBat(int type) {
        init();
        Util util = new Util();
        util.createFolderBU();
        int idFrom = 0;
        int idTo = 999999;
        try {
            String filebat = file_name_bat;
            if (system_backup_os.equals("1")) {
                filebat = "backup.sh";
            }
            File file = new File(system_backup_database_folder + filebat);
            File folder = new File(system_backup_database_folder);
            if (file.exists()) {
                file.delete();
            } else {
                if (!folder.exists())
                    folder.mkdirs();
            }
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.println("cd " + system_mysql_mysqldump_folder);
            String oDia = system_mysql_mysqldump_folder.substring(0, 2);
            writer.println(oDia);
            Calendar cal = Calendar.getInstance();
            int date = cal.get(Calendar.DATE);
            int monday = cal.get(Calendar.MONDAY) + 1;
            String DATE = String.valueOf(cal.get(Calendar.DATE));
            String MONDAY = String.valueOf(cal.get(Calendar.MONDAY) + 1);
            if (date < 10) {
                DATE = "0" + DATE;
            }
            if (monday < 10) {
                MONDAY = "0" + MONDAY;
            }
            String filename = "";
            if (type == 0) {
                filename = "backup-" + cal.get(Calendar.YEAR) + "-" + MONDAY + "-" + DATE + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE) + "-" + cal.get(Calendar.SECOND) + ".sql";
//                writer.println("mysqldump -u" + user + " -h" + host + " -P" + port + " " + database + " -r \"" + system_backupAll_database_folder + filename);
                writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " --ignore-table=" + database + "" + "." + "phpviet_file_data" + " -r \"" + system_backupAll_database_folder + filename);
            } else if (type == 1) {
                filename = "backup-" + cal.get(Calendar.YEAR) + "-" + MONDAY + "-" + DATE + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE) + "-" + cal.get(Calendar.SECOND) + ".sql";
                List<String> listTableName = new ArrayList<>();
                try {
                    listTableName = showAllTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String tableBackUp = Util.getParam("Vui lòng chọn bảng dữ liệu muốn backup?(Lưu ý:backup nhiều bảng thì phải nhập cách nhau bằng dấu phẩy)", "");
                switch (tableBackUp){
                    case "":
                        logger.warning("Vui lòng chọn bảng dữ liệu muốn backup");
                        break;
                    default:
                        StringBuilder tbBackUp = new StringBuilder();
                        boolean tableExit = false;
                        String[] listTableBackUp = tableBackUp.split(",");
                        for(String table : listTableBackUp){
                            table = table.replaceAll("\\s+", "");
                            tableExit = listTableName.contains(table);
                            if(tableExit){
                                tbBackUp.append(table);
                                tbBackUp.append(" ");
                            }
                            else {
                                logger.warning("Bảng "+table+" không tồn tại trong cơ sở dữ liệu");
                                filename = "";
                                break;
                            }
                        }
                        writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " " + tbBackUp + " -r \"" + system_backupTable_database_folder + filename);
                        break;
                }
            } else if (type == 2) {
                filename = "backup-" + cal.get(Calendar.YEAR) + "-" + MONDAY + "-" + DATE + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE) + "-" + cal.get(Calendar.SECOND) + ".sql";
                Date datenow = new Date();
                Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = formatter.format(datenow);
                String timeFrom = Util.getParam("Chọn thời gian từ ngày(nhập thời gian theo mẫu vd: 1900-01-01(năm-tháng-ngày):", "");
                String timeTo = Util.getParam("Chọn thời gian đến ngày(nhập thời gian theo mẫu vd: 1900-01-01(năm-tháng-ngày):", "");
                timeFrom = timeFrom.replaceAll("\\s+", "");
                timeTo = timeTo.replaceAll("\\s+", "");
                switch (timeFrom){
                    case "":
                    	logger.warning("Vui lòng chọn thời gian từ");
                        break;
                    default:
                        timeFrom = timeFrom + " 00:00:00";
                        break;
                }
                switch (timeTo){
                    case "":
                        timeTo = now;
                        break;
                    default:
                        timeTo = timeTo + " 23:59:59";
                        break;
                }
                boolean checkDateFrom = false;
                if(timeFrom != ""){
                   checkDateFrom = Util.validateDate(timeFrom);
                }
                boolean checkToDate = Util.validateDate(timeTo);
                String fromTime = timeFrom.replaceAll("-", "/");
                String toTime = timeTo.replaceAll("-", "/");
                if (checkDateFrom && checkToDate) {
                    Date from = new Date(fromTime);
                    Date to = new Date(toTime);
                    Long millisBetween = to.getTime() - from.getTime();
                    Long days = millisBetween / (1000 * 3600 * 24);
                    int result = Math.round(Math.abs(days));
                    if (from.getTime() <= to.getTime()) {
                        if (result >= 0) {
                            String sql = "file_id in(select file_id from phpviet_file where upload_date >= '" + timeFrom + "' and upload_date <= '" + timeTo + "')";
                            writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " --tables" + " " + fileDataBUWithTime + " --lock-all-tables --where=\"" + sql + "\" -r \"" + system_backupTable_database_folder + filename);
                        }
                    } else {
                    	logger.warning("Vui lòng thử lại thời gian từ ngày phải nhỏ hơn hoặc bằng đến ngày");
                        filename = "";
                    }
                } else {
                    filename = "";
                }
            }
            else if(type == 3){
                LocalDate today = LocalDate.now();
                String yesterday = (today.minusDays(1)).format(DateTimeFormatter.ISO_DATE);
                filename = "backup-historyTables" + cal.get(Calendar.YEAR) + "-" + MONDAY + "-" + DATE + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE) + "-" + cal.get(Calendar.SECOND) + ".sql";
                writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " --tables" + " npo_access_history npo_contract_history npo_data_prevent_history" + " --where=\"execute_date_time >= '"+ latestBUTime +"'\" -r \"" + system_backupTable_database_folder + filename);
                String dt = cal.get(Calendar.YEAR) + "-" + MONDAY + "-" + DATE + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE);
                //manage.lastestBUTime = lastestBUTime.replaceFirst(".*",dt);
                Properties prop = new Properties();
                prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
                prop.setProperty("latestBUTime",dt);
                prop.storeToXML(new FileOutputStream(System.getProperty("user.dir") + "/config.xml"),"cmt");
            }
            else if(type == 4){
                int fileNumber;
                try {
                    fileNumber = fileNumber();
                    for(int i = 1; i <= fileNumber ; i++){
                        filename = "backup-npo_transaction_property-" + i + "-" + cal.get(Calendar.YEAR) + "-" + MONDAY + "-" + DATE + ".sql";
                        writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " --tables" + " npo_transaction_property" + " --where=\"tpid between " + idFrom + " and " + idTo + "\" -r \"" + system_backupAll_database_folder + filename);
                        idFrom = idFrom + 1000000;
                        idTo = idTo + 1000000;
                    }
                    System.out.println(fileNumber);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            writer.println("Exit");
            writer.close();
            return filename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> showAllTable() throws SQLException {
        String sql = "SHOW TABLES";
        Connection connection = DBPool.makeDBConnection();
        Statement stmt = null;
        ResultSet rs = null;
        List<String> listTableName = new ArrayList<>();
        try {
            if (connection != null) {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
                System.out.println("----------Tất cả bảng dữ liệu trong database----------");
                while (rs.next()) {
                    listTableName.add(rs.getString(1));
                    System.out.println(rs.getString(1));
                }
            } else {
            	 logger.warning("Không kết nối được đến database");
            }
        } catch (SQLException e) {
        	logger.warning("Có lỗi xảy ra:" + e);
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return listTableName;
    }

    public int fileNumber()  throws SQLException{
        String sql = "Select max(tpid) from npo_transaction_property";
        Connection connection = DBPool.makeDBConnection();
        Statement stmt = null;
        ResultSet rs = null;
        int maxId = 0;
        int amout = 0;
        try {
            if (connection != null) {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
                System.out.println("---Số file backup---");
                while (rs.next()) {
                    maxId = maxId + rs.getInt(1);
                    int surplus = maxId%1000000;
                    if(surplus != 0){
                        amout = amout + maxId/1000000 + 1;
                    }
                    else {
                        amout = amout + maxId/1000000;
                    }
                }
            } else {
                logger.warning("Không kết nối được đến database");
            }
        } catch (SQLException e) {
            logger.warning("Có lỗi xảy ra:" + e);
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return amout;
    }
}
