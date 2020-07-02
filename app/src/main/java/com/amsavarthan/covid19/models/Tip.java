package com.amsavarthan.covid19.models;

public class Tip {

    private String title,content;
    private int image;

    public Tip(String title, String content, int image) {
        this.title = title;
        this.content = content;
        this.image = image;
    }

    public Tip() {
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
