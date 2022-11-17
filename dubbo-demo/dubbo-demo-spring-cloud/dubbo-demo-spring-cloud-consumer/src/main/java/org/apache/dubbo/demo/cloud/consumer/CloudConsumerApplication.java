package org.apache.dubbo.demo.cloud.consumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author Hank
 * @Date 2022-11-14 22:47
 */
@EnableDiscoveryClient
@EnableDubbo(scanBasePackages = "org.apache.dubbo.demo.cloud.consumer")
@SpringBootApplication(scanBasePackages = "org.apache.dubbo.demo.cloud.consumer")
public class CloudConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudConsumerApplication.class, args);
    }
}
