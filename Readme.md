配置springboot的动态数据源.

Target
1. 最小化配置
2. 支持注解和代码手动两种方式切换
3. 适配mybatis hibernate jpa等热门数据库框架
4. 支持多数据源之间的事务 **

使用：  
需要去除  

## 使用
引入：
```xml
<dependency>
    <groupId>com.spring.dynamic.datasource</groupId>
    <artifactId>datasource-dynamic-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 功能
本starter可以自动配置一个DynamicDatasource，来实现动态数据源管理。  
使用本starter时，按照正常逻辑创建各个Datasource的bean，但是不要给任意数据源加上@Primary注解，因为DynamicDatasource会作为基础数据源配置，配置两个primary会有冲突。同时约定masterDataSource为默认数据源的名称，如果要更改默认数据源名称，请修改如下配置：
```properties
spring.dynamic.datasource.defaultDatasource=masterDataSource
```

使用`com.spring.dynamic.datasource.annotation.CurDatasource`注解，在方法头部声明该方法所使用的数据源，例如： 
```java
@Service
public class TestService {
    @Autowired
    private SpringJdbcTemplate jdbcTemplate;
    @CurDatasource("masterDataSource")
    public void testMaster() {
        String sql = "select * from space_community_info;";
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        System.out.println(maps);
    }
    @CurDatasource("otherDataSource")
    public void testOtherDataSource() {
        String sql = "select * from sys_system_properties";
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        System.out.println(maps);
    }
}
```
value和你自己注册的各个Datasource名称一致。如果需要显式配置，使用如下类似配置  
```properties
spring.dynamic.datasource.datasourceList[0]=masterDataSource
spring.dynamic.datasource.datasourceList[1]=otherDataSource
```
也可以通过手动方式动态切换，使用com.spring.dynamic.datasource.config.DynamicDataSourceContextHolder，例子如下：
```java
@Service
public class TestService {
    public void test() {
        DynamicDataSourceContextHolder.setDataSource("masterDataSource");
        // do your operation
        DynamicDataSourceContextHolder.clearDataSource();
    }
}
```
需要注意的是需要手动clear来清除进入的数据源痕迹来回到原数据源。否则在同时使用注解和手动方式时，会出现异常

同时，本starter为每个数据源配置一个事务管理器即DataSourceTransactionManager，每个事务管理器名称为数据源名称加上Manager后缀，如果要自定义后缀修改如下配置：
```properties
spring.dynamic.datasource.transactionManagerSuffix=Manager
```

如果要暂时关闭功能，开关配置为：
```properties
spring.dynamic.datasource.enable=true
```

## 问题阐述
### 切换问题
需要了解的是，spring在获取到数据源之后，再次进行数据库操作时，会判断是否再去新建一个connect，那么也就意味着在数据嵌套的时候，可能会出现数据源不切换的现象，这是因为spring觉得不需要新用一个connect。所以在需要嵌套的场景下，显示告诉spring事务传播方式采用REQUIRES_NEW即再开启一个事务，例子如下：
```java
class Test{
    @Transactional(propagation = Propagation.REQUIRED)
    public void testMain(){
        A(a1);  //调用A入参a1
        testB();    //调用testB
        throw Exception;     //发生异常抛出
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testB(){
        B(b1);  //调用B入参b1
        B(b2);  //调用B入参b2
    }
}
```
###多数据源事务
`DataSourceTransactionManager`是spring用来管理事务的管理器。  
阅读其源码，就可以看到，在类注解的第一行有说明：
```text
org.springframework.transaction.PlatformTransactionManager implementation for a single JDBC DataSource. 
```
你可能有疑惑这个说的是PlatformTransactionManager，和我们这个不是一个类呀。  
PlatformTransactionManager实际上是DataSourceTransactionManager继承的父类的父类。  

这意味着什么？？
意味着，当我们愉快的动态切换数据源时需要注意，我们不能在一个方法中去操作两个数据源的同时，保证这两个数据源的事务一致！！！！

这需要手动处理！！！

如果是简单业务，我们可以通过不同的顺序来达到需求。  
如果是复杂的业务场景要求一定要事务一致，那么我们需要引入分布式事务管理器，可以考虑Atomikos。

如果就是单应用，数据源在应用中，事务管理也在应用中，应用能够获取到所有的资料，却不能实现事务同步，是怪怪的。有没有方法解决？思考中


