# Simple HTTP Server User Guide

This project aims to simulate simple HTTP Server that can handle RESTful API requests
without using any external library(except Json Parser(Gson))


## Features

-   The server keeps records of `Student` instances where a Student entry consists of:
    -   studentId: Integer
    -   name: String
    -   age: Integer
    -   GPA: double
-   Supported RESTful requests:
    -   GET: All students
    -   GET with id: Returns a student with given id
    -   POST: Adds new student record
    -   PUT with id: Updates the student record
    -   DELETE with id: Deletes the student record

## External Dependencies

`Gson` library is used in this project for parsing and generating Json.

Maven page of Gson library: https://search.maven.org/artifact/com.google.code.gson/gson/2.10/jar

## Server Information

- URL: 127.0.0.1/students
- Supported operations:
    - GET all students: `127.0.0.1/students`
    - GET student with specific id = x: `127.0.0.1/students/x`
    - POST student in request body as JSON: `127.0.0.1/students` with attributes of:
        - studentId: integer
        - name: String
        - age: integer
        - GPA: double
    - PUT (Update) student with specific id = x: `127.0.0.1/students/x` by providing JSON in request body
    - DELETE student with specific id = x: `127.0.0.1/students/x`

## Example Test Scenario with CURL

1. GET all students when server started.
    ```
    curl 127.0.0.1/students/
    ```
    - Should see an `empty list` with success code `200`

2. POST a new student.
    ```
    curl --location --request POST '127.0.0.1/students' \
    --header 'Content-Type: application/json' \
    --data-raw '{
    "studentId": 1,
    "name": "John",
    "age": 25,
    "GPA": 3.13
    }'
    ```

    ```
    curl --location --request POST '127.0.0.1/students' \
    --header 'Content-Type: application/json' \
    --data-raw '{
    "studentId": 2,
    "name": "Alice",
    "age": 22,
    "GPA": 2.37
    }'
    ```
    - Should see `200` success code with a message `New student successfully created in server!`

3. GET all students
    ```
    curl 127.0.0.1/students/
    ```
    - Should see 2 student records as list

4. GET specific student with id
    ```
    curl 127.0.0.1/students/2
    ```
    - Should see the student named Alice

5. Try to POST student with same id
    ```
    curl --location --request POST '127.0.0.1/students' \
    --header 'Content-Type: application/json' \
    --data-raw '{
    "studentId": 1,
    "name": "John",
    "age": 25,
    "GPA": 3.13
    }'
    ```
    - Should see `400 Bad Request` with message of `The student with given id already exists in the server!`

6. Try to get student with id that does not exist in the server
    ```
    curl 127.0.0.1/students/5
    ```
    - Should see `400 Bad Request` with message of `Server could not find a student with id of 5`

7. Try to get student with invalid source
    ```
    curl 127.0.0.1/students/should/not/work
    ```
    - Should see `400 Bad Request` with message of `Invalid query with uri of /students/should/not/work. Server is unable to process the request!`

8. Update the age and GPA of student with id 2
    ```
    curl --location --request PUT '127.0.0.1/students/2' \
    --header 'Content-Type: application/json' \
    --data-raw '{
 	"studentId": 2,
    "name": "Alice",
    "age": 36,
    "GPA": 3.99
    }'
    ```
    - Should see `200 Success` with message of `Record of the given student updated!`

9. Get all students to see update worked correctly.
    ```
    curl 127.0.0.1/students/
    ```
    - Should see new age and GPA values for Alice

10. Try to update a student that does not exist in the server
    ```
    curl --location --request PUT '127.0.0.1/students/45' \
    --header 'Content-Type: application/json' \
    --data-raw '{
 	"studentId": 2,
    "name": "Alice",
    "age": 36,
    "GPA": 3.99
    }'
    ```
    - Should see `400 Bad Request` with message of `Server could not find a student with id of 45`

11. Try to delete a student with id 1
    ```
    curl --location --request DELETE '127.0.0.1/students/1'
    ```
    - Should see `200 OK` with message of `The student has been deleted from the records!`

12. Get all students to see if student was deleted successfully
    ```
    curl 127.0.0.1/students/
    ```
    - Should see only one student record.

13. Try to delete a student that does not exist
    ```
    curl --location --request DELETE '127.0.0.1/students/33'
    ```
    - Should see `400 Bad Request` with message of `Server could not find a student with id of 33`