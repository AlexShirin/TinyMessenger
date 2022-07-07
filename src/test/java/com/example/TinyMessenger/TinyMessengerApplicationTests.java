package com.example.TinyMessenger;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TinyMessengerApplicationTests {
	private static final Logger log = LoggerFactory.getLogger(TinyMessengerController.class);

	@Autowired
	WebTestClient webTestClient;

	@BeforeEach
	void setUp() {}

	@Disabled
	@Test
	void GetAllUsersTest() {
		EntityExchangeResult<List<User>> response = webTestClient
				.get()
				.uri("/auth") // the base URL is already configured for us
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
				.expectBodyList(User.class)
				.returnResult();
		List<User> body = response.getResponseBody();
		log.info("* GetAllUsersTest, List<User>=\n{}", body);
		assertThat(body).isNotNull();
		assertThat(body.size()).isEqualTo(2);
	}

	@Disabled
	@Test
	void GetAllMessagesTest() {
		EntityExchangeResult<List<Message>> response = webTestClient
				.get()
				.uri("/msg") // the base URL is already configured for us
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
				.expectBodyList(Message.class)
				.returnResult();
		List<Message> body = response.getResponseBody();
		log.info("* GetAllMessagesTest, List<Message>=\n{}", body);
		assertThat(body).isNotNull();
		assertThat(body.size()).isEqualTo(3);
	}

	@Test
	void CreateUserAndAddHimAMessageTest() {
		// добавляем нового пользователя, получаем его токен
		User user = new User("johnny", "123");
		EntityExchangeResult<String> response = webTestClient
				.post()
				.uri("/auth") // the base URL is already configured for us
				.body(Mono.just(user), User.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
				.expectBody(String.class)
				.returnResult();
		String body = response.getResponseBody();
		log.info("* CreateUserAndAddHimAMessageTest, CreateUser body=\n{}", body);
		assertThat(body).isNotNull();
		assertThat(body.startsWith("token: "));
		String token = body.substring("token: ".length());

		// добавляем сообщение для нового пользователя
		Message message = new Message("johnny", "johnny_msg_1");
		EntityExchangeResult<List<Message>> response2 = webTestClient
				.post()
				.uri("/msg") // the base URL is already configured for us
				.body(Mono.just(message), Message.class)
				.header("Bearer_token", "Bearer_" + token)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
				.expectBodyList(Message.class)
				.returnResult();
		List<Message> body2 = response2.getResponseBody();
		assertThat(body2).isNotNull();
		assertThat(body2.size()).isEqualTo(1);
		Message body2Response = body2.get(0);
		assertThat(body2Response.getName()).isEqualTo(message.getName());
		assertThat(body2Response.getMessage()).isEqualTo(message.getMessage());

		// получаем список сообщений для нового пользователя
		Message message2 = new Message("johnny", "history 10");
		EntityExchangeResult<List<Message>> response3 = webTestClient
				.post()
				.uri("/msg") // the base URL is already configured for us
				.body(Mono.just(message2), Message.class)
				.header("Bearer_token", "Bearer_" + token)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
				.expectBodyList(Message.class)
				.returnResult();
		List<Message> body3 = response3.getResponseBody();
		assertThat(body3).isNotNull();
		assertThat(body3.size()).isEqualTo(1);
		Message body3Response = body3.get(0);
		assertThat(body3Response.getName()).isEqualTo(message2.getName());
		assertThat(body3Response.getMessage()).isEqualTo(message.getMessage());
	}
}
