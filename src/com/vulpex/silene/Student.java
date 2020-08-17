package com.vulpex.silene;

import com.google.gson.Gson;

import java.util.Date;
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
        Server.sendRequest("/api/register_student", "PUT", "", jsonInput);
    }

    /**
     * Request a lecture from the server between this user and tutor.
     * @param tutorUsername Username of the tutor.
     * @param scheduled Scheduled date of the lecture.
     * @throws InvalidUserSessionException When user session has expired.
     * @throws APIAuthorizationException When API key fails to authorise.
     * @throws UnsatisfiableCriteriaException When the tutor's days are filled.
     * @throws ServerNotInitialisedException When the server is not initialised.
     * @throws ServerException When an unexpected error occurs on Server.
     * @throws Exception When any unexpected error occurs.
     */
    public void requestLecture(String tutorUsername, Date scheduled) throws InvalidUserSessionException, APIAuthorizationException,
            UnsatisfiableCriteriaException, ServerNotInitialisedException,
            ServerException, Exception {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<String, Object>(); // Since Java does not have a functional
        // Type system...
        map.put("tutor_username", tutorUsername);
        map.put("scheduled", scheduled.getTime());
        String jsonInput = gson.toJson(map);
        Server.sendRequest("/api/request_lecture", "PUT", this.authorisation_token, jsonInput);
    }

}
