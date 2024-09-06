package com.idan.phonebook;

import com.idan.phonebook.application.users.UserDocument;
import com.idan.phonebook.application.contacts.ContactDocument;
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


import java.util.Objects;
import java.util.UUID;

import static com.idan.phonebook.EditContactFlowIntegrationTest.searchForKey;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContactsDeletionTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    private String userName = "User_" + System.currentTimeMillis();
    private String password = "testPassword123";
    private String contactId;

    @BeforeEach
    public void setup() {
        // Register a new user
        UserDocument user = new UserDocument();
        user.setUserName(userName);
        user.setPassword(password);
        restTemplate.postForEntity("http://localhost:" + port + "/register", user, String.class);

        // Log in to get a token
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/login?userName=" + userName + "&password=" + password,
                null,
                String.class);

        this.token = Objects.requireNonNull(searchForKey(loginResponse.getBody(), "token")).asText();

        // Add a contact and store its ID
        contactId = addContact("ContactFirstName", "ContactLastName");
    }

    private String addContact(String firstName, String lastName) {
        ContactDocument contact = new ContactDocument();
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
        HttpEntity<ContactDocument> request = new HttpEntity<>(contact, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + "/add-contact?token=" + token, request, String.class);

        // Extract the contact ID from the response
        return Objects.requireNonNull(searchForKey(response.getBody(), "id")).asText();
    }

    @Test
    public void testDeleteContact() {

        // Verify that the contact is indeed deleted
        ResponseEntity<String> getResponse = getContact(contactId);
        assertThat(getResponse.getBody()).contains(contactId);
        assertThat(getResponse.getStatusCode().value()).isEqualTo(200); // OK


        // Test deleting an existing contact
        ResponseEntity<String> deleteResponse = deleteContact(contactId);
        assertThat(deleteResponse.getStatusCode().value()).isEqualTo(200); // OK

        // Verify that the contact is indeed deleted
        ResponseEntity<String> getResponseDeleted = getContact(contactId);
        assertThat(getResponseDeleted.getBody()).doesNotContain(contactId);
        assertThat(getResponseDeleted.getStatusCode().value()).isEqualTo(200); // OK


        // Test deleting a non-existing contact
        String nonExistingContactId = UUID.randomUUID().toString();
        ResponseEntity<String> deleteNonExistingResponse = deleteContact(nonExistingContactId);
        assertThat(deleteNonExistingResponse.getStatusCode().value()).isEqualTo(404); // Not Found
    }

    private ResponseEntity<String> deleteContact(String id) {
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(
                "http://localhost:" + port + "/delete-contact?id=" + id + "&token=" + token,
                HttpMethod.DELETE,
                entity,
                String.class
        );
    }

    private ResponseEntity<String> getContact(String id) {
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(
                "http://localhost:" + port + "/contacts?id=" + id + "&token=" + token,
                HttpMethod.GET,
                entity,
                String.class
        );
    }
}
