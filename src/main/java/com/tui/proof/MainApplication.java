package com.tui.proof;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication
{

    public static final String VERSION_1_0 = "/v1.0";

    public static void main(String[] args)
    {
        SpringApplication.run(MainApplication.class, args);
    }

}
