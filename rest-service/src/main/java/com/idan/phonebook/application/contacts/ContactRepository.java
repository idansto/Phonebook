package com.idan.phonebook.application.contacts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface ContactRepository extends MongoRepository<ContactDocument, String> {
    Page<ContactDocument> findByUserName(String userName, Pageable pageable);

    Optional<ContactDocument> findById(String id);

    Page<ContactDocument> findByUserNameAndFirstNameAndLastName(
            String userName,
            String firstName,
            String lastName,
            Pageable pageable
    );

//    Contact findByUserNameAndId(String userName, String id);

    void deleteById(String id);


}