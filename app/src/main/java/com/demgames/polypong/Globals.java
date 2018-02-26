package com.demgames.polypong;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import netP5.NetAddress;
import oscP5.OscP5;

public class Globals extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        Globals.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Globals.context;
    }

    ArrayAdapter<String> arrayAdapter;

    private String myIpAdress;
    private int myPort=12000;
    String[] IpAdressen = new String[] {};

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private List<String> ip_list = new ArrayList<String>(Arrays.asList(IpAdressen));

    OscP5 oscP5;
    NetAddress myRemoteLocation;


    void setMyIpAdress(String myIpAdress_) {
        this.myIpAdress=myIpAdress_;
    }

    String getMyIpAdress() {
        return(this.myIpAdress);
    }

    void setMyPort(int myPort_) {
        this.myPort=myPort_;
    }

    int getMyPort() {
        return(this.myPort);
    }

    void setMyIpList(String[] IpAdressen_) {
        this.ip_list=new ArrayList<String>(Arrays.asList(IpAdressen_));
    }

    List<String> getMyIpList() {
        return(this.ip_list);
    }

    void setArrayAdapter(ArrayAdapter arrayAdapter_) {
        this.arrayAdapter=arrayAdapter_;
    }

    void addIpTolist(String IpAdress){
        if(!this.ip_list.contains(IpAdress)){
            this.ip_list.add(IpAdress);
        }
        this.arrayAdapter.notifyDataSetChanged();
    }
}

