package com.tui.proof;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class MainApplication
{

    public static final String VERSION_1_0 = "/v1.0";

    public static void main(String[] args)
    {
        SpringApplication.run(MainApplication.class, args);
    }

}
