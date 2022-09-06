package com.osp.npo.task;

import com.osp.npo.common.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by QUYENLC on 12/07/2021.
 */
public class BackupThreadAuto extends Thread {
    private static final Logger logger = Logger.getLogger(BackupThreadAuto.class.getName());
    private String system_backup_os;
    private String system_backup_database_folder;
    private String status;
    private String timeBackup;

    public static void main(String[] arg) {
        new BackupThreadAuto().start();
    }

    public void init() {
        try {
            Properties prop = new Properties();
            prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
            system_backup_os = prop.getProperty("system_backup_os");
            system_backup_database_folder = prop.getProperty("system_backup_database_folder");
            status = prop.getProperty("status");
            timeBackup = prop.getProperty("timeBackup");
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
                            if (cal.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(timeBackup.split(":")[0])) {
                                if (cal.get(Calendar.MINUTE) == Integer.parseInt(timeBackup.split(":")[1])) {
                                    if (status.equals("true")) {
//                                      logger.info("Tiến hành backup-----------------------------------");
                                        this.backupDBAll();
                                    } else {
//                                      logger.info("Ngày không tiến hành sao lưu dữ liệu-----------------------------------");
                                    }
                                } else {
                                }
                            }
                            else {
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

    public void backupDBAll() {
        try {
            ConfigDatabaseManage cfManager = new ConfigDatabaseManage();
            String fileName = cfManager.createBackupFileBat(0);
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
                        long sizeFile = util.sizeFileSqlBackUp(fileName,0);
                        if (sizeFile > 0) {
                            logger.info("Back up tất cả dữ liệu thành công");
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
}
