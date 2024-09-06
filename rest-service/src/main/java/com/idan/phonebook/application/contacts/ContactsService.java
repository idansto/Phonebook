package com.idan.phonebook.application.contacts;

import com.idan.phonebook.application.exceptions.ContactNotExistsException;
import com.idan.phonebook.application.exceptions.DuplicateContactException;
import com.idan.phonebook.application.exceptions.InternalPhoneBookServerError;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContactsService {


    @Autowired
    private ContactRepository contactRepository;

    private static final Logger logger = LoggerFactory.getLogger(ContactsService.class);


    public Page<ContactDocument> getContactsByUserName(String userName, Pageable pageable) {

        return contactRepository.findByUserName(userName, pageable);
    }

    public ContactDocument addContact(ContactDocument contact) {

        try {
            return contactRepository.save(contact);
        } catch (DuplicateKeyException e) {
            logger.warn("Failed to add contact due to duplicate id: {}", contact.getId());

            throw new DuplicateContactException("A contact with the same username and phone number already exists.", e);
        }
    }

    public Page<ContactDocument> getContactsByUserNameAndFirstNameAndLastName(String firstName, String lastName, String userName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return contactRepository.findByUserNameAndFirstNameAndLastName(userName, firstName, lastName, pageable);
    }

    public void deleteContact(String id) {
        Optional<ContactDocument> contact = contactRepository.findById(id);
        if (contact.isEmpty()) {
            logger.warn("Contact not found for id: {}", id);

            throw new ContactNotExistsException("Contact not found");
        }
        try {

            contactRepository.deleteById(id);
        } catch (RuntimeException e) {
            throw new InternalPhoneBookServerError("Failed to delete contact with ID: " + id);
        }
    }

    public ContactDocument updateContact(String id, ContactDocument updatedContact) {
        Optional<ContactDocument> existingContact = contactRepository.findById(id);
        if (existingContact.isEmpty()) {
            logger.warn("Contact not found for id: {}", id);

            throw new ContactNotExistsException("contact not found");
        }
        ContactDocument actualExistingContact = existingContact.get();
        actualExistingContact.setFirstName(updatedContact.getFirstName());
        actualExistingContact.setLastName(updatedContact.getLastName());
        actualExistingContact.setPhoneNumber(updatedContact.getPhoneNumber());
        actualExistingContact.setAddress(updatedContact.getAddress());
        actualExistingContact.setId(id);

        try {
            return contactRepository.save(actualExistingContact); //maybe call here to addContact?
        } catch (DuplicateKeyException e) {

            logger.warn("Failed to update. contact already exists. id: {}", updatedContact.getId());

            throw new DuplicateContactException("contact already exists.", e);
        }
    }


    //##################################

    public void deleteAll() {

        contactRepository.deleteAll();

    }
}