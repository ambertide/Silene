package com.vulpex.silene;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class SileneResponse {
    private int statusCode; // HTTP Status Code.
    private String jsonResponse;
    /**
     * Initialise a Seline response.
     * @param statusCode Code of the response
     * @param jsonResponse JSON of the response.
     */
    SileneResponse(int statusCode, String jsonResponse) {
        this.jsonResponse = jsonResponse;
        this.statusCode = statusCode;
    }

    /**
     * Get the Status code of the response.
     * @return The status code of the response.
     */
    int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Get the JSON response, unparsed.
     * @return Get the JSON response.
     */
    String getJsonResponse() {return this.jsonResponse;}

    /**
     * Check if the response is a successful one,
     * this can be determined if the status code is
     * in 200s range.
     * @return If the response successful.
     */
    boolean isSuccessful() {
        return statusCode <= 299 && statusCode >= 200;
    }
}
