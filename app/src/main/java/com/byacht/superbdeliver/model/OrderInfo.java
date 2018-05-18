package com.byacht.superbdeliver.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dn on 2017/9/9.
 */

public class OrderInfo implements Parcelable{

    public OrderInfo(List<String> phoneNumberList, double xPoint, double yPoint) {
        this.phoneNumberList = phoneNumberList;
        mXPoint = xPoint;
        mYPoint = yPoint;
    }

    @SerializedName("phoneNumber")
    private List<String> phoneNumberList;
    private List<Integer> orderId;
    @SerializedName("xPoint")
    private double mXPoint;
    @SerializedName("yPoint")
    private double mYPoint;
    private String place;

    public List<String> getPhoneNumberList() {
        return phoneNumberList;
    }

    public void setPhoneNumberList(List<String> phoneNumberList) {
        this.phoneNumberList = phoneNumberList;
    }

    public List<Integer> getOrderId() {
        return orderId;
    }

    public void setOrderId(List<Integer> orderId) {
        this.orderId = orderId;
    }

    public double getXPoint() {
        return mXPoint;
    }

    public void setXPoint(double XPoint) {
        mXPoint = XPoint;
    }

    public double getYPoint() {
        return mYPoint;
    }

    public void setYPoint(double YPoint) {
        mYPoint = YPoint;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.phoneNumberList);
        dest.writeList(this.orderId);
        dest.writeDouble(this.mXPoint);
        dest.writeDouble(this.mYPoint);
        dest.writeString(this.place);
    }

    protected OrderInfo(Parcel in) {
        this.phoneNumberList = in.createStringArrayList();
        this.orderId = new ArrayList<Integer>();
        in.readList(this.orderId, Integer.class.getClassLoader());
        this.mXPoint = in.readDouble();
        this.mYPoint = in.readDouble();
        this.place = in.readString();
    }

    public static final Creator<OrderInfo> CREATOR = new Creator<OrderInfo>() {
        @Override
        public OrderInfo createFromParcel(Parcel source) {
            return new OrderInfo(source);
        }

        @Override
        public OrderInfo[] newArray(int size) {
            return new OrderInfo[size];
        }
    };
}
