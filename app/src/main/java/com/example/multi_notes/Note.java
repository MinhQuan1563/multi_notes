package com.example.multi_notes;

import java.sql.Timestamp;

public class Note {
    private int id;
    private String title, content, timestamp, reminder, images;
    public Note() {
    }

    public Note(int id, String title, String content, String timestamp, String reminder, String images) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.reminder = reminder;
        this.images = images;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

}
