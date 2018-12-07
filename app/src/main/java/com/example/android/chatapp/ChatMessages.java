package com.example.android.chatapp;

import java.util.Date;

public class ChatMessages {

    private String messageText ;
    private long messageTime ;
    private String photoUrl;

    public ChatMessages(){}

    public ChatMessages(String messageText, long messageTime, String photoUrl) {
        this.messageText = messageText;
        this.messageTime = messageTime;
        this.photoUrl = photoUrl;

      //  messageTime = new Date().getTime();
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
