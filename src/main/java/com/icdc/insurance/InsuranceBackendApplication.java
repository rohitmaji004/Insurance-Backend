package com.icdc.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.icdc.insurance")
public class InsuranceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceBackendApplication.class, args);
    }
}
