package com.example.chatapp.pojo;

public class Contact {
    String username;
    boolean hasUnreadMessages;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public boolean hasUnreadMessages(){
        return hasUnreadMessages;
    }

    public Contact(String username, boolean hasUnreadMessages) {
        this.username = username;
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public Contact(String username) {
        this.username = username;
    }
    public void setHasUnreadMessages(boolean flag){
        hasUnreadMessages=flag;
    }
    @Override
    public String toString() {
        return "Contact{" +
                "username='" + username + '\'' +
                '}';
    }
}
