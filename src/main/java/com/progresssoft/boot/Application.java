package com.progresssoft.boot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.progresssoft.repository.FxDealsInvalidRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = FxDealsInvalidRepository.class)
@ComponentScan(basePackages = "com.progresssoft")
public class Application{

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}