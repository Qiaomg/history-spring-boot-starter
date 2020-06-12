package net.shopin.grpc;

/**
 * @title: GrpcMsg
 * @description: rpc 之间传递消息的格式
 * @author: qmg
 * @date: 2020/6/11 19:52
 * @version: V1.0
 */
public class GrpcMsg {
    /**
     * 操作类型 使用GrpcTypeEnum
     */
    private GrpcTypeEnum type;
    /**
     * 执行sql
     */
    private String optSql;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 服务
     */
    private String serverName;

    public GrpcTypeEnum getType() {
        return type;
    }

    public void setType(GrpcTypeEnum type) {
        this.type = type;
    }

    public String getOptSql() {
        return optSql;
    }

    public void setOptSql(String optSql) {
        this.optSql = optSql;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String toString() {
        return "GrpcMsg{" +
                "type=" + type +
                ", optSql='" + optSql + '\'' +
                ", tableName='" + tableName + '\'' +
                ", serverName='" + serverName + '\'' +
                '}';
    }

    public String toJsonString(){
        return "{\"type\":\"" + type + "\",\"optSql\":\"" + optSql + "\",\"tableName\":\"" + tableName + "\", \"serverName\":\"" + serverName + "\"}";
    }
}
