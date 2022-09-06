package com.osp.npo.task;
import com.osp.npo.common.controller.BackupController;
import com.osp.npo.common.controller.RestoreController;
import com.osp.npo.common.util.Util;
import com.osp.npo.common.util.database.DBConfig;
import com.osp.npo.common.util.database.DBPool;
import java.io.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Created by QUYENLC on 12/07/2021.
 */
public class BackUpDBStart extends Thread{
	private static final Logger logger = Logger.getLogger(BackUpDBStart.class.getName());
	static BufferedReader keyboard;
	/** Connection */
	private static Connection connection = null;
	private int statusProcess = 0;
	static
	{
		keyboard = new BufferedReader(new InputStreamReader(System.in));
	}
	public static void main(String[] arg) {
		new BackUpDBStart().start();

		try
		{
			DBConfig.loadProperties();
		}
		catch(IOException e)
		{
			System.out.println((new StringBuilder("Khong tim thay file cau hinh: ")).append(e.getMessage()).toString());
		}

		DBPool.load();

	}
	
	
    public void run () {
		new BackupThreadAuto().start();
		new BackUpFileDateDailyThreadAuto().start();
		new RemoveFileBackUpThreadAuto().start();
		for(; true; showMenu()) { }
	}

	private void showMenu() {


// In string format
		System.out.println("---------------------");
		System.out.println("1 - Chọn backup tất cả dữ liệu trong database");
		System.out.println("2 - Chọn backup dữ liệu theo bảng");
		System.out.println("3 - Chọn backup dữ liệu theo bảng với thời gian");
		System.out.println("4 - Chọn restore dữ liệu theo file backup");
		System.out.println("5 - Tối ưu hóa dữ liệu lưu trữ");
		System.out.println("6 - Chọn backup các bảng history");
		System.out.println("7 - Chọn backup bảng npo_transaction_property");
		System.out.println("8 - Thoát khỏi chương trình");
		System.out.println("---------------------");
		String option = "";
		try {
			option = keyboard.readLine();
			option = option.replaceAll("\\s+", "");
		} catch (Exception e) {
			System.out.println("showMenu:" + e.getMessage());
		}
		BackupController backupController = new BackupController();
		RestoreController restoreController = new RestoreController();
			if ("1".equals(option)) {
                logger.info("----------Tiến hành backup tất cả dữ liệu------------");
				backupController.backupDBAll();
			} else if ("2".equals(option)) {
                logger.info("----------Bắt đầu backup dữ liệu theo bảng-----------");
				String typeBackUp = Util.getParam("Bạn hãy chọn loại backup dữ liệu? T(table)", "T");
				if ("T".equalsIgnoreCase(typeBackUp)) {
					backupController.backupDBBySelectTable();
				} else {
                    logger.warning("Không có lựa chọn theo yêu cầu.Vui lòng thử lại");
				}
			} else if ("3".equals(option)) {
                logger.info("----------Bắt đầu backup dữ liệu bảng phpviet_file_data với thời gian-----------");
				backupController.backupDBBySelectTableWithTime();
			} else if ("4".equals(option)) {
				System.out.println("----------Bắt đầu restore dữ liệu-----------");
				restoreController.restoreThread();
			} else if ("5".equals(option)) {
                logger.info("----------Tiến hành tối ưu hóa dữ liệu-----------");
				new BackUpFileDataThread().run();
			}
			else if ("6".equals(option)) {
				logger.info("----------Tiến hành backup dữ liệu các bảng history-----------");
				backupController.backupHistoryTables();
			}
			else if ("7".equals(option)) {
				logger.info("----------Tiến hành backup bảng npo_transaction_property-----------");
				backupController.backupLargeDataTables();
			}
			else if ("8".equals(option)) {
				Util.exit();
			}
			else {
				logger.warning("Không có lựa chọn "+option+" theo yêu cầu.Vui lòng thử lại");
			}
	}
}
