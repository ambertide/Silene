package com.vulpex.silene;


import com.google.gson.Gson;

import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

enum UserType {
    STUDENT,
    TUTOR
}

class UserNotFoundException extends Exception {
    UserNotFoundException(String s) {
        super(s);
    }
}

class InvalidFilterException extends Exception {
    InvalidFilterException(String s) {
        super(s);
    }
}

/**
 * Defines a User.
 */
public abstract class User {
    private String username;
    private String name;
    protected String authorisation_token;
    private String locality;

    public User(String username, String name, String authorisation_token, String locality) {
        this.username = username;
        this.name = name;
        this.authorisation_token = authorisation_token;
        this.locality = locality;
    }

    /**
     * Get the lectures of the user.
     * @return A list of lectures.
     * @throws ServerNotInitialisedException In case server is not initialised.
     */
    public List<Lecture> getLecturesForUser() throws ServerNotInitialisedException, InvalidUserSessionException, Exception {
        SileneResponse sileneResponse = Server.sendRequest("/api/list_lectures", "GET",
                this.authorisation_token, "");
        Gson gson = new Gson();
        List<Lecture> lectures = new LinkedList<Lecture>();
        List<Map<String, Object>> lectureSpecs = gson.fromJson(sileneResponse.getJsonResponse(), List.class);
        for (Map<String, Object> lectureSpec : lectureSpecs) {
            Lecture lecture = new Lecture((int) lectureSpec.get("obj_id"), (String) lectureSpec.get("tutor"),
                    (String) lectureSpec.get("student"),
                    LectureState.valueOf(lectureSpec.get("state").toString().toUpperCase()),
                    Date.valueOf((String) lectureSpec.get("time")));
            lectures.add(lecture);
        }
        return lectures;
    }

    /**
     * Search for tutors given filters.
     * @param locality Locality of the search.
     * @param expertise Expertise of the tutor.
     * @return The list of tutors.
     * @throws Exception Any unexpected exception.
     * @throws UnsatisfiableCriteriaException If a tutor fitting provided filters are not found.
     * @throws InvalidJSONException If the provided JSON string is invalid.
     */
    public List<Tutor> searchForTutors(String locality, String expertise) throws Exception,
            UnsatisfiableCriteriaException, InvalidJSONException {
        if (locality.isEmpty() && expertise.isEmpty()) {
            throw new InvalidFilterException("At least one filter must be provided.");
        }
        Map<String, String> filter = new HashMap<String, String>();
        if (!locality.isEmpty()) {
            filter.put("locality", locality);
        }

        if (!expertise.isEmpty()) {
            filter.put("expertise", expertise);
        }
        Gson gson = new Gson();
        String jsonInput = gson.toJson(filter);
        SileneResponse response = Server.sendRequest("/api/find_tutors", "POST",
                this.authorisation_token, jsonInput);

        HashMap kvPairs = gson.fromJson(response.getJsonResponse(), HashMap.class);
        List<Map<String, String>> tutorCredentials = (List<Map<String, String>>) kvPairs.get("response");
        List<Tutor> tutors = new LinkedList<Tutor>();
        for (Map<String, String> credentials : tutorCredentials) {
            tutors.add(
                    new Tutor(credentials.get("username"), credentials.get("name"), null, credentials.get("locality"),
                            credentials.get("allowed_weekdays"), credentials.get("expertise"))
            );
        }
        return tutors;
    }

    /**
     * Get the type of the user given its login token.
     * @param username Username of the user.
     * @return The user type.
     * @throws Exception Any unexpected exception due to server.
     */
    private static UserType getUserType(String username) throws Exception {
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", username);
        SileneResponse response = Server.sendRequest("/api/get_user_type", "POST",
                "", gson.toJson(map));
        return UserType.valueOf(response.getJsonResponse().toUpperCase());
    }

    /**
     * Get user credentials from server.
     * @param username Username of the user.
     * @return The user credentials in a map.
     * @throws Exception Any unexpected exception.
     */
    public static Map<String, String> getUserProfile(String username) throws Exception {
        Gson gson = new Gson();
        Map<String, String> argumentMap = new HashMap<String, String>();
        argumentMap.put("username", username);
        SileneResponse response = Server.sendRequest("/api/get_user_profile", "POST",
                "", gson.toJson(argumentMap));
        HashMap<String, String> userCredentials = (HashMap) gson.fromJson(response.getJsonResponse(), HashMap.class);
        return userCredentials;
    }

    /**
     * Create a student object.
     * @param username Username of the user..
     * @return the student object.
     * @throws Exception Any unexpected exception.
     */
    private static Student createStudentFrom(String username) throws Exception {
        Map<String, String> userCredentials = getUserProfile(username);
        return new Student(userCredentials.get("username"), userCredentials.get("name"),
                "", userCredentials.get("locality"));
    }

    /**
     * Create a Tutor object given auth token.
     * @param username Username of the user.
     * @return the created Tutor object.
     * @throws Exception Any unexpected exception.
     */
    private static Tutor createTutorFrom(String username) throws Exception {
        Map<String, String> userCredentials = getUserProfile(username);
        return new Tutor(userCredentials.get("username"), userCredentials.get("name"),
                "", userCredentials.get("locality"),
                userCredentials.get("allowed_weekdays"), userCredentials.get("expertise"));
    }

    /**
     * Given a username, return the user.
     * @param username Username of the user.
     * @return the User with this username if it exists.
     */
    public static User getUser(String username) throws Exception {
        try {
            switch (getUserType(username)) {
                case STUDENT:
                    return createStudentFrom(username);
                case TUTOR:
                    return createTutorFrom(username);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Login a user, and return its representation as a local object.
     * @param username Username of the user.
     * @param password Password of the user.
     * @return the User.
     * @throws Exception Any unexpected exception.
     * @throws ServerException Thrown in case something goes wrong with Server.
     * @throws UserNotFoundException Thrown in case when User is not found or if Usertype is unexpected.
     */
    public static User loginUser(String username, String password) throws Exception, ServerException, UserNotFoundException {
        Map<String, String> loginJson = new HashMap<String, String>();
        loginJson.put("username", username);
        loginJson.put("password", password);
        Gson gson = new Gson();
        String json = gson.toJson(loginJson, HashMap.class);
        SileneResponse response = Server.sendRequest("/api/login",
                "POST", "", json);
        HashMap<String, String> authJson = gson.fromJson(response.getJsonResponse(), HashMap.class);
        String access_token = authJson.get("access_token");
        User user;
        if ((user = getUser(username)) != null) {
            return user;
        } else {
            throw new UserNotFoundException("Invalid user type.");
        }
    }

    public String getUsername() {return this.username;}
    public String getLocality() {return this.locality;}
    public String getName() {return this.name;}
}
