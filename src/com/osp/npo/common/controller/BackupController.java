package com.osp.npo.common.controller;
import com.osp.npo.common.util.ConfigDatabaseManage;
import com.osp.npo.common.util.Util;
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;
import static java.lang.Thread.sleep;

public class BackupController {
    private static final Logger logger = Logger.getLogger(BackupController.class.getName());
    private String system_backup_os;
    private String system_backup_database_folder;

    public void init() {
        try {
            Properties prop = new Properties();
            prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
            system_backup_os = prop.getProperty("system_backup_os");
            system_backup_database_folder = prop.getProperty("system_backup_database_folder");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void backupDBAll() {
        init();
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

    public void backupLargeDataTables(){
        init();
        try{
            ConfigDatabaseManage manage = new ConfigDatabaseManage();
            String fileName = manage.createBackupFileBat(4);
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
                logger.info("Đang tiến hành backup dữ liệu.Vui lòng đợi");
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

    public void backupHistoryTables(){
        init();
        try {
            ConfigDatabaseManage manage = new ConfigDatabaseManage();
            String fileName = manage.createBackupFileBat(3);
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
                logger.info("Đang tiến hành backup dữ liệu các bảng history.Vui lòng đợi");
                logger.info("Backup dữ liệu thành công");
            } else {
                System.out.println("Back up dữ liệu thất bại.Vui lòng thử lại");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void backupDBBySelectTable() {
        init();
        try {
            ConfigDatabaseManage cfManager = new ConfigDatabaseManage();
            String fileName = cfManager.createBackupFileBat(1);
            if (fileName != "") {
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
                    logger.info("Đang tiến hành backup dữ liệu theo bảng.Vui lòng đợi");
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
                            long sizeFile = util.sizeFileSqlBackUp(fileName,1);
                            if (sizeFile > 0) {
                                logger.info("Back up dữ liệu theo bảng thành công");
                                break;
                            }
                        }
                    }
                } else {
                    logger.warning("Back up dữ liệu theo bảng thất bại.Vui lòng thử lại");
                }
            }
            else {
//                logger.warning("Đã xảy ra lỗi. Vui lòng thử lại");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void backupDBBySelectTableWithTime() {
        init();
        try {
            ConfigDatabaseManage cfManager = new ConfigDatabaseManage();
            String fileName = cfManager.createBackupFileBat(2);
            if (fileName != "") {
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
                    logger.info("Đang tiến hành backup dữ liệu theo bảng.Vui lòng đợi");
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
                            long sizeFile = util.sizeFileSqlBackUp(fileName,1);
                            if (sizeFile > 0) {
                                logger.info("Back up dữ liệu theo bảng thành công");
                                break;
                            }
                        }
                    }
                } else {
                    logger.warning("Back up dữ liệu theo bảng thất bại.Vui lòng thử lại");
                }
            } else {
//                logger.warning("Đã xảy ra lỗi. Vui lòng thử lại");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
