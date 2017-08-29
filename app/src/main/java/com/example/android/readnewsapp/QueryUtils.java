package com.example.android.readnewsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kostas on 15/7/2017.
 */

public class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Static final int values for HTTP request & response
     */
    private static final int READ_TIME_OUT = 10000; // milliseconds
    private static final int CONNECT_TIME_OUT = 15000; // milliseconds
    private static final int RESPONSE_CODE_SUCCESS = 200;

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link News} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<News> extractNewsFromJson(String newsJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding News to
        List<News> newsArticles = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            // Extract the JSONObject associated with the key called "response"
            JSONObject response = baseJsonResponse.getJSONObject("response");

            // If the JSONObject has results (or News Articles)
            // Extract the JSONArray with the key called "results"
            if (response.has("results")) {
                JSONArray newsArray = response.getJSONArray("results");
                int count = newsArray.length();

                // For each News in the NewsArray, create an {@link News} object
                for (int i = 0; i < count; i++) {

                    // Get a single News Article at position i within the list of News Articles
                    JSONObject currentNews = newsArray.getJSONObject(i);

                    // Extract the value from the key called webTitle
                    String title = "";
                    if (currentNews.has("webTitle")) {
                        title = currentNews.getString("webTitle");
                    }

                    // Extract the value from the key called sectionName
                    String section = "";
                    if (currentNews.has("sectionName")) {
                        section = currentNews.getString("sectionName");
                    }

                    // Check if the news article has a description
                    // and extract the value from the key called trailText if there is one
                    String description = "";
                    if (currentNews.has("fields")) {
                        JSONObject fields = currentNews.getJSONObject("fields");
                        if (currentNews.has("fields")) {
                            description = fields.getString("trailText");
                        }
                    }

                    // Extract the Date & Time from the key called webPublicationDate
                    String dateTime = "";
                    if (currentNews.has("webPublicationDate")) {
                        // after extracting the value,  Remove T & Z from the date
                        dateTime = currentNews.getString("webPublicationDate").replace("T", " ").replace("Z", "");
                        // Remove also the last 3 characters (seconds) from the time (":09")
                        dateTime = dateTime.substring(0, dateTime.length() - 3);
                    }

                    // Extract the url from the key called webUrl
                    String url = "";
                    if (currentNews.has("webUrl")) {
                        url = currentNews.getString("webUrl");
                    }

                    // Create a new {@link News} object with the title, section, description, dateTime
                    // and url from the JSON response.
                    News news = new News(title, section, description, dateTime, url);

                    // Add the new {@link News} Object to the list of earthquakes.
                    newsArticles.add(news);
                }
            } else {
                Log.v(LOG_TAG, "No results found");
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the News Article JSON results", e);
        }

        // Return the list of News Articles
        return newsArticles;
    }

    /**
     * Query the Guardian API dataset and return a list of {@link News} objects.
     */
    public static List<News> fetchNewsData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Return the list of {@link News} Articles
        return extractNewsFromJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIME_OUT /* milliseconds */);
            urlConnection.setConnectTimeout(CONNECT_TIME_OUT /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == RESPONSE_CODE_SUCCESS) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the News Article JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
