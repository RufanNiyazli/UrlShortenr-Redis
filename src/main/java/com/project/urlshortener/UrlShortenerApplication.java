package com.project.urlshortener;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EntityScan("com.project")
@ComponentScan("com.project")
@EnableScheduling



public class UrlShortenerApplication {

    public static void main(String[] args) {

        SpringApplication.run(UrlShortenerApplication.class, args);
    }

}
