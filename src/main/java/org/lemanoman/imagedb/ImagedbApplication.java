package org.lemanoman.imagedb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImagedbApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImagedbApplication.class, args);
    }

}
