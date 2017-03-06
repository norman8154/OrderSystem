package com.norman.ordersystem;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by user on 2016/12/12.
 */

public class Order_info implements Parcelable{
    private String name;
    private String phone;
    private String addr;
    private String uid;
    private String time;
    private String approach;
    private HashMap<String,Integer> items;

    public Order_info(){

    }

    private Order_info(Parcel in) {
        this.name = in.readString();
        this.phone = in.readString();
        this.addr = in.readString();
        this.uid = in.readString();
        this.time = in.readString();
        this.approach = in.readString();
        in.readMap(this.items,Integer.class.getClassLoader());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUID() {
        return uid;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public HashMap<String,Integer> getItems() {
        return items;
    }

    public void setItems(HashMap<String,Integer> items) {
        this.items = items;
    }

    public String getApproach(){
        return approach;
    }

    public void setApproach(String approach){
        this.approach = approach;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.phone);
        dest.writeString(this.addr);
        dest.writeString(this.uid);
        dest.writeString(this.time);
        dest.writeString(this.approach);
        dest.writeMap(this.items);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Order_info createFromParcel(Parcel in) {
            return new Order_info(in);
        }

        public Order_info[] newArray(int size) {
            return new Order_info[size];
        }
    };
}
