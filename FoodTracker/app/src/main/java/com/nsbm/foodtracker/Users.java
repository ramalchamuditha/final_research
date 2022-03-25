package com.nsbm.foodtracker;

public class Users {
    private String userName;
    private String userEmail;
    private String userID;
    private String userPhone;
    private String loginPassword;
    private String userProfile;

    public Users() {
    }

    public Users(String userName, String userEmail, String userID, String userPhone, String loginPassword, String userProfile) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userID = userID;
        this.userPhone = userPhone;
        this.loginPassword = loginPassword;
        this.userProfile = userProfile;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }
}
