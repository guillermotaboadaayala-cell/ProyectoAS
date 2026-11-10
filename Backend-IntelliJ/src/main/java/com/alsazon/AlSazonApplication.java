package com.alsazon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlSazonApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlSazonApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" ¡AL SAZON (SPRING BOOT + HIBERNATE) ACTIVO EN: http://localhost:8080/ !");
        System.out.println("==================================================================");
    }
}