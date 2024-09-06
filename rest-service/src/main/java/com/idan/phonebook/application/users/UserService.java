package com.idan.phonebook.application.Users;

import com.idan.phonebook.application.contacts.ContactsController;
import com.idan.phonebook.application.exceptions.InternalPhoneBookServerError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    public void addUser(UserDocument user) { //TODO: save passwords encrypted.

        try {
            userRepository.save(user);
        } catch (RuntimeException e) {

            logger.warn("couldn't register user: {}", user.getUserName());
            throw new InternalPhoneBookServerError("couldn't register user: " + user.getUserName());
        }
    }

    public boolean isAuthenticated(String username, String password) {
        Optional<UserDocument> optionalUser = userRepository.findByUserName(username);
        if (optionalUser.isPresent()) {
            return optionalUser.get().getPassword().equals(password);
        }
        return false;
    }

}

