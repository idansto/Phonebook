package com.idan.phonebook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idan.phonebook.application.Users.UserDocument;
import com.idan.phonebook.application.Users.UserService;
import com.idan.phonebook.application.contacts.ContactDocument;
import org.bson.json.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.idan.phonebook.ContactsControllerIntegrationTest1.extractKeyFromResponse;
import static org.assertj.core.api.Assertions.assertThat;
@ActiveProfiles("test")

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EditContactFlowIntegrationTest {

    static final String TOKEN = "token";
    static final String ID = "id";
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    private String token;
    private String userName;
    private String password;
    private String contactId;

    @BeforeAll
    public static void setupClass() {

    }

    @BeforeEach
    public void setup() {
        userName = "EditContactUser" + new Date().getTime();
        password = "testEditContact123";

        // Register a new user
        registerUser(userName, password, HttpStatus.OK.value());


        // Log in to get a token
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/login?userName=" + userName + "&password=" + password,
                null,
                String.class);

        // Extract the token
        this.token = extractKeyFromResponse(response, TOKEN);

        // Add a contact for the user using add-contact API
        addInitialContact();
    }

    @Test
    public void testEditContactFlow() throws JsonProcessingException {
        // Edit the contact using the edit-contact REST API
        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Bearer " + token);

        ContactDocument updatedContact = new ContactDocument();
        String updatedFirstName = "UpdatedFirstName";
        String updatedLastName = "UpdatedLastName";

        updatedContact.setFirstName(updatedFirstName);
        updatedContact.setLastName(updatedLastName);
        String updatedNumber = "123456789";
        updatedContact.setPhoneNumber(updatedNumber);
        String updatedAddress = "Ethiopia";
        updatedContact.setAddress(updatedAddress);

        HttpEntity<ContactDocument> request = new HttpEntity<>(updatedContact, headers);

        ResponseEntity<String> editResponse = restTemplate.exchange(
                "http://localhost:" + port + "/edit-contact?id=" + contactId + "&" + TOKEN + "=" + token,
                HttpMethod.PUT,
                request,
                String.class);

        // Assert that the contact was updated successfully
        assertThat(editResponse.getStatusCode().value()).isEqualTo(200);

        // Search and validate the updated contact using the search REST API
        ResponseEntity<String> searchResponse = restTemplate.exchange(
                "http://localhost:" + port + "/search?" + TOKEN + "=" + token + "&firstName=" + updatedFirstName + "&lastName=" + updatedLastName,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class);


        // Extract the updated contact details and assert
        String extractedUpdatedFirstName = Objects.requireNonNull(searchForKey(searchResponse.getBody(), "firstName")).asText();
        String extractedUpdatedLastName = Objects.requireNonNull(searchForKey(searchResponse.getBody(), "lastName")).asText();
        String extractedUpdatedPhoneNumber = Objects.requireNonNull(searchForKey(searchResponse.getBody(), "phoneNumber")).asText();
        String extractedUpdatedAddress = Objects.requireNonNull(searchForKey(searchResponse.getBody(), "address")).asText();


        assertThat(extractedUpdatedFirstName).isEqualTo(updatedFirstName);
        assertThat(extractedUpdatedLastName).isEqualTo(updatedLastName);
        assertThat(extractedUpdatedPhoneNumber).isEqualTo(updatedNumber);
        assertThat(extractedUpdatedAddress).isEqualTo(updatedAddress);

        // Try updating a non-existing contact and validate failure
        String nonExistingContactId = "nonExistingContactId12345";
        ResponseEntity<String> badEditResponse = restTemplate.exchange(
                "http://localhost:" + port + "/edit-contact?id=" + nonExistingContactId + "&" + TOKEN + "=" + token,
                HttpMethod.PUT,
                request,
                String.class);

        // Assert that the non-existing contact update failed
        assertThat(badEditResponse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value()); // Assuming 404 for not found
    }

    private void addInitialContact() {
        HttpHeaders headers = new HttpHeaders();

        ContactDocument contact = new ContactDocument();
        contact.setFirstName("InitialFirstName");
        contact.setLastName("InitialLastName");
        contact.setPhoneNumber("052");
        contact.setAddress("InitialAddress");

        HttpEntity<ContactDocument> request = new HttpEntity<>(contact, headers);

        // Add the contact using add-contact API
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/add-contact?" + TOKEN + "=" + token,
                request,
                String.class);

        // Extract the contactId for further editing
        this.contactId = extractKeyFromResponse(response, ID);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    private void registerUser(String userName, String password, int expectedStatusCode) {
        UserDocument user = new UserDocument();
        user.setUserName(userName);
        user.setPassword(password);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/register",
                user,
                Void.class);

        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatusCode);
    }

    public static JsonNode searchForKey(String searchResponseJson, String key)  {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(searchResponseJson);
            if (rootNode.has(key)) {
                return rootNode.get(key);
            }

            // If it's an object, traverse its fields
            if (rootNode.isObject()) {
                for (JsonNode node : rootNode) {
                    JsonNode foundNode = searchForKey(node.toString(), key);
                    if (foundNode != null) {
                        return foundNode;
                    }
                }
            }

            // If it's an array, traverse each element
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    JsonNode foundNode = searchForKey(node.toString(), key);
                    if (foundNode != null) {
                        return foundNode;
                    }
                }
            }

            return null; // Key not found
        } catch (JsonProcessingException e) {
            return null;

        }
    }
}

