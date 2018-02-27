package com.demgames.polypong;

import android.app.Application;
import android.widget.ArrayAdapter;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import netP5.*;
import oscP5.*;

public class Globals extends Application {


    private String myIpAdress;
    private String remoteIpAdress;
    private int myPort=12000;

    //arrayadapter for updating list of listview
    private ArrayAdapter<String> arrayAdapter;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private List<String> ipAdressList = new ArrayList<String>(Arrays.asList(new String[] {}));

    private OscP5 oscP5;

    private boolean connectState=false;
    private boolean readyState=false;
    private boolean settingsState=false;
    private boolean gameLaunched=false;

    private String numberOfBalls=null;
    private List<String> playerNamesList=new ArrayList<String>(Arrays.asList(new String[] {}));

    private Thread updateThread;


    //---------------NETWORKING-------------------

    void setMyIpAdress(String myIpAdress_) {
        this.myIpAdress=myIpAdress_;
    }

    String getMyIpAdress() {
        return(this.myIpAdress);
    }

    //----------------------------------

    void setRemoteIpAdress(String remoteIpAdress_) {
        this.remoteIpAdress=remoteIpAdress_;
    }

    String getRemoteIpAdress() {
        return(this.remoteIpAdress);
    }

    //----------------------------------

    void setMyPort(int myPort_) {
        this.myPort=myPort_;
    }

    int getMyPort() {
        return(this.myPort);
    }

    //----------------------------------

    void setConnectState(boolean connectState_) {
        this.connectState=connectState_;
    }

    boolean getConnectState() {
        return(this.connectState);
    }



    void setSettingsState(boolean settingsState_) {
        this.settingsState=settingsState_;
    }

    boolean getSettingsState() {
        return(this.settingsState);
    }

    //----------------------------------


    void setReadyStateState(boolean readyState_) {
        this.readyState=readyState_;
    }

    boolean getReadyState() {
        return(this.readyState);
    }

    //----------------------------------

    void setGameLaunched(boolean gameLaunched_) {
        this.gameLaunched=gameLaunched_;
    }

    boolean getGameLaunched() {
        return(this.gameLaunched);
    }

    //----------------------------------

    void setMyIpList(String[] IpAdresses_) {
        this.ipAdressList=new ArrayList<String>(Arrays.asList(IpAdresses_));
    }

    List<String> getMyIpList() {
        return(this.ipAdressList);
    }


    //----------------------------------

    void setOscP5(OscP5 oscP5_) {
        this.oscP5=oscP5_;
    }

    OscP5 getOscP5() {
        return(this.oscP5);
    }

    void stopOscP5() {
        this.oscP5.stop();
    }




    //----------------LISTVIEW------------------

    void setArrayAdapter(ArrayAdapter arrayAdapter_) {
        this.arrayAdapter=arrayAdapter_;
    }

    ArrayAdapter getArrayAdapter() {
        return(this.arrayAdapter);
    }





    //---------------LIST MANIPULATION-------------------

    void addIpTolist(String IpAdress){
        if(!this.ipAdressList.contains(IpAdress)){
            this.ipAdressList.add(IpAdress);
        }

        this.arrayAdapter.notifyDataSetChanged();
    }

    //----------------------------------

    void addPlayerNameTolist(String newPlayerName){
        if(!this.playerNamesList.contains(newPlayerName)){
            this.playerNamesList.add(newPlayerName);
        }
    }




    //----------------THREAD------------------

    void setUpdateThread(Runnable myRunnable) {
        updateThread = new Thread(myRunnable);
    }

    Thread getUpdateThread() {
        return(this.updateThread);
    }

    void interruptUpdateThread() {
        this.updateThread.interrupt();
    }

    //----------------GAMEVARIABLES------------------

    void setNumberOfBalls(String numberOfBalls_) {
        this.numberOfBalls=numberOfBalls_;
    }

    String getNumberOfBalls() {
        return(this.numberOfBalls);
    }

    //----------------------------------

    void setPlayerNamesList(String[] playerNames_) {
        this.playerNamesList=new ArrayList<String>(Arrays.asList(playerNames_));
    }

    List<String> getPlayerNamesList() {
        return(this.playerNamesList);
    }
}

