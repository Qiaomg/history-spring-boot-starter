# history-spring-boot-starter
1、目的
 数据库表中部分数据变更要求有历史及变更记录,便于数据追溯,对于此类场景不必在业务中先记录再操作.

### 自定义spring-boot-starter（可插拔式组件）.
   基于拦截器和Sql反解析实现

### 当前版本0.0.1版本.
  目前支持 springboot + mybatis plus + mysql 项目的使用.

2、使用
### 添加pom
 <dependency>
  <groupId>net.shopin</groupId>
  <artifactId>history-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
 </dependency>    
 
### 添加mybatis plus 拦截器
 @Autowired
 private HistoryTableInterceptor historyTableInterceptor;
