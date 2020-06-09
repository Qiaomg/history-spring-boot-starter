package net.shopin.utils.history;


import java.util.Arrays;

/**
 * @title: SqlConvertDto
 * @description: TODO(用一句话描述该文件做什么)
 * @author: qmg
 * @date: 2019/12/19 13:49
 * @version: V1.0
 */
public class SqlConvertDto {
    private String type;
    private String tableName;
    private String historyTableName;
    private String where;
    private String[] columnKey;
    private String[] columnVal;
    private String creater;

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
                '}';
    }
}
