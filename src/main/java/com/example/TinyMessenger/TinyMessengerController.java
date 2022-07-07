package com.example.TinyMessenger;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class TinyMessengerController {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;
    private Key key = new AesKey("const_secret_key".getBytes()); //секретный ключ шифрования

    public TinyMessengerController(UserRepository userRepo, MessageRepository messageRepo) {
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
    }

//    @GetMapping("/auth")
//    ResponseEntity<List<User>> getAllUsers() {
//        return new ResponseEntity<List<User>>(userRepo.findAll(), HttpStatus.OK);
//    }
//
//    @GetMapping("/msg")
//    ResponseEntity<List<Message>> getAllMessages() {
//        return new ResponseEntity<List<Message>>(messageRepo.findAll(), HttpStatus.OK);
//    }

    @PostMapping("/auth")
    ResponseEntity<String> addUser(@RequestBody User user) {
        log.info("* Controller, Post, /auth, addUser, user={}", user);
        //Если пользователь с заданным именем существует - возвращаем сообщение об этом
        if (userRepo.findFirstByName(user.getName()).isPresent())
            return new ResponseEntity<String>("User " + user.getName() + " already exists", HttpStatus.BAD_REQUEST);
        //Если пользователя с заданным именем в базе нет - добавляем его, затем создаем и возвращаем токен
        User newUser = userRepo.save(user);
        log.info("* Controller, Post, /auth, addUser, newUser={}", newUser);
        return new ResponseEntity<String>("token: " + string2Token("name: " + newUser.getName()), HttpStatus.OK);
    }

    @PostMapping("/msg")
    ResponseEntity<List<Message>> processMessage(@RequestBody Message message,
                                                 @RequestHeader("Bearer_token") String tokenString) {
        log.info("* Controller, Post, /msg, processMessage, message={}", message);
        log.info("* Controller, Post, /msg, processMessage, tokenString={}", tokenString);
        //выделяем токен из строки заголовка
        String token = tokenString.substring("Bearer_".length());
        log.info("* Controller, Post, /msg, processMessage, token={}", token);

        //декодируем строку вида ("name: john") из токена
        String decodedPayload = token2String(token);
        log.info("* Controller, Post, /msg, processMessage, decodedPayload={}", decodedPayload);

        //выделяем имя пользователя из строки вида ("name: john")
        String decodedName = decodedPayload.substring("name: ".length());
        log.info("* Controller, Post, /msg, processMessage, decodedUsername={}", decodedName);

        // декодированное имя не совпадает с переданным в сообщении - неверный токен
        if (!decodedName.equals(message.getName()))
            return new ResponseEntity<>(
                    Collections.singletonList(new Message("", "Invalid token")),
                    HttpStatus.BAD_REQUEST);

        if (message.getMessage().equals("history 10")) {
            // если сообщение=="history 10", то выводим список из 10 последних сообщений пользователя
            Optional<List<Message>> olist = messageRepo.getLast10UserMessages(message.getName());
            if (olist.isPresent()) { // список из 10 последних сообщений пользователя не пустой - возвращаем его
                return new ResponseEntity<>(olist.get(), HttpStatus.OK);
            } else // список из 10 последних сообщений пользователя пустой - возвращаем сообщение об этом
                return new ResponseEntity<>(
                        Collections.singletonList(
                                new Message("", "No message exists for user " + message.getName())),
                        HttpStatus.OK);
        } else {
            // иначе добавляем новое сообщение в БД и возвращаем его в ответ
            return new ResponseEntity<>(Collections.singletonList(messageRepo.save(message)), HttpStatus.OK);
        }
    }


    // Алгоритм кодирования строки (payload) в токен. Взято из https://bitbucket.org/b_c/jose4j/wiki/Home
    String string2Token(String payload) {
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setPayload(payload);
        jwe.setAlgorithmHeaderValue(
                KeyManagementAlgorithmIdentifiers.A128KW);
        jwe.setEncryptionMethodHeaderParameter(
                ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        jwe.setKey(key);
        try {
            return jwe.getCompactSerialization();
        } catch (JoseException e) {
            e.printStackTrace();
        }
        return "Error calculating token";
    }

    // Алгоритм декодирования токена в строку (payload). Взято из https://bitbucket.org/b_c/jose4j/wiki/Home
    String token2String(String token) {
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setAlgorithmConstraints(
                new AlgorithmConstraints(
                        AlgorithmConstraints.ConstraintType.PERMIT,
                        KeyManagementAlgorithmIdentifiers.A128KW));
        jwe.setContentEncryptionAlgorithmConstraints(
                new AlgorithmConstraints(
                        AlgorithmConstraints.ConstraintType.PERMIT,
                        ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256));
        jwe.setKey(key);
        try {
            jwe.setCompactSerialization(token);
            return jwe.getPayload();
        } catch (JoseException e) {
            e.printStackTrace();
        }
        return "Error calculating payload - incorrect token";
    }
}

