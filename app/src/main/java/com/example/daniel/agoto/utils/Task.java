package com.example.daniel.agoto.utils;

/**
 * Created by Daniel on 6/2/2017.
 */

public class Task {

    private long id;
    private long server_id;
    private String name;
    private String body;
    private String solution;
    private boolean completed;
    private double longitude;
    private double latitude;
    private int score;

    public Task(long server_id, String name, String body, String solution, boolean completed, double longitude, double latitude, int score) {
        this.server_id = server_id;
        this.name = name;
        this.body = body;
        this.solution = solution;
        this.completed = completed;
        this.longitude = longitude;
        this.latitude = latitude;
        this.score = score;
    }

    public Task(long id, long server_id, String name, String body, String solution, boolean completed, double longitude, double latitude, int score) {

        this.id = id;
        this.server_id = server_id;
        this.name = name;
        this.body = body;
        this.solution = solution;
        this.completed = completed;
        this.longitude = longitude;
        this.latitude = latitude;
        this.score = score;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getId() {
        return id;
    }

    public long getServer_id() {
        return server_id;
    }

    public void setServer_id(long server_id) {
        this.server_id = server_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
