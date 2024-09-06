package com.idan.phonebook.application.contacts;

import com.idan.phonebook.application.exceptions.ContactNotExistsException;
import com.idan.phonebook.application.exceptions.DuplicateContactException;
import com.idan.phonebook.application.exceptions.InternalPhoneBookServerError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.idan.phonebook.application.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
public class ContactsController {

    public static final int DEF_PAGE_SIZE = 10;
    private static final int DEF_PAGE_NUM = 0;
    public static final int MAX_PAGE_SIZE = 10;
    @Autowired
    private ContactsService contactsService;
    private static final Logger logger = LoggerFactory.getLogger(ContactsController.class);


    @GetMapping("/contacts")
    public ResponseEntity<Page<ContactDocument>> getContacts(
            @RequestParam String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        size = (size >= 0 && size <= 10) ? size : DEF_PAGE_SIZE;
        page = (page >= 0) ? page : DEF_PAGE_NUM;
        logger.info("Fetching contacts for token: {} on page: {}", token, page);


        String username = JwtUtil.validateToken(token);
        if (username != null) {
            logger.debug("Token validation successful. Username: {}", username);
        } else {
            logger.warn("Token validation failed for token: {}", token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Return UNAUTHORIZED if token is invalid
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ContactDocument> contacts = contactsService.getContactsByUserName(username, pageable);
        logger.info("Returning {} in page {}, of contacts {} for username: {}", contacts.getNumberOfElements(), page, contacts.getTotalElements(), username);

        return ResponseEntity.ok(contacts); // Return the contacts if the token is valid

    }

    @GetMapping("/search")
    public Page<ContactDocument> searchContact(@RequestParam(value = "firstName", defaultValue = "") String firstName,
                                               @RequestParam(value = "lastName", defaultValue = "") String lastName,
                                               @RequestParam String token,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {

        size = (size >= 0 && size <= MAX_PAGE_SIZE) ? size : DEF_PAGE_SIZE;
        page = (page >= 0) ? page : DEF_PAGE_NUM;
        logger.info("Searching contacts by firstName: {} and lastName {} for token: {}", firstName, lastName, token);

        String username = JwtUtil.validateToken(token);
        if (username != null) {
            logger.debug("Token validation successful. Username: {}", username);


        } else {
            logger.warn("Token validation failed for token: {}", token);

        }
        return contactsService.getContactsByUserNameAndFirstNameAndLastName(firstName, lastName, username, page, size);
    }

    @PostMapping("/add-contact")
    public ResponseEntity<ContactDocument> addContact(@RequestBody ContactDocument contact,
                                                      @RequestParam(value = "token") String token
    ) {
        logger.info("Adding new contact for token: {}", token);

        String username = JwtUtil.validateToken(token);
        if (username != null) {
            logger.debug("Token validation successful. Username: {}", username);


        } else {
            logger.warn("Token validation failed for token: {}", token);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Return UNAUTHORIZED if token is invalid

        }
        contact.setUserName(username);
        ContactDocument contactResponse;
        try {
            contactResponse = contactsService.addContact(contact);
            logger.info("Contact added successfully for username: {}", username);

        } catch (DuplicateContactException e) {
            logger.error("Duplicate contact detected for username: {}", username);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(contactResponse);
    }

    @PutMapping("/edit-contact")
    public ResponseEntity<ContactDocument> updateContact(
            @RequestParam String id,
            @RequestBody ContactDocument updatedContact,
            @RequestParam(value = "token") String token) {

        logger.info("Updating contact with ID: {} for token: {}", id, token);


        String username = JwtUtil.validateToken(token);
        if (username != null) {
            logger.debug("Token validation successful. Username: {}", username);


        } else {
            logger.warn("Token validation failed for token: {}", token);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        }
        try {
            ContactDocument contact = contactsService.updateContact(id, updatedContact);

            logger.info("Contact {} with ID: {} updated successfully for username: {}", contact.getFirstName() + " " + contact.getLastName(), id, username);

            return ResponseEntity.ok(contact);
        } catch (ContactNotExistsException e) {
            logger.error("Contact with ID: {} does not exist", id);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DuplicateContactException e) {

            logger.error("Duplicate contact ({}) detected during update for id: {}", updatedContact.getFirstName() + " " + updatedContact.getLastName(), id);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete-contact")
    public ResponseEntity<Object> deleteContact(@RequestParam(value = "token") String token, @RequestParam(value = "id") String id) {

        logger.info("Deleting contact with ID: {} for token: {}", id, token);

        String username = JwtUtil.validateToken(token);
        if (username != null) {
            logger.debug("Token validation successful. Username: {}", username);


        } else {
            logger.warn("Token validation failed for token: {}", token);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Return UNAUTHORIZED if token is invalid

        }
        try {
            contactsService.deleteContact(id);
        } catch (ContactNotExistsException e) {
            logger.error("Contact with ID: {} does not exist", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (InternalPhoneBookServerError e) {
            logger.error("Failed to delete contact with ID: {}.", id);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    //################################################
    @DeleteMapping("/delete-all")
    public void deleteAllContact() {
        contactsService.deleteAll();
    }


}