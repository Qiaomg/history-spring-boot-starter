package net.shopin.history;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import net.shopin.context.Common;
import net.shopin.grpc.GRpcClient;
import net.shopin.grpc.GrpcEnum;
import net.shopin.grpc.GrpcMsg;
import net.shopin.history.annotation.History;
import net.shopin.history.properties.HistoryProperties;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static net.shopin.context.HistoryClientContext.executorService;


/**
 * @title: HistorySender
 * @description: bean后置处理器
 * @author: qmg
 * @date: 2019/12/14 12:15
 * @version: V1.0
 */
@Component
public class HistorySender {
    private static final Logger logger = Logger.getLogger(HistorySender.class.getName());
    private static Map<String, String> ENTITY_MAP = new ConcurrentHashMap<String, String>();


    public static Map<String, String> init() {
        if (ENTITY_MAP != null && ENTITY_MAP.size() > 0) {
            return ENTITY_MAP;
        }
        // 指明扫描路径HistoryProperties.getEntityPath()
        Reflections reflections = new Reflections(HistoryProperties.entityPath);
        // 获取带 History 注解的类
        Set<Class<?>> classList = reflections.getTypesAnnotatedWith(History.class);
        for (Class classes : classList) {
            History historyTableName = (History) classes.getAnnotation(History.class);
            String tableName = historyTableName.value().substring(0,historyTableName.value().indexOf("_history"));
            //数据库    表名key，历史表表名value
            ENTITY_MAP.put(tableName, historyTableName.value());
        }
        return ENTITY_MAP;
    }

    public static String getBtTableName(String key) {
        if(ENTITY_MAP == null || ENTITY_MAP.size()==0){
            init();
        }
        return ENTITY_MAP.get(key);
    }





    /**
     * @param sqlConvertDto
     */
    public static void execute(SqlConvertDto sqlConvertDto){
        executorService.submit(() -> {
            GrpcMsg msg = new GrpcMsg();
            msg.setType(GrpcEnum.INSERT);
            msg.setOptSql(sqlConvertDto.getLogSql());
            msg.setTableName(sqlConvertDto.getHistoryTableName());
            msg.setServerName(HistoryProperties.serverName);
            logger.info("rpc client: " + msg.toString());
            GRpcClient.getInstance().greet(msg.toJsonString());
        });
    }



    /**
     * 业务代码
     * 1、判断操作类型
     * insert 解析全部sql          ->  insertHistory
     * delete 解析where条件部分    ->  根据条件查询数据 insertHistory
     * update 解析where条件部分    ->  根据条件查询数据 insertHistory
     * 2、解析SQL
     * 获得sql操作的数据库表
     * 获取数据库表对应的实体类
     * 获得实体类上的注解 @History
     * SqlCommandType sqlType
     */
    public static void analysisSql(String creater, Statement stmt, String sql, String type) {
        try {

            sql = sql.replaceAll("[\\s]+", " ");
            SqlConvertDto sqlConvertDto;

            if (Common.INSERT.equals(type)) {
                sqlConvertDto = HistorySender.splitSqlInsert(sql);
            } else if (Common.DELETE.equals(type)) {
                sqlConvertDto = HistorySender.splitSqlDelete(sql);
            } else if (Common.UPDATE.equals(type)) {
                sqlConvertDto = HistorySender.splitSqlUpdate(sql);
            } else {
                return;
            }
            if(sqlConvertDto.getTableName() == null){
                return;
            }
            sqlConvertDto.setCreater(creater);
            sqlConvertDto.createLogSql(stmt);
            execute(sqlConvertDto);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    /**
     * 解析insert sql
     * TODO mybatis执行的sql有这种情况吗?  自定义insert sql 暂不考虑(既然使用了mybatis/plus 不需要手写insert sql)!
     * INSERT INTO table_name VALUES (value1,value2,value3,...);
     * INSERT INTO table_name (column1,column2,column3,...) VALUES (value1,value2,value3,...);
     * #f3d2#table_name (column1,column2,column3,...)#f3d2#(value1,value2,value3,...)
     * @param tmpSql
     * @return
     */
    public static SqlConvertDto splitSqlInsert(String tmpSql){
        SqlConvertDto sqlDto = new SqlConvertDto();
        //解析sql
        MySqlStatementParser parser = new MySqlStatementParser(tmpSql);
        SQLStatement statement = parser.parseStatement();
        MySqlInsertStatement insert = (MySqlInsertStatement)statement;

        String[] columnKey = new String[50];
        String[] columnVal = new String[50];
        for (int i = 0; i < insert.getColumns().size(); i++) {
            columnKey[i] =insert.getColumns().get(i).toString();
            columnVal[i] =insert.getValues().getValues().get(i).toString();
        }

        sqlDto.setType(Common.INSERT);
        sqlDto.setTableName(insert.getTableName().getSimpleName());
        sqlDto.setHistoryTableName(HistorySender.getBtTableName(sqlDto.getTableName()));
        sqlDto.setColumnKey(columnKey);
        sqlDto.setColumnVal(columnVal);
        return sqlDto;
    }


    /**
     * UPDATE table_name SET column1=value1,column2=value2,... WHERE some_column=some_value;
     * 解析 updatesql
     * @param tmpSql
     * @return
     */
    public static SqlConvertDto splitSqlUpdate(String tmpSql){
        SqlConvertDto sqlDto = new SqlConvertDto();
        //解析sql
        MySqlStatementParser parser = new MySqlStatementParser(tmpSql);
        SQLStatement statement = parser.parseStatement();
        MySqlUpdateStatement update = (MySqlUpdateStatement)statement;

        String[] columnKey =new String[50];
        String[] columnVal =new String[50];
        for (int i = 0; i < update.getItems().size(); i++) {
            columnKey[i] = update.getItems().get(i).getColumn().toString();
            columnVal[i] = update.getItems().get(i).getValue().toString();
        }

        sqlDto.setType(Common.UPDATE);
        sqlDto.setTableName(update.getTableName().getSimpleName());
        sqlDto.setHistoryTableName(HistorySender.getBtTableName(sqlDto.getTableName()));
        sqlDto.setWhere(update.getWhere().toString());
        sqlDto.setColumnKey(columnKey);
        sqlDto.setColumnVal(columnVal);
        return sqlDto;
    }

    /**
     * DELETE FROM table_name WHERE some_column=some_value AND column1=...;
     * 解析delete sql
     * @param tmpSql
     * @return
     */
    public static SqlConvertDto splitSqlDelete(String tmpSql){
        SqlConvertDto sqlDto = new SqlConvertDto();
        //解析sql
        MySqlStatementParser parser = new MySqlStatementParser(tmpSql);
        SQLStatement statement = parser.parseStatement();
        MySqlDeleteStatement delete = (MySqlDeleteStatement)statement;

        sqlDto.setType(Common.UPDATE);
        sqlDto.setTableName(delete.getTableName().getSimpleName());
        sqlDto.setHistoryTableName(HistorySender.getBtTableName(sqlDto.getTableName()));
        sqlDto.setWhere(delete.getWhere().toString());
        return sqlDto;
    }


}
