package org.apache.dubbo.demo.cloud.consumer.interceptor;

import org.springframework.beans.BeansException;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class BeanHolder implements ApplicationContextAware {

    public static ApplicationContext applicationContext;


    public static DiscoveryClient getDiscoveryClient() {
        return applicationContext.getBean(DiscoveryClient.class);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
