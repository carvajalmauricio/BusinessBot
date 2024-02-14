package com.innovaweb.businessbot;

public class Users {

    String userId, name, profile, businessName , offer ;

    public Users(String userId, String name, String profile,String businessName, String offer ) {
        this.userId = userId;
        this.name = name;
        this.profile = profile;
        this.businessName = businessName;
        this.offer = offer;
    }

    public Users(){

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }
}
