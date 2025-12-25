package com.amazi.model;

public class Portfolio {
    private String title;
    private String description; // For the text part
    private String filePath;    // For the image or file path
    private String category;
    private String status;

    // The Constructor: This is how you "build" a new portfolio object
    public Portfolio(String title, String description, String filePath, String category, String status) {
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.category = category;
        this.status = status;
    }

    // Getters: These allow the Controller to "read" the data
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFilePath() { return filePath; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
}