package com.farmapredict;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FarmaPredictApplication {
    public static void main(String[] args) {
        SpringApplication.run(FarmaPredictApplication.class, args);
    }
}
