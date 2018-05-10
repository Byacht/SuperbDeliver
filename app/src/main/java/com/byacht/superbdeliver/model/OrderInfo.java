package com.byacht.superbdeliver.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
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

    private List<String> phoneNumberList;
    @SerializedName("xpoint")
    private double mXPoint;
    @SerializedName("ypoint")
    private double mYPoint;

    public List<String> getPhoneNumberList() {
        return phoneNumberList;
    }

    public void setPhoneNumberList(List<String> phoneNumberList) {
        this.phoneNumberList = phoneNumberList;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.phoneNumberList);
        dest.writeDouble(this.mXPoint);
        dest.writeDouble(this.mYPoint);
    }

    protected OrderInfo(Parcel in) {
        this.phoneNumberList = in.createStringArrayList();
        this.mXPoint = in.readDouble();
        this.mYPoint = in.readDouble();
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
