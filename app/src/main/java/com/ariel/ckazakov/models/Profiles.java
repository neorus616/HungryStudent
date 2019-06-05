package com.ariel.ckazakov.models;

/**
 * This is a model class for Profile
 */
public class Profiles {
    public String profileimage, firstname, lastname;

    /**
     * default constructor
     */
    public Profiles() {
    }

    /**
     * constructor
     *
     * @param profileimage - profile image of the user
     * @param firstname    - first name of the user
     * @param lastname     - last name of the user
     */
    public Profiles(String profileimage, String firstname, String lastname) {
        this.profileimage = profileimage;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
