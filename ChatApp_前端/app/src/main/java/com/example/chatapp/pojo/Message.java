package com.example.chatapp.pojo;
public class Message {
    private String sender;//发送userName
    private String receiver;//接收userName
    private String content;//message内容
    private String timestamp;
    private boolean isread;
    private int isImage; // 是否为图片消息
    private boolean sentSuccess;

    public boolean isSentSuccess() {
        return sentSuccess;
    }

    public void setSentSuccess(boolean sentSuccess) {
        this.sentSuccess = sentSuccess;
    }

    public boolean isIsread() {
        return isread;
    }
    public int isImage(){return isImage;}

    public void setIsread(boolean isread) {
        this.isread = isread;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }
    public void setIsImage(int isImage){this.isImage = isImage;}
    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Message(String sender, String receiver, String content, String timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
        this.isImage=0;
    }
    public Message(String sender, String receiver, String content, String timestamp,int isImage) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
        this.isImage=isImage;
    }
}
