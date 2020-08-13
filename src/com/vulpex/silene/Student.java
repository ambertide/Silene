package com.vulpex.silene;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Student extends User {

    public Student(String username, String name, String authorisation_token, String locality) {
        super(username, name, authorisation_token, locality);
    }

    /**
     * Register a Student to the server, this DOES NOT return the registered user instance.
     * @param username Username to register.
     * @param name Name of the student.
     * @param password Password of the student.
     * @param locality Locality of the student.
     * @throws UserAlreadyExistsException If user already exists.
     * @throws Exception Any other exception.
     */
    public void registerStudent(String username, String name, String password, String locality) throws UserAlreadyExistsException, Exception {
        Map<String, String> userCredentials = new HashMap<String, String>();
        userCredentials.put("username", username);
        userCredentials.put("name", name);
        userCredentials.put("password", password);
        userCredentials.put("locality", locality);
        Gson gson = new Gson();
        String jsonInput = gson.toJson(userCredentials);
        Server.sendRequest("/api/register_student", "POST", "", jsonInput);
    }

}
