package com.android.miki.quickly.models;

import java.io.Serializable;

/**
 * Created by mpokr on 5/29/2017.
 */

public class Gif implements Serializable{

    private String url;
    private int width;
    private int height;


    public Gif() {

    }

    public Gif(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
