package com.bloodbank.branchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.bloodbank.branchservice",
    "com.bloodbank.common"
})
public class BranchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BranchServiceApplication.class, args);
    }
}
