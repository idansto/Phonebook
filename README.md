# Phonebook Application

This is a Spring Boot-based phonebook application that allows users to manage their contacts.
The application supports JWT-based authentication and provides endpoints for adding, updating, searching, and deleting contacts.
 It also includes user management features.

## Table of Contents

- [Project Structure](#project-structure)
- [Dependencies](#dependencies)
- [Setting Up the Project](#setting-up-the-project)
- [API Endpoints](#api-endpoints)
  - [User Management](#user-management)
  - [Contacts Management](#contacts-management)
- [ContactDocument](#contactdocument)
- [UserDocument](#userdocument)
- [Performance](#performance)
- [Logging](#logging)
- [Tests](#tests)

## Project Structure

- `com.idan.phonebook.application.contacts`: Contains the controllers and services related to contact management.
- `com.idan.phonebook.application.Users`: Contains the controllers and services related to user management.
- `com.idan.phonebook.application.Security`: Handles JWT authentication and token generation.

## Dependencies

- **Spring Boot**: Core framework.
- **Spring Data MongoDB**: To interact with MongoDB.
- **JWT**: For securing the application using JSON Web Tokens.
- **Logstash & Elasticsearch**: For logging and monitoring (optional).

## Setting Up the Project

1. Clone the repository.
2. docker-compose up -d.
3. Or:
   Run the application using your preferred IDE or through the command line:

   mvn spring-boot:run

4. The application will be available at `http://localhost:8080`.

## API Endpoints

### User Management

#### **Add a New User**

- **URL**: `/register`
- **Method**: `POST`
- **Description**: Adds a new user to the application.
- **Request Body**:
  json
  {
      "userName": "string",
      "password": "string"
  }
  

#### **Login**

- **URL**: `/login`
- **Method**: `POST`
- **Description**: Authenticates a user and returns a JWT token.
- **Request Parameters**:
  - `userName` (String): The username of the user.
  - `password` (String): The password of the user.
- **Response**:
  - `200 OK`: Returns an `AuthResponse` with the JWT token.
    json
  {
  "token": "string",
  }
  
  - `401 UNAUTHORIZED`: If the user is not authenticated.

### Contacts Management

#### **Get Contacts**

- **URL**: `/contacts`
- **Method**: `GET`
- **Description**: Retrieves a paginated list of contacts for the authenticated user.
- **Request Parameters**:
  - `token` (String): JWT token for authentication.
  - `page` (int): Page number (default: 0).
  - `size` (int): Number of contacts per page (default: 10, range: 1-10).
- **Response**: Returns a (possibly empty) paginated list of `ContactDocument` objects.
- {
  "content": [{
  "id": "66d76a234f073030534642d9",
  "firstName": "Eran1",
  "lastName": "Zahavi",
  "phoneNumber": "058",
  "address": "Hat",
  "userName": "rise"
  }, {
  "id": "66d76a274f073030534642da",
  "firstName": "Eran2",
  "lastName": "Zahavi",
  "phoneNumber": "058",
  "address": "Hat",
  "userName": "rise"
  }],
  "pageable": {
  "pageNumber": 0,
  "pageSize": 10,
  "sort": {
  "empty": true,
  "sorted": false,
  "unsorted": true
  },
  "offset": 0,
  "paged": true,
  "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 2,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
  "empty": true,
  "sorted": false,
  "unsorted": true
  },
  "numberOfElements": 2,
  "empty": false
  }

#### **Search Contacts**

- **URL**: `/search`
- **Method**: `GET`
- **Description**: Searches for contacts based on the first and last name for the authenticated user.
- **Request Parameters**:
  - `firstName` (String): First name to search for (optional).
  - `lastName` (String): Last name to search for (optional).
  - `token` (String): JWT token for authentication.
  - `page` (int): Page number (default: 0).
  - `size` (int): Number of contacts per page (default: 10, range: 1-10).
- **Response**: Returns a paginated list of `ContactDocument` objects that match the search criteria.
- see `/contacts`

#### **Add a Contact**

- **URL**: `/add-contact`
- **Method**: `POST`
- **Description**: Adds a new contact for the authenticated user.
- **Request Body**:
  json
  {
      "firstName": "string",
      "lastName": "string",
      "phoneNumber": "string",
      "email": "string"
  }
  
- **Request Parameters**:
  - `token` (String): JWT token for authentication.
- **Response**: Returns the added `ContactDocument` object.
- Returns 400 BAD_REQUEST - if contact is already in the database.

#### **Update a Contact**

- **URL**: `/edit-contact`
- **Method**: `PUT`
- **Description**: Updates an existing contact for the authenticated user.
- **Request Parameters**:
  - `id` (String): The ID of the contact to update.
  - `token` (String): JWT token for authentication.
- **Request Body**:
  json
  {
      "firstName": "string",
      "lastName": "string",
      "phoneNumber": "string",
      "email": "string"
  }
  
- **Response**: Returns the updated `ContactDocument` object.
- returns NOT_FOUND - in case no contact has this id.
- returns INTERNAL_SERVER_ERROR - in case of internal server error.

#### **Delete a Contact**

- **URL**: `/delete-contact`
- **Method**: `DELETE`
- **Description**: Deletes a contact by ID for the authenticated user.
- **Request Parameters**:
  - `id` (String): The ID of the contact to delete.
  - `token` (String): JWT token for authentication.
- **Response**: Returns OK. 
  - Returns NOT_FOUND - if no contact has the id.

## ContactDocument

The `ContactDocument` represents a contact entity that stores the following fields:

- `id` (String): Unique identifier for the contact.
- `userName` (String): The username of the user to whom the contact belongs.
- `firstName` (String): The first name of the contact.
- `lastName` (String): The last name of the contact.
- `phoneNumber` (String): The phone number of the contact.
- `address` (String): The home address of the contact.

## UserDocument

The `UserDocument` represents a user entity that stores the following fields:

- `userName` (String): The username of the user.
- `password` (String): The password of the user.

## Performance

- I made the app in a way that it can be instantiated multiple times on demand for scaling.
- I used mongodb because it can be expanded later both vertically and horizontally scaled, using several nodes with the shard feature on the userName key.
- I indexed the relevant DB keys in order to improve queries rate.
- I used JWT for authentication, in order to remain stateless but still not to query the DB for every request.
- I didn't use caching layer because phonebook isn't the kind of app that you want to access the same entries multiple times frequently, and it would have add complexity for the project.
- Logs are written ASYNC, to avoid a bottleneck.


## Logging

The application is configured to use Logstash and Elasticsearch for logging.
Logs are sent to a centralized logging system to support scalability and performance.
Currently it doesn't work, but an ASYNC local logging solution is working.

## Tests

The project contains several Integration tests. I didn't implement Junit logical due to lack of time, and the fact that the Classes don't have much logic in them.
Tests work with secondary "testdb", for a clean production db.


# flow example

This example demonstrates how the user "rise" can register, log in, and add a new contact using the API.



### 1. Register a New User

**Endpoint:** `POST /register`  

**Example Request:**
curl -X POST http://localhost:8080/register -H "Content-Type: application/json" -d '{
  "userName": "rise",
  "password": "pass123",
}'

### 2. Perform Login

**Endpoint:** `POST /login`
**Example Request:**
curl -X POST http://host:8080/login?userName=rise&password=pass123

**Expected Response:**
json
{
  "token": "your_jwt_token_here"
}
Save the token returned in the response.

### 3. Add a New Contact

**Endpoint:** `POST /add-contact`  
**Example Request:**
curl -X POST http://host:8080/add-contact?token=your_jwt_token_here -H "Content-Type: application/json" -d '{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "123-456-7890",
  "address": "Jerusalem, Israel"
}'

**Expected Response:**
{
  "id": "generated_contact_id",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "123-456-7890",
  "address": "Jerusalem, Israel",
  "userName": "rise"
}

---

