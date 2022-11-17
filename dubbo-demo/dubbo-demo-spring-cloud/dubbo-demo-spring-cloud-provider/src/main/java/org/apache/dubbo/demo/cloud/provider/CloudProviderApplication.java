package org.apache.dubbo.demo.cloud.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author Hank
 * @Date 2022-11-14 23:09
 */
@EnableDiscoveryClient
@EnableDubbo(scanBasePackages = "org.apache.dubbo.demo.cloud.provider")
@SpringBootApplication(scanBasePackages = "org.apache.dubbo.demo.cloud.provider")
public class CloudProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudProviderApplication.class, args);
    }
}
