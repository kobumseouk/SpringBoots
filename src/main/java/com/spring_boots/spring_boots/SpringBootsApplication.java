package com.spring_boots.spring_boots;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class SpringBootsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootsApplication.class, args);
    }

}
