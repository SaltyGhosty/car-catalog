package it.carcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CarCatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarCatalogApplication.class, args);
    }
}
