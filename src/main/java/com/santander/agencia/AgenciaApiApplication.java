package com.santander.agencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
public class AgenciaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenciaApiApplication.class, args);
    }
}
