package com.example.vcmessenger;

import java.util.ArrayList;

public class ChatRoom {
    String chatRoomId;
    ArrayList<String> members;
    String title;
    String chatRoomImageUrl;

    public ChatRoom() {
    }

    public ChatRoom(String chatRoomId, ArrayList<String> members) {
        this.chatRoomId = chatRoomId;
        this.members = members;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getChatRoomImageUrl() {
        return chatRoomImageUrl;
    }

    public void setChatRoomImageUrl(String chatRoomImageUrl) {
        this.chatRoomImageUrl = chatRoomImageUrl;
    }
}
