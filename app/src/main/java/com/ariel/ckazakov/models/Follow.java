package com.ariel.ckazakov.models;

/**
 * This is a model class for Profile followers
 */
public class Follow {

    public String follow;

    /**
     * default constructor
     */
    public Follow() {
    }

    /**
     * constructor
     *
     * @param follow - boolean in string format(firebase purpose) when ever a user follows another user
     */
    public Follow(String follow) {
        this.follow = follow;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }
}
