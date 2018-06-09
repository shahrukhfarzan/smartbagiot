package com.example.hackdroid.smartbag;

public class Bag {
    private String Image, Name,Value;
    public Bag(){

    }

    public Bag(String image, String name, String value) {
        Image = image;
        Name = name;
        Value = value;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
