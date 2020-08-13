package com.vulpex.silene;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Tutor extends User {
    private String allowedWeekdays;
    private String expertise;

    public Tutor(String username, String name,
                 String authorisation_token, String locality, String allowedWeekdays, String expertise) {
        super(username, name, authorisation_token, locality);
        this.allowedWeekdays = allowedWeekdays;
        this.expertise = expertise;
    }

    public String getExpertise() {
        return expertise;
    }

    /**
     * Register a Tutor to the server, this DOES NOT return the registered user instance.
     * @param username Username to register.
     * @param name Name of the student.
     * @param password Password of the student.
     * @param locality Locality of the student.
     * @param expertise Expertise of the tutor.
     * @param allowedWeekdays Weekdays the tutor is available in a string like 023 (Sunday Tuesday Wednesday.)
     * @throws UserAlreadyExistsException If user already exists.
     * @throws Exception Any other exception.
     */
    public void registerTutor(String username, String name, String password, String locality,
                              String expertise, String allowedWeekdays) throws UserAlreadyExistsException, Exception {
        Map<String, String> userCredentials = new HashMap<String, String>();
        userCredentials.put("username", username);
        userCredentials.put("name", name);
        userCredentials.put("password", password);
        userCredentials.put("locality", locality);
        userCredentials.put("expertise", expertise);
        userCredentials.put("allowedWeekdays", allowedWeekdays);
        Gson gson = new Gson();
        String jsonInput = gson.toJson(userCredentials);
        Server.sendRequest("/api/register_tutor", "POST", "", jsonInput);
    }
}
