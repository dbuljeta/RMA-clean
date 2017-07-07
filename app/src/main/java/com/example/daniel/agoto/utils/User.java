package com.example.daniel.agoto.utils;

/**
 * Created by Daniel on 6/6/2017.
 */

public class User {

    private Long id;
    private String email;
    private Integer score;
    private Boolean isMe;

    public User(Long id, String email, Integer score, Boolean isMe) {
        this.id = id;
        this.email = email;
        this.score = score;
        this.isMe = isMe;
    }

    public User(String email, Integer score, Boolean isMe) {    
        this.email = email;
        this.score = score;
        this.isMe = isMe;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Boolean getMe() {
        return isMe;
    }

    public void setMe(Boolean me) {
        isMe = me;
    }
}
