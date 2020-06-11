package net.shopin.utils;

import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import net.shopin.grpc.GRpcClient;
import net.shopin.history.annotation.History;
import net.shopin.history.entity.SqlConvertDto;
import net.shopin.history.properties.HistoryProperties;
import org.reflections.Reflections;
import org.springframework.util.StringUtils;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 * @title: HistoryRecordUtils
 * @description: bean后置处理器
 * @author: qmg
 * @date: 2019/12/14 12:15
 * @version: V1.0
 */
public class HistoryRecordUtils {
    private static final Logger logger = Logger.getLogger(HistoryRecordUtils.class.getName());
    private static final String DATE_PATTON_3 = "yyyy-MM-dd HH:mm:ss";
    private static Map<String, String> ENTITY_MAP = new ConcurrentHashMap<String, String>();

    public static Map<String, String> init() {
        if (ENTITY_MAP != null && ENTITY_MAP.size() > 0) {
            return ENTITY_MAP;
        }
        //指明扫描路径
        Reflections reflections = new Reflections(HistoryProperties.getEntityPath());
        //获取带Handler注解的类
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
    public static void saveHistory(String creater,Statement stmt, String sql, String type) {
        try {
            sql = sql.replaceAll("[\\s]+", " ");
            SqlConvertDto sqlConvertDto;

            if (HistoryProperties.INSERT.equals(type)) {
                sqlConvertDto = HistoryRecordUtils.splitSqlInsert(sql);
            } else if (HistoryProperties.DELETE.equals(type)) {
                sqlConvertDto = HistoryRecordUtils.splitSqlDelete(sql);
            } else if (HistoryProperties.UPDATE.equals(type)) {
                sqlConvertDto = HistoryRecordUtils.splitSqlUpdate(sql);
            } else {
                return;
            }
            if(sqlConvertDto.getTableName() == null){
                return;
            }
            sqlConvertDto.setCreater(creater);
            HistoryRecordUtils.selectInfoToHistory(stmt, sqlConvertDto);
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

        sqlDto.setType(HistoryProperties.INSERT);
        sqlDto.setTableName(insert.getTableName().getSimpleName());
        sqlDto.setHistoryTableName(HistoryRecordUtils.getBtTableName(sqlDto.getTableName()));
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

        sqlDto.setType(HistoryProperties.UPDATE);
        sqlDto.setTableName(update.getTableName().getSimpleName());
        sqlDto.setHistoryTableName(HistoryRecordUtils.getBtTableName(sqlDto.getTableName()));
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

        sqlDto.setType(HistoryProperties.UPDATE);
        sqlDto.setTableName(delete.getTableName().getSimpleName());
        sqlDto.setHistoryTableName(HistoryRecordUtils.getBtTableName(sqlDto.getTableName()));
        sqlDto.setWhere(delete.getWhere().toString());
        return sqlDto;
    }

    /**
     * 查询和保存数据
     * @param stmt
     * @param sqlConvertDto
     */
    public static void selectInfoToHistory(Statement stmt,SqlConvertDto sqlConvertDto)throws Throwable  {


        // 配置druid连接时使用filters: stat配置
        if (stmt instanceof PreparedStatementProxyImpl) {
            stmt = ((PreparedStatementProxyImpl) stmt).getRawObject();
        }

        if(sqlConvertDto == null || StringUtils.isEmpty(sqlConvertDto.getTableName())  || StringUtils.isEmpty(sqlConvertDto.getHistoryTableName())|| StringUtils.isEmpty(sqlConvertDto.getType())){
            System.out.println("sqlConvertDto 空");
            return;
        }

        //-----------select 查询sql
        StringBuilder whereStr  = new StringBuilder(" 1=1 ");
        if(HistoryProperties.INSERT.equals(sqlConvertDto.getType())){
            for (int i = 0; i < sqlConvertDto.getColumnKey().length; i++) {
                if(sqlConvertDto.getColumnKey()[i] == null){
                    break;
                }
                whereStr.append(" AND ");
                whereStr.append(sqlConvertDto.getColumnKey()[i]);
                whereStr.append("=");
                whereStr.append(sqlConvertDto.getColumnVal()[i]);
            }
        }else if(HistoryProperties.UPDATE.equals(sqlConvertDto.getType())){
            whereStr.append(" AND ");
            whereStr.append(sqlConvertDto.getWhere());
        }else if(HistoryProperties.DELETE.equals(sqlConvertDto.getType())){
            whereStr.append(" AND ");
            whereStr.append(sqlConvertDto.getWhere());
        }

        StringBuilder selectSqlStr =new StringBuilder(600);
        selectSqlStr.append("SELECT * FROM ");
        selectSqlStr.append(sqlConvertDto.getTableName());
        selectSqlStr.append(" WHERE ");
        selectSqlStr.append(whereStr);
        String selectSql =selectSqlStr.toString().replaceAll("[\\s]+", " ");
        System.out.println("拦截器执行【"+ selectSql+"】");
//        log.info("拦截器执行【{}】",selectSql);
        //获取操作结果集
        Map<String,Object> rsmap = new HashMap<String,Object>();
        Connection conn = stmt.getConnection();
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            ps = conn.prepareStatement(selectSql);
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
//            log.info("sql执行未改变任何值");
            System.out.println("sql执行未改变任何值");
            return;
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
        valStr.append("'" + sqlConvertDto.getCreater() + "','" + getDBDate() + "','" + sqlConvertDto.getType() + "'");
        insertSqlStr.append("INSERT INTO ");
        insertSqlStr.append(sqlConvertDto.getHistoryTableName());
        insertSqlStr.append("(" + keyStr.toString() + ")");
        insertSqlStr.append(" VALUES ");
        insertSqlStr.append("(" + valStr.toString() + ") ");
        String insertSql =insertSqlStr.toString().replaceAll("[\\s]+", " ");
        logger.info("拦截器执行【"+insertSql+"】");
        //保存数据
        Connection conn1 = stmt.getConnection();

        PreparedStatement ps1 = null;
        try {
            ps1 = conn1.prepareStatement(insertSql);
            ps1.execute();
        }finally {
            if (ps1 != null) {
                ps1.close();
            }
        }

        sendRpcToServer(insertSql);
    }

    public static void sendRpcToServer(String insertSql){
        logger.info("rpc client: "+insertSql);
        GRpcClient.getInstance().greet(insertSql);
    }

    /**
     * 当前日期 格式 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getDBDate() {
        SimpleDateFormat SDF_PATTON_3= new SimpleDateFormat(DATE_PATTON_3);
        synchronized (SDF_PATTON_3){
            return SDF_PATTON_3.format(new Date());
        }
    }

//    public static void main(String[] args) {
//        GRpcClient client = GRpcClient.getInstance();;
//        try {
//            client.greet("123");
//        }  finally {
//            try {
//                client.shutdown();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//    }
}
