package net.shopin.grpc;

/**
 * @title: GrpcMsg
 * @description: rpc 之间传递消息的格式
 * @author: qmg
 * @date: 2020/6/11 19:52
 * @version: V1.0
 */
public class GrpcMsg {
    private String optSql;
    private String tableName;
    private String serverName;

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
                "optSql='" + optSql + '\'' +
                ", tableName='" + tableName + '\'' +
                ", serverName='" + serverName + '\'' +
                '}';
    }

    public String toJsonString(){
        return "{\"optSql\":\"" + optSql + "\",\"tableName\":\"" + tableName + "\", \"serverName\":\"" + serverName + "\"}";
    }
}
