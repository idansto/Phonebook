package com.idan.phonebook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idan.phonebook.application.Users.UserDocument;
import com.idan.phonebook.application.Users.UserService;
import com.idan.phonebook.application.contacts.ContactDocument;
import com.idan.phonebook.application.contacts.ContactsService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContactsControllerIntegrationTest1 {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ContactsService contactsService;

    private static String tokenUser1;
    private static String tokenUser2;
    private static String userName1;
    private static String password1;
    private static String userName2;
    private static String password2;
    private static boolean usersRegistered = false;


    @BeforeAll
    public static void setupClass() {
        // Set up any static resources if needed

        userName1 = "User1_" + new Date().getTime();
        password1 = "password1";
        userName2 = "User2_" + new Date().getTime();
        password2 = "password2";

        // Register two users


    }

    @BeforeEach
    public void setup() {
        if (!usersRegistered) {
            registerUser(userName2, password2, HttpStatus.OK.value());
            registerUser(userName1, password1, HttpStatus.OK.value());
            usersRegistered = true;
        }

        // Log in and get tokens for both users
        tokenUser1 = loginUser(userName1, password1);
        tokenUser2 = loginUser(userName2, password2);
    }


    @Test
    public void testRegisterDuplicateUsername() {
        // Try to register a user with the same username
        registerUser(userName1, "somePassword", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void testUserContacts() {
        int numberOfContacts = 6;
        // Add 6 contacts for User1
        addContactsForUser(tokenUser1, "User1Contact", numberOfContacts);

        // Add 6 contacts for User2
        addContactsForUser(tokenUser2, "User2Contact", numberOfContacts);

        // Get contacts for User1 and validate
        ResponseEntity<String> responseUser1 = restTemplate.exchange(
                "http://localhost:" + port + "/contacts?page=0&size=10&token=" + tokenUser1,
                HttpMethod.GET,
                new HttpEntity<>(null, new HttpHeaders()),
                String.class);
        assertThat(responseUser1.getStatusCode().value()).isEqualTo(200);
        assertThat(responseUser1.getBody()).contains("User1Contact");
        assertThat(extractKeyFromResponse(responseUser1, "totalElements")).isEqualTo(String.valueOf(numberOfContacts));

        // Get contacts for User2 and validate
        ResponseEntity<String> responseUser2 = restTemplate.exchange(
                "http://localhost:" + port + "/contacts?page=0&size=10&token=" + tokenUser2,
                HttpMethod.GET,
                new HttpEntity<>(null, new HttpHeaders()),
                String.class);
        assertThat(responseUser2.getStatusCode().value()).isEqualTo(200);
        assertThat(responseUser2.getBody()).contains("User2Contact");
        ;
        assertThat(extractKeyFromResponse(responseUser2, "totalElements")).isEqualTo(String.valueOf(numberOfContacts));

        // Ensure User1's contacts are not visible to User2
        assertThat(responseUser2.getBody()).doesNotContain("User1Contact");
        assertThat(responseUser1.getBody()).doesNotContain("User2Contact");
    }

    public void registerUser(String userName, String password, int expectedStatusCode) {
        UserDocument user = new UserDocument();
        user.setUserName(userName);
        user.setPassword(password);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/register",
                user,
                Void.class);

        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatusCode);
    }

    private String loginUser(String userName, String password) {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/login?userName=" + userName + "&password=" + password,
                null,
                String.class);

        return extractKeyFromResponse(response, "token");
    }

    private void addContactsForUser(String token, String contactPrefix, int num) {
        for (int i = 1; i <= num; i++) {
            ContactDocument contact = new ContactDocument();
            contact.setFirstName(contactPrefix + i);
            contact.setLastName("LastName" + i);
            contact.setAddress("Jerus" + i);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<ContactDocument> entity = new HttpEntity<>(contact, headers);

            restTemplate.postForEntity(
                    "http://localhost:" + port + "/add-contact?token=" + token,
                    entity,
                    Void.class);
        }
    }

    public static String extractKeyFromResponse(ResponseEntity<String> responseEntity, String key) {
        String json = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert JSON string to Map
            JsonNode responseMap = objectMapper.readTree(json);
            return responseMap.path(key).asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
