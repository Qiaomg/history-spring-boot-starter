# history-spring-boot-starter

### 1、目的
    数据库表中部分数据变更要求有历史及变更记录,便于数据追溯,对于此类场景不必在业务中先记录再操作.

#### 自定义spring-boot-starter（可插拔式组件）.
    基于拦截器和Sql反解析实现

#### 当前版本0.0.1版本.
    兼容计划
    - [x] springboot + mybatis plus + mysql 项目
    - [ ] springboot + mybatis + mysql 项目

### 2、使用  可参考history-client-demo项目配置
#### 添加pom
```XML
<dependency>
    <groupId>net.shopin</groupId>
    <artifactId>history-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency> 
```   
 
#### 添加mybatis plus 拦截器
```JAVA
    @Configuration
    @MapperScan(value = "net.shopin.client.mapper",sqlSessionFactoryRef = "SqlSessionFactory")
    public class DataSourceConfig {
        @Autowired
        private HistoryTableInterceptor historyTableInterceptor;
        
        @Bean("SqlSessionFactory")
        public SqlSessionFactory couponcoreSqlSessionFactory(@Qualifier("DataSource") DataSource dataSource ) throws Exception {
            MybatisSqlSessionFactoryBean mybatisFactoryBean =new MybatisSqlSessionFactoryBean();
            mybatisFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/**/*.xml"));
            mybatisFactoryBean.setDataSource(dataSource);
            Interceptor[] plugins = {myBatisPlusConfig.paginationInterceptor(),historyTableInterceptor};
            mybatisFactoryBean.setPlugins(plugins);
            return mybatisFactoryBean.getObject();
        }
    ....//省略代码
    }
```
#### 添加实体类注解
```JAVA
    @History("market_activity_history")
    public class Activity{
    ...//省略代码
    }
```
