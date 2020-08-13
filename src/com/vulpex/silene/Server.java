package com.vulpex.silene;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * This exception indicates a problem with the Server.
 * Acts as a wrapper to low level exceptions.
 */
class ServerException extends Exception {
    ServerException(String s) {
        super(s);
    }
}


/**
 * This exception indicates a problem with the API key.
 */
class APIAuthorizationException extends Exception {
    APIAuthorizationException(String s) {
        super(s);
    }
}

/**
 * This exception indicates that the server config has not been
 * initialised yet.
 */
class ServerNotInitialisedException extends Exception {
    ServerNotInitialisedException(String s) {
        super(s);
    }
}

/**
 * This exceptions indicates the JSON string is malformed.
 */
class InvalidJSONException extends Exception {
    InvalidJSONException(String s) { super(s); }
}

/**
 * Thrown in case the user attempted to be registered already exists.
 */
class UserAlreadyExistsException extends Exception {
    UserAlreadyExistsException(String s) { super(s); }
}

/**
 * Search or demanded criteria is unsatisfiable.
 */
class UnsatisfiableCriteriaException extends Exception {
    UnsatisfiableCriteriaException(String s) { super(s); }
}

/**
 * Authorization user session expired or otherwise invalid.
 */
class InvalidUserSessionException extends Exception {
    InvalidUserSessionException(String s) { super(s); }
}

/**
 * Defines the configuration for the server. This is a singleton, so each app
 * may only have one server configuration and therefore connection.
 */
public class Server {
    private final String url;
    private final String api_key;
    private static Server instance;

    /**
     * Initialise a ServerConfig.
     * @param url URL of the server.
     * @param api_key APIKey of the server.
     */
    private Server(String url, String api_key) throws MalformedURLException {
        if (!url.contains("://")) {
            throw new MalformedURLException("URL does not contain proper protocole, ie: http, https etc.");
        }
        this.url = url;
        this.api_key = api_key;
    }

    /**
     * Set up the Server configuration.
     * @param url URL of the server.
     * @param api_key API key of the client.
     */
    public static void setUpServer(String url, String api_key) throws MalformedURLException {
        instance = new Server(url, api_key);
    }

    /**
     * Return the global server instance.
     * @return the server instance.
     */
    static Server getServer() throws ServerNotInitialisedException {
        if (instance == null) {
            throw new ServerNotInitialisedException("setUpServer must be called first.");
        }
        return instance;
    }

    /**
     * Send an input through a connection.
     * @param con HTTP connection.
     * @param input Input to sent.
     * @throws IOException According to official Javadoc this may occur.
     */
    private static void sendInput(HttpURLConnection con, String input) throws IOException {
        try (OutputStream outputStream = con.getOutputStream()) {
            byte[] inputBytes = input.getBytes();
            outputStream.write(inputBytes, 0, inputBytes.length);
        }
    }

    /**
     * Get the output from the body of the response.
     * @param con Connection in http.
     * @return the output JSON string.
     * @throws IOException According to official Javadoc this may occur.
     */
    private static String getOutput(HttpURLConnection con) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
        )) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    /**
     * Send a request to the given URI. Return its response.
     * @param api_uri URI to send request to, EXCLUDING the server ip
     * @param method HTTP Method of the request: GET, POST, PUT, DELETE ... etc
     * @param authorization Authorization token of the User.
     * @param input Input to be sent to the server.
     * @return the Response from the server.
     * @throws ServerNotInitialisedException Raised in case .setUpServer() is not called prior.
     * @throws ServerException An exception occurred in the server.
     * @throws InvalidJSONException JSON provided is wrong.
     * @throws APIAuthorizationException API Key is invalid.
     * @throws UserAlreadyExistsException User already exists
     * @throws UnsatisfiableCriteriaException Criteria for search or creation unsatisfiable.
     * @throws InvalidUserSessionException User session corrupt.
     */
    static SileneResponse sendRequest(String api_uri, String method,
                                      String authorization, String input) throws InvalidJSONException, APIAuthorizationException,
            UserAlreadyExistsException, UnsatisfiableCriteriaException, InvalidUserSessionException, ServerException, ServerNotInitialisedException{
        try {
            Server server = getServer();
            URL url = new URL(server.url + api_uri + '/' + server.api_key);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Accept", "application/json");
            if (!authorization.isEmpty()) { // If given authorization token.
                con.setRequestProperty("Authorization", "Bearer " + authorization); // Set token.
            }
            if (input != null && !input.isEmpty()) {
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                sendInput(con, input);
            }
            String jsonResponse = getOutput(con);
            SileneResponse response = new SileneResponse(con.getResponseCode(), jsonResponse) ;
            if (!response.isSuccessful()) {
                switch (response.getStatusCode()) {
                    case 400:
                        throw new InvalidJSONException("JSON format is invalid.");
                    case 401:
                        throw new APIAuthorizationException("Authentication failed: API Key or user" +
                                "credentials might be wrong or not enough to access this resource.");
                    case 403:
                        throw new UserAlreadyExistsException("User with this username already" +
                                "exists.");
                    case 412:
                        throw new UnsatisfiableCriteriaException("Criteria requested not satisfiable.");
                    case 422:
                        throw new InvalidUserSessionException("Invalid user session.");
                    default:
                        throw new ServerException("An unexpected exception occured.");
                }
            } else {
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerException("Connection closed.");
        }
    }

    /**
     * Check if the server accepting connections.
     * @return True if the server accepts connections false if not.
     * @throws ServerException in case a connection error occurs in server.
     * @throws APIAuthorizationException in case provided API Key is wrong.
     * @throws Exception may occur.
     */
    public boolean isReady() throws APIAuthorizationException, ServerException, Exception {
        SileneResponse response = sendRequest("", "GET", "", "");
        return true;
    }

    /**
     * Get the server configuration.
     * @return the Server configuration as a map.
     * @throws Exception If an unexpected exception occurs.
     */
    public Map<String, String> getServerConfiguration() throws Exception {
        SileneResponse response = sendRequest("/api", "GET", "", "");
        String jsonString = response.getJsonResponse();
        Gson gsonObj = new Gson();
        Map<String, String> values = gsonObj.fromJson(jsonString, HashMap.class);
        return values;
    }
}
