package com.example.froject;

public class PostData {
    String title;
    String place;
    String date;

    public PostData() {}

    public PostData(String title, String place, String date) {
        this.title = title;
        this.place = place;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



}