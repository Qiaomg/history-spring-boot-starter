package net.shopin.history.interceptors;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import net.shopin.context.Common;
import net.shopin.history.HistorySender;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Statement;
import java.util.Properties;

import static net.shopin.context.Common.DELETE;


/**
 * @Title: HistoryTableInterceptor
 * @Description: 使用拦截器实现 自动存储history功能
 * 当前版本不通用
 * @author: qmg
 * @date: 2019/8/13 9:36
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "update", args = Statement.class)})
public class HistoryTableInterceptor implements Interceptor {
    private volatile static HistoryTableInterceptor instance;

    private HistoryTableInterceptor() {
    }

    /**
     * 单例 双检锁模式
     *
     * @return HistoryTableInterceptor
     */
    public static HistoryTableInterceptor getInstance() {
        if (instance == null) {
            synchronized (HistoryTableInterceptor.class) {
                if (instance == null) {
                    instance = new HistoryTableInterceptor();
                }
            }
        }
        return instance;
    }

    /**
     * 只记录 update操作和 delete操作。
     * 对于select 操作 无需记录history
     * 对于insert 操作 初始创建 不重复记录
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object obj;
        Statement stmt;
        String sql;
        String sqlType;
        Statement st = (Statement) invocation.getArgs()[0];
        try {
            if (st instanceof DruidPooledPreparedStatement) {
                stmt = ((DruidPooledPreparedStatement) st).getStatement();
                // 配置druid连接时使用filters: stat配置
                if (stmt instanceof PreparedStatementProxyImpl) {
                    stmt = ((PreparedStatementProxyImpl) stmt).getRawObject();
                }
                sql = ((com.mysql.cj.jdbc.ClientPreparedStatement) stmt).asSql().replaceAll("\\s+", " ");
                sqlType = sql.substring(0, 6).toUpperCase();
                if (Common.UPDATE.equals(sqlType) || DELETE.equals(sqlType)) {
                    HistorySender.analysisSql("system", stmt, sql, sqlType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            obj = invocation.proceed();
            return obj;
        }
        //如果要记录insert操作 使用下方代码段
        /*
       try {
            if(st instanceof DruidPooledPreparedStatement) {
                if (HistoryProperties.INSERT.equals(sqlType)) {
                    analysisSql("system", stmt, sql, sqlType);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            return obj;
        }*/

    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
