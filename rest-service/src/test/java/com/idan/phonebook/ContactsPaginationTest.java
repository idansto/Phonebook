package com.idan.phonebook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.idan.phonebook.application.contacts.ContactDocument;
import com.idan.phonebook.application.Users.UserDocument;
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

import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.idan.phonebook.EditContactFlowIntegrationTest.searchForKey;
import static org.assertj.core.api.Assertions.assertThat;
@ActiveProfiles("test")

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContactsPaginationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    private String userName = "User_" + System.currentTimeMillis();
    private String password = "testPassword123";

    @BeforeEach
    public void setup() throws JsonProcessingException {
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

        // Add 20 contacts for the user
        IntStream.rangeClosed(1, 20).forEach(i -> addContact("FirstName" + i, "LastName" + i));
    }

    private void addContact(String firstName, String lastName) {
        ContactDocument contact = new ContactDocument();
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
        HttpEntity<ContactDocument> request = new HttpEntity<>(contact, headers);
        restTemplate.postForEntity("http://localhost:" + port + "/add-contact?token=" + token, request, String.class);
    }

    @Test
    public void testContactsPagination() {
        int pageSize = 5;
        int totalPages = 4; // 20 contacts, pageSize 5 -> 4 pages
        for (int page = 0; page < totalPages; page++) {
            verifyPageContacts(page, pageSize);
        }

        // Make sure the 5th page request returns no content (as there are only 4 pages)
        ResponseEntity<String> response = getContactsPage(4, pageSize);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"content\":[]");  // No contacts on this page
    }

    private void verifyPageContacts(int page, int pageSize) {
        ResponseEntity<String> response = getContactsPage(page, pageSize);
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // Validate that the number of elements in this page equals the pageSize
        String expectedContacts = String.format("\"numberOfElements\":%d", pageSize);
        assertThat(response.getBody()).contains(expectedContacts);

        // Verify that the correct contacts are returned for the current page
        for (int i = 1; i <= pageSize; i++) {
            String expectedContact = String.format("\"firstName\":\"FirstName%d\"", (page * pageSize) + i);
            assertThat(response.getBody()).contains(expectedContact);
        }
    }

    private ResponseEntity<String> getContactsPage(int page, int size) {
        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(
                "http://localhost:" + port + "/contacts?page=" + page + "&size=" + size + "&token=" + token,
                HttpMethod.GET,
                entity,
                String.class
        );
    }


}
