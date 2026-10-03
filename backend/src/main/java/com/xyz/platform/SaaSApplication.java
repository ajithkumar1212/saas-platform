package com.xyz.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SaaSApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaaSApplication.class, args);
    }
}
