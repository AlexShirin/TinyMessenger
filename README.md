docker link: docker pull alexshirin752/tinymessenger:latest

## Мессенджер
### Описание
Мессенджер представляет собой Веб-сервис, который позволяет создавать, хранить и отображать сообщения пользователей. 
Мы добавляем своего пользователя, добавляем ему сообщения, а также можем посмотреть последние 10 сообщений пользователя. 
При создании пользователя ему выдается токен, с помощью которого происходит аутентификация пользователя в процессе последующего взаимодействия.
Хранение списка пользователей и их сообщений производится в БД Н2, хранящейся в оперативной памяти.

### Порядок работы программы

##### Создаем нового пользователя, получаем его токен для последующей аутентификации

Эндпоинт http://localhost:8484/auth 

Данные нового пользователя передаются в формате JSON в виде: {"name": "johnny", "password": "123"}

Пример curl запроса:

$ curl -d '{"name":"johnny", "password":"123"}' -H "Content-Type: application/json" -X POST http://localhost:8484/auth

Сервер регистрирует нового пользователя и в ответ присылает его токен в формате JSON в строке вида 

token: eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.Ck7wNUG1uP79PvRBbB8tgD1kYKRrqY132gNXRZps2u_embYaNv7k_A.cSvUsZ4FnHJdKYHy5ublXg.dS1o6E8EL-m0p4lyNNSFcw.Lmu7pOyyaMj_4h6sgh3W9g

##### Добавляем сообщения новому пользователю с аутентификацией по токену

Эндпоинт http://localhost:8484/msg 

Сообщения передаются в формате JSON в виде: {"name": "johnny", "message": "Hi all"}

Для аутентификации пользователя необходимо создать в сообщении дополнительный Header с названием "Bearer_token" вида
Bearer_eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.Ck7wNUG1uP79PvRBbB8tgD1kYKRrqY132gNXRZps2u_embYaNv7k_A.cSvUsZ4FnHJdKYHy5ublXg.dS1o6E8EL-m0p4lyNNSFcw.Lmu7pOyyaMj_4h6sgh3W9g

Пример curl запроса:

$ curl -d '{"name":"johnny", "message":"hi there"}' -H "Content-Type: application/json" -H "Bearer_token: Bearer_eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.Ck7wNUG1uP79PvRBbB8tgD1kYKRrqY132gNXRZps2u_embYaNv7k_A.cSvUsZ4FnHJdKYHy5ublXg.dS1o6E8EL-m0p4lyNNSFcw.Lmu7pOyyaMj_4h6sgh3W9g" -X POST http://localhost:8484/msg

##### Просматриваем список последних 10 сообщений пользователя с аутентификацией по токену

Эндпоинт http://localhost:8484/msg 

Сообщения передаются в формате JSON в виде: {"name": "johnny", "message": "history 10"}

Для аутентификации пользователя необходимо создать в сообщении дополнительный Header с названием "Bearer_token" вида
Bearer_eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.Ck7wNUG1uP79PvRBbB8tgD1kYKRrqY132gNXRZps2u_embYaNv7k_A.cSvUsZ4FnHJdKYHy5ublXg.dS1o6E8EL-m0p4lyNNSFcw.Lmu7pOyyaMj_4h6sgh3W9g

Пример curl запроса:

$ curl -d '{"name":"johnny", "message":"history 10"}' -H "Content-Type: application/json" -H "Bearer_token: Bearer_eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.Ck7wNUG1uP79PvRBbB8tgD1kYKRrqY132gNXRZps2u_embYaNv7k_A.cSvUsZ4FnHJdKYHy5ublXg.dS1o6E8EL-m0p4lyNNSFcw.Lmu7pOyyaMj_4h6sgh3W9g" -X POST http://localhost:8484/msg

#### Дополнительная информация

При запуске сервера в БД создаются два пользователя 

{"name": "john", "password": "111"}
{"name": "jack", "password": "222"}

и им добавляются сообщения

{"name": "john", "message": "john_msg_1"} 
{"name": "john", "message": "john_msg_2"} 
{"name": "jack", "message": "jack_msg_1"}

Это сделано для упрощения тестирования сервера и д.б. убрано в релизе.
