配置springboot的动态数据源.

Target
1. 最小化配置
2. 支持注解和代码手动两种方式切换
3. 适配mybatis hibernate jpa等热门数据库框架

使用：  
需要去除  
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
