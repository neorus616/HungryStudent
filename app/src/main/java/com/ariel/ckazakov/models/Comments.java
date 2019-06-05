package com.ariel.ckazakov.models;

/**
 * This is a model class for comments of a recipe
 */
public class Comments {

    public String comment, date, time, username;

    /**
     * default constructor
     */
    public Comments() {

    }

    /**
     * constructor
     *
     * @param comment  - content of the comment
     * @param time     - time (HH:mm)
     * @param date     - date (dd-mm-YYYY)
     * @param username - full name of the publisher
     */
    public Comments(String comment, String date, String time, String username) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
