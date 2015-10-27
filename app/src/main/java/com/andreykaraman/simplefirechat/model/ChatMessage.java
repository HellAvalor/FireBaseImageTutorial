package com.andreykaraman.simplefirechat.model;

/**
 * Created by Andrew on 24.10.2015.
 */
public class ChatMessage {
    private String name;
    private String text;
    private String imageUrl;

    public ChatMessage() {
        // necessary for Firebase's deserializer
    }
    public ChatMessage(String name, String text, String imageUrl) {
        this.name = name;
        this.text = text;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}