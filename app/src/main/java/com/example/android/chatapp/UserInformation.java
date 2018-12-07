package com.example.android.chatapp;

public class UserInformation {
    public String name ;
    public String email ;
    public String password ;

    public UserInformation(){}

    public UserInformation(String name , String email,String password ) {
        this.name = name;
        this.email = email;
        this.password = password ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordl() {
        return email;
    }

    public void setPassword(String email) {
        this.email = email;
    }
}
