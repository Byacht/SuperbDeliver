package com.byacht.superbdeliver.model;

import com.amap.api.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dn on 2017/9/9.
 */

public class OrderInfo implements Serializable{

    private LatLng mLatLng;
    private List<String> phoneNumberList;

    public OrderInfo(LatLng latLng, List<String> phoneNumberList) {
        this.mLatLng = latLng;
        this.phoneNumberList = phoneNumberList;
    }

    public List<String> getPhoneNumberList() {
        return phoneNumberList;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }
}
