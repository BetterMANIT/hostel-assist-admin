package com.manit.hostel.assist.data;
public class Entries {
    private String entryNo;
    private String name;
    private String roomNo;
    private String scholarNo;
    private String exitTime;
    private String entryTime = "";

    public boolean isBackInHostel() {
        return isBackInHostel;
    }

    private boolean isBackInHostel = true;

    public String getPhotoURL() {
        return photoURL;
    }

    private String photoURL;

    // Constructor
    public Entries(String entryNo,
                   String name,
                   String roomNo,
                   String scholarNo,
                   String exitTime,
                   String entryTime,
                   String photoURL) {
        this.entryNo = entryNo;
        this.name = name;
        this.roomNo = roomNo;
        this.scholarNo = scholarNo;
        this.exitTime = exitTime;
        isBackInHostel = !(entryTime == null || entryTime.isEmpty());
        this.entryTime = entryTime;
        this.photoURL = photoURL;
    }

    public Entries(String name) {
        this.entryNo = "24092024C12938";
        this.name = name;
        this.roomNo = "10C103";
        this.scholarNo = "241100112233";
        this.exitTime = "11:14 AM";
        this.entryTime = "";
        this.photoURL = "https://fadcdn.s3.ap-south-1.amazonaws.com/media/1216/Lead_image_57701.jpg";
    }

    // Getters and Setters
    public String getEntryNo() {
        return entryNo;
    }

    public void setEntryNo(String entryNo) {
        this.entryNo = entryNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getScholarNo() {
        return scholarNo;
    }

    public void setScholarNo(String scholarNo) {
        this.scholarNo = scholarNo;
    }

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }
}
