package com.osp.npo.common.util;
import com.osp.npo.common.util.database.DBPool;

import java.io.*;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by QUYENLC on 12/07/2021.
 */
public class Util {
    private static final Logger logger = Logger.getLogger(Util.class.getName());
    private Connection connection;
    static BufferedReader keyboard;
    private String datesConfig;
    private String system_backup_database_folder;
    private String system_backupAll_database_folder;
    private String system_backupTable_database_folder;
    private String data_directory;
    static
    {
        keyboard = new BufferedReader(new InputStreamReader(System.in));
    }


    public void init() {
        try {
            Properties prop = new Properties();
            prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
            datesConfig = prop.getProperty("datesConfig");
            system_backup_database_folder = prop.getProperty("system_backup_database_folder");
            system_backupTable_database_folder = prop.getProperty("system_backupTable_database_folder");
            system_backupAll_database_folder =  prop.getProperty("system_backupAll_database_folder");
            data_directory = prop.getProperty("data_directory");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getParam(String prompt, String defaultValue)
    {
        String value = "";
        String promptFull = prompt;
        promptFull = promptFull + (defaultValue == null ? "" : " [" + defaultValue + "] ");
        System.out.print(promptFull);
        try
        {
            value = keyboard.readLine();
        }
        catch(IOException ioexception) { }
        if(value.compareTo("") == 0)
        {
            return defaultValue;
        } else
        {
            return value;
        }
    }
    public static void exit()
    {
        System.out.println("Thao tác Exit....");
        System.exit(1);
    }

    public List<String> convertListBooleanToListString() {
        init();
        List<String> listDateConfig = new ArrayList<String>();
        List<Boolean> listDate = null;
        listDate = parseListDateBackup(datesConfig);
        for (int i = 0; i < listDate.size(); i++) {
            if (listDate.get(i) == true) {
                if (i == 6) {
                    listDateConfig.add("Sunday");
                } else {
                    listDateConfig.add(getDayOfWeek(i + 2));
                }
            }
        }
        return listDateConfig;
    }

    public String getDayOfWeek(int value) {
        String day = "";
        switch (value) {
            case 1:
                day = "Sunday";
                break;
            case 2:
                day = "Monday";
                break;
            case 3:
                day = "Tuesday";
                break;
            case 4:
                day = "Wednesday";
                break;
            case 5:
                day = "Thursday";
                break;
            case 6:
                day = "Friday";
                break;
            case 7:
                day = "Saturday";
                break;
        }
        return day;
    }

    public List<Boolean> parseListDateBackup(String key){
        List<Boolean> subkeys = new ArrayList<Boolean>();
        String[] date = key.split(" ");
        for(int i = 0;i<date.length ;i++){
            subkeys.add(Boolean.parseBoolean(date[i]));
        }
        while(subkeys.size() < 7){
            subkeys.add(Boolean.parseBoolean("false"));
            if(subkeys.size() == 7){
                break;
            }
        }
        return subkeys;
    }

    public static int countPage(int total) {
        int rowPerpage = 1000;
        int result = 0;
        result = total / rowPerpage;
        int temp = total % rowPerpage;
        if (temp > 0) {
            result = result + 1;
            return result;
        } else return result;
    }

    public static boolean validateDate(String date){
        boolean validateDate = false;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateInput = df.parse(date);
            if(dateInput.getTime() >0){
                validateDate = true;
            }
            else {
                logger.warning("Thời gian nhập hoặc cấu hình không đúng định dạng.Vui lòng nhập lại");
            }
        }
        catch (Exception ex){
            logger.warning("Thời gian nhập hoặc cấu hình không đúng định dạng.Vui lòng nhập lại");
        }
        return validateDate;
    }
    // Tạo mới fodler lưu trữu file back up khi chưa tồn tại
    public void createFolderBU(){
        init();
        File folder = new File(system_backup_database_folder);
        File folderBUAll = new File(system_backupAll_database_folder);
        File folderBUTable = new File(system_backupTable_database_folder);
        File folderData =  new File(data_directory);
        if(folder.exists()){
        }
        else {
            folder.mkdirs();
            if(!folderBUAll.exists()){
                folderBUAll.mkdirs();
            }
            else {
            }
            if(!folderBUTable.exists()){
                folderBUTable.mkdirs();
            }
            else{
            }
        }
        if(!folderData.exists()){
            folderData.mkdirs();
        }
    }
    // Kiểm tra trạng thái tiến trình chạy
    public long checkStatusProccess() throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * FROM information_schema.processlist ");
        stringBuilder.append("where info like 'SELECT /*!40001 SQL_NO_CACHE */%'");
        connection = DBPool.makeDBConnection();
        PreparedStatement ps = null;
        long result = -1;
        try {
            if (connection != null) {
                ps = connection.prepareStatement(String.valueOf(stringBuilder));
                ResultSet rs = null;
                rs = ps.executeQuery();
                rs.next();
                result = rs.getLong(1);
            } else {
                logger.warning("Không kết nối được đến database");
            }
        } catch (SQLException e) {
            result = -1;
        }
        finally {
            connection.close();
        }
        return result;
    }
    // Kiểm tra dung lượng file
    public long sizeFileSqlBackUp(String fileName,int type) {
        init();
        //find file backup
        File folder = new File("");
        if(type == 0){
            folder = new File(system_backupAll_database_folder);
        }
        else if(type == 1){
            folder = new File(system_backupTable_database_folder);
        }
        File[] files = folder.listFiles();
        long thisFileSize = 0;
        if(files.length > 0) {
            for (int i = files.length - 1; i >= 0; i--) {
                String nameFile = files[i].getName();
                if (nameFile.substring(nameFile.lastIndexOf(".")).equals(".sql") && nameFile.equals(fileName)) {
                    thisFileSize = files[i].length();
                }
            }
        }
        return thisFileSize;
    }

    // Tìm kiếm tất cả file backup trong thư mục lưu trữ
    public List<String> findFileSqlBackUp() {
        init();
        File folderBackUpAll = new File(system_backupAll_database_folder);
        File folderBackUpTable = new File(system_backupTable_database_folder);
        File[] filesBUAll = folderBackUpAll.listFiles();
        File[] filesBUTable = folderBackUpTable.listFiles();
        List<String> fileBackUp = new ArrayList<>();
        if(filesBUAll.length > 0){
            for (int i = filesBUAll.length - 1; i >= 0; i--) {
                String fileName = filesBUAll[i].getName();
                if (fileName.substring(fileName.lastIndexOf(".")).equals(".sql")) {
                    fileBackUp.add(fileName);
                }
            }
        }
        if(filesBUTable.length > 0) {
            for (int i = filesBUTable.length - 1; i >= 0; i--) {
                String fileName = filesBUTable[i].getName();
                if (fileName.substring(fileName.lastIndexOf(".")).equals(".sql")) {
                    fileBackUp.add(fileName);
                }
            }
        }
        return fileBackUp;
    }

    // Tìm kiếm tất cả file backupAll trong thư mục lưu trữ
    public List<String> findFileSqlBackUpAll() {
        init();
        File folderBackUpAll = new File(system_backupAll_database_folder);
        File[] filesBUAll = folderBackUpAll.listFiles();
        List<String> fileBackUp = new ArrayList<>();
        if(filesBUAll.length > 0){
            for (int i = filesBUAll.length - 1; i >= 0; i--) {
                String fileName = filesBUAll[i].getName();
                if (fileName.substring(fileName.lastIndexOf(".")).equals(".sql")) {
                    fileBackUp.add(fileName);
                }
            }
        }
        return fileBackUp;
    }

    // Tìm kiếm tất cả file backupTable trong thư mục lưu trữ
    public List<String> findFileSqlBackUpTable() {
        init();
        File folderBackUpAll = new File(system_backupTable_database_folder);
        File[] filesBUTable = folderBackUpAll.listFiles();
        List<String> fileBackUp = new ArrayList<>();
        if(filesBUTable.length > 0) {
            for (int i = filesBUTable.length - 1; i >= 0; i--) {
                String fileName = filesBUTable[i].getName();
                if (fileName.substring(fileName.lastIndexOf(".")).equals(".sql")) {
                    fileBackUp.add(fileName);
                }
            }
        }
        return fileBackUp;
    }

    // Convert to ArrayList<String> to String[]
    public String[] getStringArray(ArrayList<String> arr)
    {
        String str[] = new String[arr.size()];
        for (int j = 0; j < arr.size(); j++) {
            str[j] = arr.get(j);
        }
        return str;
    }

    // Convert to ArrayList<Long> to Long[]
    public Long[] getLonggArray(ArrayList<Long> arr)
    {
        Long str[] = new Long[arr.size()];
        for (int j = 0; j < arr.size(); j++) {
            str[j] = arr.get(j);
        }
        return str;
    }

}
