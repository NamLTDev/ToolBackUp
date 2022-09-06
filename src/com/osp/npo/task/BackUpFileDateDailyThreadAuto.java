package com.osp.npo.task;

import com.osp.npo.common.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by QUYENLC on 12/07/2021.
 */
public class BackUpFileDateDailyThreadAuto extends Thread {
    private static final Logger logger = Logger.getLogger(BackupThreadAuto.class.getName());
    private static final String file_name_bat = "backup.bat";
    private String database;
    private String user;
    private String pass;
    private String host;
    private String port;
    private String system_backup_os;
    private String system_backup_database_folder;
    private String system_backupTable_database_folder;
    private String system_mysql_mysqldump_folder;
    private String status;
    private String fileDataBUWithTime;
    private String timeBackupFileData;
    private String dateTime;

    public static void main(String[] arg) {
        new BackupThreadAuto().start();
    }

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
            status = prop.getProperty("status");
            system_backup_database_folder = prop.getProperty("system_backup_database_folder");
            system_backupTable_database_folder = prop.getProperty("system_backupTable_database_folder");
            system_mysql_mysqldump_folder = prop.getProperty("system_mysql_mysqldump_folder");
            fileDataBUWithTime = prop.getProperty("fileDataBUWithTime");
            timeBackupFileData = prop.getProperty("timeBackupFileData");
            dateTime = prop.getProperty("dateTime");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        init();
        while (true) {
            Calendar cal = Calendar.getInstance();
            Util util = new Util();
            List<String> listDateBackup = util.convertListBooleanToListString();
            String dayOfWeek = util.getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
            if (listDateBackup.size() > 0) {
                for (int i = 0; i < listDateBackup.size(); i++) {
                    if (dayOfWeek.equals(listDateBackup.get(i))) {
                        try {
                            if (cal.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(timeBackupFileData.split(":")[0])) {
                                if (cal.get(Calendar.MINUTE) == Integer.parseInt(timeBackupFileData.split(":")[1])) {
                                    if (status.equals("true")) {
                                        List<String> date = checkDateBUBefore();
                                        this.backUpFileDateDaily(date.get(0), date.get(1));
                                    } else {
                                    }
                                } else {
                                }
                            } else {
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
            try {
                sleep(60000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private List<String> checkDateBUBefore() {
        init();
        List<String> date = new ArrayList<>();
        // Thá»�i gian back up tá»«
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateUpdate = null;
        try {
            dateUpdate = formatter.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calF = Calendar.getInstance();
        calF.setTime(dateUpdate);
        if (System.currentTimeMillis() >= calF.getTimeInMillis()) {
            calF.setTimeInMillis(calF.getTimeInMillis()+1000);
            DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String dateF = df2.format(calF.getTime());
            date.add(dateF);
            // Ä�áº¿n
            Calendar calT = Calendar.getInstance();
            DateFormat dt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String dateT = dt.format(calT.getTime());
            date.add(dateT);
        }
        return date;
    }

    private void backUpFileDateDaily(String dateFrom, String toDate) {
        try {
            String fileName = createBackupFileBat(dateFrom, toDate);
            Runtime c = Runtime.getRuntime();
            String cmd = "cmd /c start " + system_backup_database_folder + "backup.bat";
            Process pro;
            if (system_backup_os.equals("0")) {
                pro = c.exec(cmd, null, new File(system_backup_database_folder));
            } else {
                pro = c.exec(system_backup_database_folder + "backup.sh",
                        null, new File(system_backup_database_folder));
            }
            if (pro.waitFor() == 0) {
                logger.info("Đang tiến hành backup tất cả dữ liệu.Vui lòng đợi");
                while (true) {
                    Util util = new Util();
                    long proccessID = util.checkStatusProccess();
                    if (proccessID > 0) {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                            long sizeFile = util.sizeFileSqlBackUp(fileName, 1);
                        if (sizeFile >=0) {
                            logger.info("Back up dữ liệu bảng file_data thành công");
                            //Cap nhat lai thoi gian data da chay trong config.xml
                            Properties prop = new Properties();
                            try {
                                prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // Xử lý dữ liệu đến ngày
                            String hoursTo = toDate.substring(11, 19);
                            hoursTo = hoursTo.replaceAll("-", ":");
                            toDate = toDate.substring(0, 10);
                            toDate += " ";
                            toDate += hoursTo;
                            File configFile = new File(System.getProperty("user.dir")+"/config.xml");
                            OutputStream outputStream = new FileOutputStream(configFile);
                            prop.setProperty("dateTime", toDate+"");
                            prop.storeToXML(outputStream, "");
                            outputStream.close();
                            break;
                        }
                    }
                }
            } else {
                System.out.println("Back up dữ liệu thất bại.Vui lòng thử lại");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createBackupFileBat(String dateFrom, String toDate) {
        init();
        Util util = new Util();
        util.createFolderBU();
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
            String filename = "backup_file_data_" + dateFrom + "_" + toDate +".sql";
            // Xử lý dữ liệu từ ngày
            String hoursFrom = dateFrom.substring(11, 19);
            hoursFrom = hoursFrom.replaceAll("-", ":");
            dateFrom = dateFrom.substring(0, 10);
            dateFrom += " ";
            dateFrom +=hoursFrom;
            // Xử lý dữ liệu đến ngày
            String hoursTo = toDate.substring(11, 19);
            hoursTo = hoursTo.replaceAll("-", ":");
            toDate = toDate.substring(0, 10);
            toDate += " ";
            toDate += hoursTo;
            String sql = "file_id in(select file_id from phpviet_file where upload_date >= '" + dateFrom + "' and upload_date <= '" + toDate + "')";
            writer.println("mysqldump -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " --tables" + " " + fileDataBUWithTime + " --lock-all-tables --where=\"" + sql + "\" -r \"" + system_backupTable_database_folder + filename);
            writer.println("Exit");
            writer.close();
            return filename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
