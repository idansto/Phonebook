//package com.idan.phonebook.application.config;
//
//import com.mongodb.MongoClientSettings;
//import com.mongodb.MongoCredential;
//import com.mongodb.ServerAddress;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
//
//import static java.util.Collections.singletonList;
//
//@Configuration
//public class Config extends AbstractMongoClientConfiguration {
//
//    @Override
//    protected String getDatabaseName() {
//        return "contacts";
//    }
//
//    @Override
//    public boolean autoIndexCreation() {
//        return true;
//    }
//
//    @Override
//    protected void configureClientSettings(MongoClientSettings.Builder builder) {
//
//        builder.applyToClusterSettings(settings  -> {
//                    settings.hosts(singletonList(new ServerAddress("127.0.0.1", 27017)));
//                });
//    }
//}
