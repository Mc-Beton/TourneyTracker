package com.tourney;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.common", "com.tourney.user_service"})
public class ApplicationRun {
    public static void main(String[] args) {

    }
}
