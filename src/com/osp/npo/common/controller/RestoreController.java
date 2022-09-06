package com.osp.npo.common.controller;

import com.osp.npo.common.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class RestoreController {
    private static final Logger logger = Logger.getLogger(RestoreController.class.getName());
    private String database;
    private String user;
    private String pass;
    private String host;
    private String port;
    private String system_backup_os;
    private String system_backup_database_folder;
    private String system_backupAll_database_folder;
    private String system_backupTable_database_folder;
    private String system_mysql_mysqldump_folder;

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
            system_backupAll_database_folder = prop.getProperty("system_backupAll_database_folder");
            system_backupTable_database_folder = prop.getProperty("system_backupTable_database_folder");
            system_mysql_mysqldump_folder = prop.getProperty("system_mysql_mysqldump_folder");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void restoreThread() {
        init();
        Util uTil = new Util();
        uTil.createFolderBU();
        boolean checkFileExit = false;
        String restore = Util.getParam("Bạn có muốn khôi phục dữ liệu? Y/N", "Y");
        restore = restore.replaceAll("\\s+", "");
        String fileBackUp = "";
        switch (restore) {
            case "Y":
                fileBackUp = Util.getParam("Chọn file để khôi phục dữ liệu:", "");
                fileBackUp = fileBackUp.replaceAll("\\s+", "");
                Util util1 = new Util();
                List<String> lstFileSQL = util1.findFileSqlBackUp();
                switch (fileBackUp) {
                        case "":
                        logger.warning("Vui lòng chọn file để khôi phục dữ liệu");
                        break;
                    default:
                        for (String fileSQL : lstFileSQL) {
                            if (fileSQL.equals(fileBackUp)) {
                                checkFileExit = true;
                            }
                        }
                        if(!checkFileExit){
                            logger.info("File lựa chọn khôi phục không tồn tại");
                        }
                        break;
                }
                break;
            case "y":
                fileBackUp = Util.getParam("Chọn file để khôi phục dữ liệu:", "");
                fileBackUp = fileBackUp.replaceAll("\\s+", "");
                Util util2 = new Util();
                List<String> lstFileSQL1 = util2.findFileSqlBackUp();
                switch (fileBackUp) {
                    case "":
                        logger.warning("Vui lòng chọn file để khôi phục dữ liệu");
                        break;
                    default:
                        for (String fileSQL : lstFileSQL1) {
                            if (fileSQL.equals(fileBackUp)) {
                                checkFileExit = true;
                            }
                        }
                        if(!checkFileExit){
                            logger.info("File lựa chọn khôi phục không tồn tại");
                        }
                        break;
                }
                break;
            default:
                logger.warning("Vui lòng chọn đồng ý để thực thi yêu cầu");
                break;
    }
        if (checkFileExit) {
            String[] cmd = new String[10];
            int i = 0;
            cmd[i++] = system_mysql_mysqldump_folder.substring(0, 2);
            cmd[i++] = "cd \"" + system_mysql_mysqldump_folder + "\"";
            Util util = new Util();
            List<String> fileBackUpAll = util.findFileSqlBackUpAll();
            boolean checkBUAllExits = fileBackUpAll.contains(fileBackUp);
            List<String> fileBackUpTable = util.findFileSqlBackUpTable();
            boolean checkBUTableExits = fileBackUpTable.contains(fileBackUp);
            if(checkBUAllExits){
                cmd[i++] = "mysql -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " < \"" + system_backupAll_database_folder + fileBackUp + "\"";
            }
            else if(checkBUTableExits){
                cmd[i++] = "mysql -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " < \"" + system_backupTable_database_folder + fileBackUp + "\"";
            }
            Runtime c = Runtime.getRuntime();
            String fileretore = "restore.bat";
            if (system_backup_os.equals("1")) {
                fileretore = "restore.sh";
            }
            createFileBackUpOrRetore(cmd, fileretore, i);
            String cmdStr = "cmd /c start " + system_backup_database_folder + "restore.bat";
            try {
                Process pro;
                if (system_backup_os.equals("0")) {
                    pro = c.exec(cmdStr, null, new File(system_backup_database_folder));
                } else {
                    pro = c.exec(system_backup_database_folder + "restore.sh",
                            null, new File(system_backup_database_folder));
                }
                if (pro.waitFor() == 0) {
                    logger.info("Đang tiến hành khôi phục tất cả dữ liệu.Vui lòng đợi");
                    while (true) {
                        Util util3 = new Util();
                        long proccessID = util3.checkStatusProccess();
                        if (proccessID > 0) {
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            long sizeFile = 0;
                            if(checkBUAllExits){
                                sizeFile = util3.sizeFileSqlBackUp(fileBackUp,0);
                            }
                            else if(checkBUTableExits){
                                sizeFile = util3.sizeFileSqlBackUp(fileBackUp,1);
                            }
                            if (sizeFile > 0) {
                                logger.info("Khôi phục dữ liệu thành công");
                                break;
                            }
                        }
                    }
                } else {
                    System.out.println("Khôi phục dữ liệu thất bại.Vui lòng thử lại");
                }
            } catch (Exception e) {
                System.out.println("Bạn đã gặp lỗi trong quá trình khôi phục dữ liệu");
            }
        } else {
        }
    }

    public void createFileBackUpOrRetore(String[] fileContent, String fileName, int length) {
        try {
            File file = new File(system_backup_database_folder + fileName);
            File folder = new File(system_backup_database_folder);
            if (file.exists()) {
                file.delete();
            } else {
                if (!folder.exists())
                    folder.mkdirs();
            }
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            for (int i = 0; i < length; i++) {
                writer.println(fileContent[i]);
            }
            writer.println("Exit");
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
