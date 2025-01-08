package com.example.vcmessenger;

import java.util.Date;

public class msgModelclass {
    String message;
    String senderUid;
    String reciverUid;
    Date timeStamp;

    public msgModelclass() {
    }

    public msgModelclass(String message, String senderUid, Date timeStamp) {
        this.message = message;
        this.senderUid = senderUid;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReciverUid() {
        return reciverUid;
    }
}