package com.acme.library.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LibraryProperties.class)
public class LibraryConfig {
    @Bean 
    public Clock clock(LibraryProperties properties) {
        return Clock.system(ZoneId.of(properties.timeZone()));
    }
}
