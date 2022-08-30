package com.xar.lore.message;

import com.google.firebase.database.DatabaseReference;

import java.util.Date;

public class ChatMessage {

    private String messageText;
    private long messageTime;

    public ChatMessage(String messageText) {
        this.messageText = messageText;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public ChatMessage(){

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
}
