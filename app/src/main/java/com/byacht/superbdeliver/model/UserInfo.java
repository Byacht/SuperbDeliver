package com.byacht.superbdeliver.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dn on 2018/5/3.
 */

public class UserInfo {
    @SerializedName("userId")
    private int id;
    private String name;
    @SerializedName("photo")
    private String portrait;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    private String phoneNumber;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }
}
