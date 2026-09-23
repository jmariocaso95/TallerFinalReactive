package com.example.taller.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimuladorConfiguration {

    @Bean
    public SimuladorState simuladorState() {
        return new SimuladorState();
    }
}
