package com.example.TinyMessenger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo, MessageRepository messageRepo) {
        return args -> {
            log.info("Preloading " + userRepo.save(new User("john", "111")));
            log.info("Preloading " + userRepo.save(new User("jack", "222")));
            log.info("Preloading " + messageRepo.save(new Message("john", "john_msg_1")));
            log.info("Preloading " + messageRepo.save(new Message("john", "john_msg_2")));
            log.info("Preloading " + messageRepo.save(new Message("jack", "jack_msg_1")));
        };
    }
}

