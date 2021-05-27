package com.spring.dynamic.datasource.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

public class DynamicDataSourceManagerRegister implements BeanDefinitionRegistryPostProcessor {
    private ApplicationContext applicationContext;
    private  DynamicDatasourceProperties dynamicDatasourceProperties;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setDynamicDatasourceProperties(DynamicDatasourceProperties dynamicDatasourceProperties) {
        this.dynamicDatasourceProperties = dynamicDatasourceProperties;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        // 注册Bean定义，容器根据定义返回bean
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(PlatformTransactionManager.class);
        BeanDefinition rawBeanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        List<String> datasourceList = dynamicDatasourceProperties.getDatasourceList();
        assert StringUtils.hasLength(dynamicDatasourceProperties.getTransactionManagerSuffix());
        //构造bean定义
        if (CollectionUtils.isEmpty(datasourceList)) {
            String[] beanNamesForType = applicationContext.getBeanNamesForType(DataSource.class);
            for (String name : beanNamesForType) {
                rawBeanDefinition.setDependsOn(name);
                beanDefinitionRegistry.registerBeanDefinition(name + dynamicDatasourceProperties.getTransactionManagerSuffix(),
                        rawBeanDefinition);
            }
        }else {
            for (String name : datasourceList) {
                rawBeanDefinition.setDependsOn(name);
                beanDefinitionRegistry.registerBeanDefinition(name + dynamicDatasourceProperties.getTransactionManagerSuffix(),
                        rawBeanDefinition);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
/*        List<String> datasourceList = dynamicDatasourceProperties.getDatasourceList();
        if (CollectionUtils.isEmpty(datasourceList)) {
            String[] beanNamesForType = applicationContext.getBeanNamesForType(DataSource.class);
            for (String name : beanNamesForType) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(PlatformTransactionManager.class,
                        () -> new DataSourceTransactionManager((DataSource) configurableListableBeanFactory.getBean(name)));

                BeanDefinition beanDefinition = builder.getRawBeanDefinition();
                ((DefaultListableBeanFactory) configurableListableBeanFactory).registerBeanDefinition(name + dynamicDatasourceProperties.getTransactionManagerSuffix(),
                        beanDefinition);
            }

        }*/
    }
}
