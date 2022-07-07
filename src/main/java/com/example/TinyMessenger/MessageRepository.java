package com.example.TinyMessenger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // выводим список из 10 последних сообщений пользователя
    @Query(value = "SELECT * FROM messages m WHERE name = :username ORDER BY id DESC LIMIT 10",
            nativeQuery = true)
    Optional<List<Message>> getLast10UserMessages(String username);
}

