package com.norman.ordersystem;

/**
 * Created by user on 2016/11/29.
 */

public class User {

    public String name;
    public String email;
    public String phone;
    public String addr;
    public int isAdmin;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String name, String email,String phone,String addr) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.addr = addr;
        this.isAdmin = 0;
    }
}
