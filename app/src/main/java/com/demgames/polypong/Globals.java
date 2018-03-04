package com.demgames.polypong;

import android.app.Application;
import android.nfc.Tag;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import netP5.*;
import oscP5.*;

public class Globals extends Application {

    //Variablen
    private String myIpAdress;
    private String remoteIpAdress;
    private String numberOfBalls=null;
    private int myPort=12000;
    private float friction=(float)0.0;

    private OscP5 oscP5;

    private boolean connectState=false;
    private boolean readyState=false;
    private boolean settingsState=false;
    private boolean gameLaunched=false;
    private boolean attraction=false;
    private boolean gravity=false;

    private static final String TAG = "Globals";


    //arrayadapter for updating list of listview
    private ArrayAdapter<String> arrayAdapter;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private List<String> ipAdressList = new ArrayList<String>(Arrays.asList(new String[] {}));

    private List<String> playerNamesList=new ArrayList<String>(Arrays.asList(new String[] {}));

    private Thread updateThread;

    private float[] ballsXPositions;
    private float[] ballsYPositions;
    private float[] ballsSizes;


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
        updateList();
        //Updatet die Liesview wenn eine neue IP Adresse gefunden wird

    }

    //----------------------------------

    void addPlayerNameTolist(String newPlayerName){
        if(!this.playerNamesList.contains(newPlayerName)){
            this.playerNamesList.add(newPlayerName);
        }
    }

    void updateList(){
        this.arrayAdapter.notifyDataSetChanged();
        Log.d(TAG, "addIpTolist: IP wurde hinzugefügt++++++++++++++++++++++++++++++++++++++++++++");
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

    //----------------------------------

    void setAttractionState(Boolean attraction_) {
        this.attraction=attraction_;
    }

    Boolean getAttractionState() {
        return(this.attraction);
    }

    //----------------------------------

    void setGravityState(Boolean gravity_) {
        this.gravity=gravity_;
    }

    Boolean getGravityState() {
        return(this.gravity);
    }

    //----------------------------------

    void setFriction(Float friction_) {
        this.friction=friction_;
    }

    Float getFriction() {
        return(this.friction);
    }

    //----------------------------------

    void setBalls(boolean randomPosition) {
        Random rand=new Random();
        this.ballsXPositions=new float[Integer.parseInt(this.getNumberOfBalls())];
        this.ballsYPositions=new float[Integer.parseInt(this.getNumberOfBalls())];
        this.ballsSizes=new float[Integer.parseInt(this.getNumberOfBalls())];
        if(randomPosition) {
            for (int i = 0; i < Integer.parseInt(this.getNumberOfBalls()); i++) {
                this.ballsXPositions[i] = rand.nextFloat();
                this.ballsYPositions[i] = rand.nextFloat();
                this.ballsSizes[i] = rand.nextFloat();
            }
        }
    }

    void setBallsXPositions(int ballNumber_, float ballsXPosition_) {
        this.ballsXPositions[ballNumber_]=ballsXPosition_;
    }

    void setBallsYPositions(int ballNumber_, float ballsYPosition_) {
        this.ballsYPositions[ballNumber_]=ballsYPosition_;
    }

    void setBallsSizes(int ballNumber_, float ballsSize_) {
        this.ballsSizes[ballNumber_]=ballsSize_;
    }


    float[] getBallsXPositions() {
        return(this.ballsXPositions);
    }

    float[] getBallsYPositions() {
        return(this.ballsYPositions);
    }

    float[] getBallsSizes() {
        return(this.ballsSizes);
    }


}

