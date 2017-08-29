package com.example.android.readnewsapp;

/**
 * Created by Kostas on 14/7/2017.
 * The News class holds the information for a News Article
 */

public class News {
    /**
     * Global Variables to store the relative information
     * for a news Article
     */
    private String mTitle;
    private String mSection;
    private String mDescription;
    private String mDateTime;
    private String mUrl;

    /**
     * Constructor - constructs a new {@link News} Object
     *
     * @param title       is the Title of the News Article     *
     * @param section     is the Section Name of the News Article
     * @param description is the description of the News Article
     * @param dateTime    is the Published Date and Time of the News Article
     * @param url         is the Web url for the News Article
     */
    public News(String title, String section, String description, String dateTime, String url) {
        mTitle = title;
        mSection = section;
        mDescription = description;
        mDateTime = dateTime;
        mUrl = url;
    }

    /**
     * Set Getters
     * Get (return) the Title of the News Article
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Get (return) the Section Name of the News Article
     */
    public String getSection() {
        return mSection;
    }

    /**
     * Get (return) the Description of the News Article
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Get (return) the Published Date & Time of the News Article
     */
    public String getDateTime() {
        return mDateTime;
    }

    /**
     * Get (return) the Web Url of the News Article
     */
    public String getUrl() {
        return mUrl;
    }
}
