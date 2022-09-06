package com.osp.npo.task;

import com.osp.npo.common.util.Util;
import com.osp.npo.common.util.database.DBPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by QUYENLC on 12/07/2021.
 */
public class BackUpFileDataCallable implements Callable {
    private Connection connection;
    private String startTime;
    private String endTime;
    private int partition;
    private static BackUpFileDataCallable instance;
    private static final Logger logger = Logger.getLogger(BackUpFileDataCallable.class.getName());

    public BackUpFileDataCallable(int partition, String startTime , String endTime) {
        this.partition = partition;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public Boolean call() throws Exception {
        connection = DBPool.makeDBConnection();
        int dataInPartition = countData(partition);
        int dataInPartitionWithTime = countDataInPartition(partition,startTime,endTime);
        int countPartitionSucess = 0;
        int numberPage = Util.countPage(dataInPartition);
        for (int i=1;i<=numberPage;i++){
            connection = DBPool.makeDBConnection();
            int count = selectDataFileByTime(partition,i,startTime,endTime);
            countPartitionSucess += count;
            if(countPartitionSucess == dataInPartitionWithTime){
                logger.info("Back up dữ liệu Partition("+partition+") thành công với "+dataInPartition+" bản ghi");
            }
        }
        return true;
    }

    private int countData(int partition) throws SQLException {
        int result = 0;
        StringBuilder str = new StringBuilder();
        str.append("SELECT COUNT(file_data.file_id) FROM phpviet_file_data PARTITION(p"+partition+") file_data ");
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (connection != null) {
                ps = connection.prepareStatement(String.valueOf(str));
                rs = ps.executeQuery();
                rs.next();
                result = rs.getInt(1);
            } else {
                logger.warning("Không kết nối được đến database");
            }
        } catch (SQLException e) {
            result = 0;
        }
        return result;
    }

    private int selectDataFileByTime(int partitionNumber, int page, String startTime , String endTime) throws SQLException {
        int rowPerPage = 1000;
        int offset = rowPerPage * (page - 1);
        StringBuilder str = new StringBuilder();
        str.append("INSERT INTO phpviet_file_data_temp ");
        str.append("SELECT DISTINCT file_data.file_id,file_data.partition_id,file_data.data,file_data.html FROM phpviet_file_data PARTITION(p"+partitionNumber+") file_data ");
        str.append("INNER JOIN phpviet_file file ");
        str.append("ON file_data.file_id = file.file_id ");
        str.append("WHERE file.upload_date >= '" + startTime + "' ");
        str.append("AND file.upload_date <= '" + endTime + "' ");
        str.append("LIMIT " + offset + "," + rowPerPage + "");
        PreparedStatement ps = null;
        int result = 0;
        try{
            if (connection != null) {
                ps = connection.prepareStatement(String.valueOf(str));
                result = ps.executeUpdate();
            } else {
                logger.warning("Không kết nối được đến database");
            }
        }
        catch (SQLException e) {
            result = 0;
            logger.warning("Có lỗi xảy ra:"+e);
            e.printStackTrace();
        }
        finally {
            connection.close();
        }
        return result;
    }

    private int countDataInPartition(int partitionNumber , String startTime , String endTime) throws SQLException {
        StringBuilder str = new StringBuilder();
        str.append("SELECT COUNT(DISTINCT file_data.file_id) FROM phpviet_file_data PARTITION(p"+partitionNumber+") file_data ");
        str.append("INNER JOIN phpviet_file file ");
        str.append("ON file_data.file_id = file.file_id ");
        str.append("WHERE file.upload_date >= '" + startTime + "' ");
        str.append("AND file.upload_date <= '" + endTime + "' ");
        PreparedStatement ps = null;
        ResultSet rs = null;
        int result = 0;
        try{
            if (connection != null) {
                ps = connection.prepareStatement(String.valueOf(str));
                rs = ps.executeQuery();
                rs.next();
                result = rs.getInt(1);
            } else {
                logger.warning("Không kết nối được đến database");
            }
        }
        catch (SQLException e) {
            result = 0;
            logger.warning("Có lỗi xảy ra:"+e);
            e.printStackTrace();
        }
        finally {
            connection.close();
        }
        return result;
    }

    public static synchronized BackUpFileDataCallable getInstance(int partition, String startTime, String endTime) {
        if (instance == null) {
            instance = new BackUpFileDataCallable(partition, startTime, endTime);
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public static BackUpFileDataCallable getInstance() {
        return instance;
    }

    public static void setInstance(BackUpFileDataCallable instance) {
        BackUpFileDataCallable.instance = instance;
    }

    public static Logger getLogger() {
        return logger;
    }
}
