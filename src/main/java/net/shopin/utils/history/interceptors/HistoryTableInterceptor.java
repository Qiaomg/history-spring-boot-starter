package net.shopin.utils.history.interceptors;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import net.shopin.utils.history.HistoryRecordUtils;
import net.shopin.utils.history.HistoryProperties;
import net.shopin.utils.history.SqlConvertDto;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Statement;
import java.util.Properties;



/**
 * @Title: HistoryTableInterceptor
 * @Description: 使用拦截器实现 自动存储history功能
 * 当前版本不通用
 * @author: qmg
 * @date: 2019/8/13 9:36
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "update", args = Statement.class)})
public class HistoryTableInterceptor implements Interceptor {
    private volatile static HistoryTableInterceptor instance;
    private HistoryTableInterceptor(){}
    /**
     * 单例 双检锁模式
     * @return HistoryTableInterceptor
     */
    public static HistoryTableInterceptor getInstance(){
        if(instance == null){
            synchronized (HistoryTableInterceptor.class){
                if(instance == null){
                    System.out.println("init HistoryTableInterceptor");
                    instance = new HistoryTableInterceptor();
                }
            }
        }
        return instance;
    }

    /**
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object obj;
//        long startTime = System.currentTimeMillis();
        Statement stmt=null;
        String sql="";
        String sqlType="";
        Statement st = (Statement) invocation.getArgs()[0];
        try {
            if(st instanceof DruidPooledPreparedStatement){
                stmt = ((DruidPooledPreparedStatement) st).getStatement();
                // 配置druid连接时使用filters: stat配置
                if (stmt instanceof PreparedStatementProxyImpl) {
                    stmt = ((PreparedStatementProxyImpl) stmt).getRawObject();
                }
                sql = ((com.mysql.cj.jdbc.ClientPreparedStatement) stmt).asSql().replaceAll("\\s+", " ");
                sqlType = sql.substring(0, 6).toUpperCase();

                System.out.println("【执行SQL】:" + sql);
//                log.info("【执行SQL】:" + sql);

                if (HistoryProperties.UPDATE.equals(sqlType) || HistoryProperties.DELETE.equals(sqlType)) {
                    saveHistory( "system" ,stmt, sql, sqlType);
                }
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            obj = invocation.proceed();
        }
        try {
            if(st instanceof DruidPooledPreparedStatement) {
                if (HistoryProperties.INSERT.equals(sqlType)) {
                    saveHistory("system", stmt, sql, sqlType);
                }
//                long endTime = System.currentTimeMillis();
//                log.info("拦截器 耗时：{}", (endTime - startTime));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            return obj;
        }

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
//        long startTime = System.currentTimeMillis();
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
//                log.info("执行类型为{}的sql,不处理，", type);
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
//        long endTime = System.currentTimeMillis();
//        log.info("saveHistory 耗时：{}", (endTime - startTime));
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

//        log.info("setProperties");
    }

}
