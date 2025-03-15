package com.example.vcmessenger;

import java.util.Date;

public class msgModelclass {
    String message;
    String senderUid;
    String reciverUid;
    Date timeStamp;
    String userName;

    public msgModelclass() {
    }

    public msgModelclass(String message, String senderUid, Date timeStamp, String userName) {
        this.message = message;
        this.senderUid = senderUid;
        this.timeStamp = timeStamp;
        this.userName = userName;
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

    public void setReciverUid(String reciverUid) {
        this.reciverUid = reciverUid;
    }

    public String getUserName() {
        return userName;
    }
}