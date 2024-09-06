package com.idan.phonebook;

import com.idan.phonebook.application.users.UserDocument;
import com.idan.phonebook.application.users.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
@ActiveProfiles("test")

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRegistrationTest {

    @LocalServerPort
    private int port;  // Injects the dynamically allocated port

    @Autowired
    private UserService userService;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String userName = "UserDup_" + System.currentTimeMillis();
    private final String userName1 = "User1_" + System.currentTimeMillis();
    private final String userName2 = "User2_" + System.currentTimeMillis();
    private final String password = "samePassword123";

    @Test
    public void testRegisterSameUsernameFails() {


        // Register first user
        UserDocument user1 = new UserDocument();
        user1.setUserName(userName);
        user1.setPassword(password);
        ResponseEntity<String> response1 = restTemplate.postForEntity(getRegisterUrl(), user1, String.class);

        // Assert the first registration is successful
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Register second user with the same username but same password
        UserDocument user2 = new UserDocument();
        user2.setUserName(userName);
        user2.setPassword(password);
        ResponseEntity<String> response2 = restTemplate.postForEntity(getRegisterUrl(), user2, String.class);

        // Assert the second registration fails (username must be unique)
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testRegisterSamePasswordSucceeds() {

        // Register first user
        UserDocument user1 = new UserDocument();
        user1.setUserName(userName1);
        user1.setPassword(password);
        ResponseEntity<String> response1 = restTemplate.postForEntity(getRegisterUrl(), user1, String.class);

        // Assert the first registration is successful
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Register second user with a different username but the same password
        UserDocument user2 = new UserDocument();
        user2.setUserName(userName2);
        user2.setPassword(password);
        ResponseEntity<String> response2 = restTemplate.postForEntity(getRegisterUrl(), user2, String.class);

        // Assert the second registration is also successful
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    private String getRegisterUrl() {
        return "http://localhost:" + port + "/register";  // Use the dynamically allocated port
    }
}
