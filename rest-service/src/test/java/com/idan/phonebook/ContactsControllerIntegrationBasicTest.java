package com.idan.phonebook;

import com.idan.phonebook.application.users.UserDocument;
import com.idan.phonebook.application.users.UserRepository;
import com.idan.phonebook.application.users.UserService;

import com.idan.phonebook.application.contacts.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;


import static com.idan.phonebook.ContactsControllerIntegrationTest1.extractKeyFromResponse;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContactsControllerIntegrationBasicTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactsRepository;
    

    private String token;
    private String userName;
    private String password;



    @BeforeEach
    public void setup() {
        cleanTestDb();
        userName = "Rise" + new Date().getTime();
        password = "cazo1";

        // Register a new user
        UserDocument user = new UserDocument();
        user.setUserName(userName);

        user.setPassword(password);
        userService.addUser(user);

        // Log in to get a token
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/login?userName=" + userName + "&password=" + password,
                null,
                String.class);

        // Extract the token
        this.token = extractKeyFromResponse(response, "token");
    }

    private void cleanTestDb() {
        userRepository.deleteAll();
        contactsRepository.deleteAll();
    }

    @Test
    public void testGetContacts() {
        // Set up headers with the token
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        // Make a GET request to /contacts
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/contacts?page=0&size=10&token=" + token,
                HttpMethod.GET,
                entity,
                String.class);

        // Assert the response status is 200 OK
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // Assert the body contains expected empty content
        assertThat(response.getBody()).contains("\"content\":[]");
    }

}
