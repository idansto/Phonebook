package com.idan.phonebook.application.Users;

import com.idan.phonebook.application.Security.AuthResponse;
import com.idan.phonebook.application.Security.JwtUtil;
import com.idan.phonebook.application.exceptions.InternalPhoneBookServerError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService service;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody UserDocument user) {
        logger.info("Registering user with username: {}", user.getUserName());
        try {
            service.addUser(user);
            logger.info("User registered successfully: {}", user.getUserName());


        } catch (InternalPhoneBookServerError e) {
            logger.error("Failed to register user with username: {} due to server error", user.getUserName());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestParam String userName,
            @RequestParam String password) {
        logger.info("User login attempt with username: {}", userName);

        try {
            if (service.isAuthenticated(userName, password)) {
                logger.info("User authenticated successfully: {}", userName);

                String token = JwtUtil.generateToken(userName);

                AuthResponse authResponse = new AuthResponse(token);
                return new ResponseEntity<>(authResponse, HttpStatus.OK);
            }
        } catch (RuntimeException e) {
            logger.warn("User authentication failed for username: {}", userName);
        }
        return new ResponseEntity<>(new AuthResponse(null), HttpStatus.UNAUTHORIZED);

    }
}
