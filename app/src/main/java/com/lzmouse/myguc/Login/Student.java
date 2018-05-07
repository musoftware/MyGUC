package com.lzmouse.myguc.Login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Ahmed Ali on 4/12/2018.
 */

public class Student {
    private static final Student ourInstance =  new Student();

    public static Student getInstance(String username,String password) {
        ourInstance.password=password;
        ourInstance.username=username;
        return ourInstance;
    }
    public @Nullable
    static Student getInstance() {
       if(ourInstance.username == null)
           return null;
       return ourInstance;
    }

    private String username,password;

    private Student()
    {
    }
    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
