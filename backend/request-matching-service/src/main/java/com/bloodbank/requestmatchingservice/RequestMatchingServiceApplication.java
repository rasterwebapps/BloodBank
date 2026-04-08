package com.bloodbank.requestmatchingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.bloodbank.requestmatchingservice", "com.bloodbank.common"})
@EnableJpaAuditing
@EnableCaching
public class RequestMatchingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestMatchingServiceApplication.class, args);
    }
}
