package com.fallntic.ridesharepassenger;

import java.io.Serializable;

public class User implements Serializable {

    private boolean authorized;
    private String role;
    private int id;
    private String name;
    private String email;
    private String phoneNumber;
    private String rating;
    private String created_at;
    private String updated_at;
    private int authenticated;

    public User(boolean authorized, String role, int id, String name, String email,
                String phoneNumber, String rating, String created_at,
                String updated_at, int authenticated) {

        this.authorized = authorized;
        this.role = role;
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.rating = rating;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.authenticated = authenticated;
    }

    public User(){}

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }


    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(int authenticated) {
        this.authenticated = authenticated;
    }

}
