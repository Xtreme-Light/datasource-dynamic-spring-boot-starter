package com.spring.dynamic.datasource.aspect;

import com.spring.dynamic.datasource.annotation.DynamicDatasource;
import com.spring.dynamic.datasource.config.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Order(1)
@Component
@Slf4j
public class DataSourceAspect {
    @Pointcut("@annotation(com.spring.dynamic.datasource.annotation.DynamicDatasource)")
    public void doPointCut() {

    }
    @Around("doPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DynamicDatasource dataSourceAnnotation = method.getAnnotation(DynamicDatasource.class);
        if (dataSourceAnnotation != null) {
            String value = dataSourceAnnotation.value();
            if (log.isDebugEnabled()) {
                log.debug("切换数据源到{}", value);
            }
            DynamicDataSourceContextHolder.setDataSource(value);
        }
        try{
            return point.proceed();
        }finally {
            DynamicDataSourceContextHolder.clearDataSource();
        }
    }

}
