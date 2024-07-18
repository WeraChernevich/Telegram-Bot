package ru.chernevich;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableJpaRepositories("ru.chernevich.*")
@EntityScan("ru.chernevich.*")
@ComponentScan("ru.chernevich.*")
@SpringBootApplication

public class RestService {

    public static void main(String[] args) {
        SpringApplication.run(RestService.class);
    }
}

