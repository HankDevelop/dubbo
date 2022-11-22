package org.apache.dubbo.demo.cloud.consumer.interceptor;

import com.alibaba.fastjson.JSON;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.util.Set;

/**
 * 日志jar包冲突校验
 */
@Order(100)
@Component
public class LogJarConflictCheck implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        try {
            Class<LoggerFactory> loggerFactoryClazz = LoggerFactory.class;
            Constructor<LoggerFactory> constructor = loggerFactoryClazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            LoggerFactory instance = constructor.newInstance();
            Method method = loggerFactoryClazz.getDeclaredMethod("findPossibleStaticLoggerBinderPathSet");
            // 强制进入
            method.setAccessible(true);
            Set<URL> staticLoggerBinderPathSet = (Set<URL>) method.invoke(instance);
            if (CollectionUtils.isEmpty(staticLoggerBinderPathSet)) {
                handleLogJarConflict(staticLoggerBinderPathSet, "Class path is Empty.添加对应日志jar包");
            }
            if (staticLoggerBinderPathSet.size() == 1) {
                return;
            }
            handleLogJarConflict(staticLoggerBinderPathSet, "Class path contains multiple SLF4J bindings. 注意排包");
        } catch (Throwable t) {
            t.getStackTrace();
        }
    }

    /**
     * 日志jar包冲突报警
     *
     * @param staticLoggerBinderPathSet jar包路径
     * @param tip                       提示语
     */
    private void handleLogJarConflict(Set<URL> staticLoggerBinderPathSet, String tip) {
        String ip = getLocalHostIp();
        StringBuilder detail = new StringBuilder();
        detail.append("ip为").append(ip).append("; 提示语为").append(tip);
        if (CollectionUtils.isNotEmpty(staticLoggerBinderPathSet)) {
            String path = JSON.toJSONString(staticLoggerBinderPathSet);
            detail.append("; 重复的包路径分别为 ").append(path);
        }
        String logDetail = detail.toString();
        //可以自定义告警
        System.out.println("====>" + logDetail);
    }

    private String getLocalHostIp() {
        String ip;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();
        } catch (Exception var2) {
            ip = "";
        }
        return ip;
    }
}
