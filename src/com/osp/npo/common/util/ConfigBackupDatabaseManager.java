package com.osp.npo.common.util;
import com.osp.npo.task.BackupThreadAuto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by QUYENLC on 12/07/2021.
 */
public class ConfigBackupDatabaseManager {
    private static final Logger logger = Logger.getLogger(BackupThreadAuto.class.getName());
    private static final String file_name_bat = "backup.bat";
    private String database;
    private String user;
    private String pass;
    private String host;
    private String port;
    private String system_backup_os;
    private String system_backup_database_folder;
    private String fileDataBUWithTime;
    private String system_mysql_mysqldump_folder;

    public void init() {
        try {
            Properties prop = new Properties();
            database = prop.getProperty("database");
            user = prop.getProperty("userName");
            pass = prop.getProperty("password");
            host = prop.getProperty("host");
            port = prop.getProperty("port");
            system_backup_os = prop.getProperty("system_backup_os");
            system_backup_database_folder = prop.getProperty("system_backup_database_folder");
            system_mysql_mysqldump_folder = prop.getProperty("system_mysql_mysqldump_folder");
            fileDataBUWithTime = prop.getProperty("fileDataBUWithTime");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String createBackupFileBat(int type) {
        System.out.println("Vào ConfigBackupDatabaseManager hàm createBackupFileBat ---------------------------------");
        init();
        try {
            String filebat = file_name_bat;
            if (system_backup_os.equals("1")){
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
            if(date < 10){
                DATE = "0" + DATE;
            }
            if(monday < 10){
                MONDAY = "0" + MONDAY;
            }
            String filename = "backup-" + cal.get(Calendar.YEAR) + "-" + MONDAY + "-" + DATE + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE) + "-" + cal.get(Calendar.SECOND) + ".sql";
             if(type == 0){
                writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " --ignore-table="+database+""+"."+"phpviet_file_data"+" -r \"" + system_backup_database_folder + filename);
            }
            else if(type == 1) {
                String tableBackUp = Util.getParam("Vui lòng chọn bảng dữ liệu muốn backup?(Lưu ý:backup nhiều bảng thì phải nhập cách nhau bằng dấu phẩy)", "");
                if(tableBackUp == ""){
                    Util.exit();
                }
                else{
                    tableBackUp = tableBackUp.replaceAll(","," ");
                    writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " "+ tableBackUp +" -r \"" + system_backup_database_folder + filename);
                }
            }
            else if(type == 2){
                 Date datenow = new Date();
                 Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                 String now = formatter.format(datenow);
                 String timeFrom = Util.getParam("Chọn thời gian từ ngày(nhập thời gian theo mẫu vd: 1900-01-01(năm-tháng-ngày):", "");
                 String timeTo = Util.getParam("Chọn thời gian đến ngày(nhập thời gian theo mẫu vd: 1900-01-01(năm-tháng-ngày):", "");
                 if(timeFrom == ""){
                     Util.exit();
                 }else {
                     timeFrom = timeFrom + " 00:00:00";
                 }
                 if(timeTo == ""){
                     timeTo = now;
                 }else {
                     timeTo = timeTo + " 23:59:59";
                 }
                 String fromTime = timeFrom.replaceAll("-","/");
                 String toTime = timeTo.replaceAll("-","/");
                 Date from = new Date(fromTime);
                 Date to = new Date(toTime);
                 Long millisBetween = to.getTime() - from.getTime();
                 Long days = millisBetween / (1000 * 3600 * 24);
                 int result = Math.round(Math.abs(days));
                 if(from.getTime() <= to.getTime()){
                     if(result >= 0){
                         String sql = "file_id in(select file_id from phpviet_file where upload_date >= '"+timeFrom+"' and upload_date <= '"+timeTo+"')";
                         writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " --tables" +" " + fileDataBUWithTime + " --lock-all-tables --where=\""+sql+"\" -r \""  + system_backup_database_folder + filename);
                     }
                 }
                 else {
                     logger.warning("Vui lòng thử lại thời gian từ ngày phải nhỏ hơn hoặc bằng đến ngày");
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
}
