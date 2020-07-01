package net.shopin.history;


import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import net.shopin.context.Common;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @title: SqlConvertDto
 * @description: TODO(用一句话描述该文件做什么)
 * @author: qmg
 * @date: 2019/12/19 13:49
 * @version: V1.0
 */
public class SqlConvertDto {
    /**
     * 操作类型 insert  update delete
     */
    private String type;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 历史表表名
     */
    private String historyTableName;
    /**
     * 条件
     */
    private String where;
    /**
     * 列
     */
    private String[] columnKey;
    /**
     * 值
     */
    private String[] columnVal;
    /**
     * 操作人
     */
    private String creater;

    private String fullSql;
    private String logSql;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getHistoryTableName() {
        return historyTableName;
    }

    public void setHistoryTableName(String historyTableName) {
        this.historyTableName = historyTableName;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String[] getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String[] columnKey) {
        this.columnKey = columnKey;
    }

    public String[] getColumnVal() {
        return columnVal;
    }

    public void setColumnVal(String[] columnVal) {
        this.columnVal = columnVal;
    }

    public String getCreater() {
        return creater;
    }

    public void setCreater(String creater) {
        this.creater = creater;
    }

    public String getFullSql() {
        if(!StringUtils.isEmpty(fullSql)){
            return fullSql;
        }
        if( StringUtils.isEmpty(tableName)  || StringUtils.isEmpty(historyTableName)|| StringUtils.isEmpty(type)){
            return null;
        }
        // select 查询sql
        StringBuilder whereStr  = new StringBuilder();
        boolean flag = true;
        if(Common.INSERT.equals(type)){
            for (int i = 0; i < columnKey.length; i++) {
                if(columnKey[i] == null){
                    break;
                }
                if(flag){
                    whereStr.append(" WHERE ");
                    flag = false;
                }
                whereStr.append(columnKey[i]);
                whereStr.append("=");
                whereStr.append(columnVal[i]);
            }
        }else if(Common.UPDATE.equals(type)){
            whereStr.append(" WHERE ");
            whereStr.append(where);
        }else if(Common.DELETE.equals(type)){
            whereStr.append(" WHERE ");
            whereStr.append(where);
        }

        StringBuilder selectSqlStr =new StringBuilder(600);
        selectSqlStr.append("SELECT * FROM ");
        selectSqlStr.append(tableName);
        selectSqlStr.append(whereStr);
        fullSql = selectSqlStr.toString().replaceAll("[\\s]+", " ");
        return fullSql;
    }

    public String getLogSql(){
        return logSql;
    }

    public String createLogSql(Statement stmt)throws Exception {
        if(logSql != null){
            return logSql;
        }
        // 配置druid连接时使用filters: stat配置
        if (stmt instanceof PreparedStatementProxyImpl) {
            stmt = ((PreparedStatementProxyImpl) stmt).getRawObject();
        }
        //获取操作结果集
        Map<String,Object> rsmap = new HashMap<String,Object>();
        Connection conn = stmt.getConnection();
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            ps = conn.prepareStatement(getFullSql());
            rs=ps.executeQuery();
            while(rs.next()){
                ResultSetMetaData rsmd = rs.getMetaData();
                for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                    rsmap.put(rsmd.getColumnName(j),rs.getObject(j));
                }
            }
        }finally {
            if (ps != null) {
                ps.close();
            }
        }

        if(rsmap.size() <= 0){
            return null;
        }
        //-----------insert 插入sql
        StringBuilder insertSqlStr = new StringBuilder(600);
        StringBuilder keyStr =new StringBuilder();
        StringBuilder valStr =new StringBuilder();
        Set<String> keySet = rsmap.keySet();
        for (String key : keySet) {
            if(rsmap.get(key) !=null && !"null".equals(rsmap.get(key))){
                keyStr.append(key);
                keyStr.append(",");
                valStr.append("\'" + rsmap.get(key)+"\',");
            }
        }
        keyStr.append("opt_user,opt_time,opt_type");
        valStr.append("'" + creater + "','" + getDateTime() + "','" + type + "'");
        insertSqlStr.append("INSERT INTO ");
        insertSqlStr.append(historyTableName);
        insertSqlStr.append("(" + keyStr.toString() + ")");
        insertSqlStr.append(" VALUES ");
        insertSqlStr.append("(" + valStr.toString() + ") ");
        logSql = insertSqlStr.toString().replaceAll("[\\s]+", " ");
        return logSql;
    }



    @Override
    public String toString() {
        return "SqlConvertDto{" +
                "type='" + type + '\'' +
                ", tableName='" + tableName + '\'' +
                ", historyTableName='" + historyTableName + '\'' +
                ", where='" + where + '\'' +
                ", columnKey=" + Arrays.toString(columnKey) +
                ", columnVal=" + Arrays.toString(columnVal) +
                ", creater='" + creater + '\'' +
                ", fullSql='" + fullSql + '\'' +
                ", logSql='" + logSql + '\'' +
                '}';
    }

    /**
     * 当前日期 格式 yyyy-MM-dd HH:mm:ss
     * @return
     */
    private static String getDateTime() {
        SimpleDateFormat SDF_PATTON_3= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        synchronized (SDF_PATTON_3){
            return SDF_PATTON_3.format(new Date());
        }
    }
}
