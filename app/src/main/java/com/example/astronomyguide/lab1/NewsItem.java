package com.example.astronomyguide.lab1;

public class NewsItem {
    private String title;
    private String content;
    private int likes;

    public NewsItem(String title, String content) {
        this.title = title;
        this.content = content;
        this.likes = 0;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getLikes() {
        return likes;
    }

    public void addLike() {
        this.likes++;
    }
}