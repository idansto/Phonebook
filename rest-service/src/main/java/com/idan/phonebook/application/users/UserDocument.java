package com.idan.phonebook.application.users;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Indexed(unique = true)
    private String userName;
    private String password;


    // Getters and Setters

    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
