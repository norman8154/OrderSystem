package com.norman.ordersystem;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 2016/12/10.
 */

public class Item implements Parcelable{
    private String name;
    private String price;
    private String URL;
    private String description;
    private String remain;

    public Item(){

    }

    public Item(Parcel in) {
        String[] data = new String[1];
        in.readStringArray(data);
        this.name = data[0];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemain() {
        return remain;
    }

    public void setRemain(String remain){
        this.remain = remain;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] { this.name });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}
