package com.demgames.polypong;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;

import java.net.InetAddress;
import java.nio.ByteOrder;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oscP5.*;
import netP5.*;

public class Client extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_client);

        /***Deklarationen***/
        final Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.setConnectState(false);
        globalVariables.setReadyStateState(false);
        globalVariables.setSettingsState(false);
        globalVariables.setGameLaunched(false);

        globalVariables.setMyIpList(new String[] {});
        globalVariables.setArrayAdapter(new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, globalVariables.getMyIpList()));

        globalVariables.setOscP5(new OscP5(this, globalVariables.getMyPort()));

        final TextView myIpTextView = (TextView) findViewById(R.id.IpAdressTextView);
        final Button devBtn = (Button) findViewById(R.id.devButton);
        final ListView ClientLV = (ListView) findViewById(R.id.ClientListView);

        ClientLV.setAdapter(globalVariables.getArrayAdapter());


        //automatically detect ip if available, create new thread for searching ip
        final Byte testByte = 0;
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(1000);
                        if(!checkIfIp(globalVariables.getMyIpAdress())) {
                            globalVariables.setMyIpAdress(wifiIpAddress(getApplicationContext()));
                        }

                        if(globalVariables.getConnectState() && !globalVariables.getSettingsState() &&
                                checkIfIp(globalVariables.getMyIpAdress())) {
                            sendClientConnect();
                        } else if(globalVariables.getSettingsState() && globalVariables.getConnectState()) {
                            sendClientReady();
                        }

                        //Eigene IP-Adresse
                        myIpTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (checkIfIp(globalVariables.getMyIpAdress())) {
                                    myIpTextView.setText("Deine IP-Adresse lautet: " + globalVariables.getMyIpAdress());
                                    //IP Adresse wird in die Liste Hinzugefügt
                                    //globalVariables.addIpTolist(globalVariables.getMyIpAdress());

                                } else {
                                    myIpTextView.setText("Unable to get Ip-Adress");
                                }
                                //globalVariables.updateListView();

                            }
                        });


                    }

                } catch (InterruptedException e) {
                    Log.d(Client.class.getSimpleName(),"myThread interrupted");
                    e.printStackTrace();
                }

            }
        };

        globalVariables.setUpdateThread(updateRunnable);
        globalVariables.getUpdateThread().start();


        /***Developer Button Listener***/
        /*devBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String balls = "20";
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                //Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("myipadress", globalVariables.getMyIpAdress());
                startGame.putExtra("ballnumber", balls);
                startGame.putExtra("mode", "client");
                startActivity(startGame);
            }
        });*/

    }


    /********* OSCP5 EVENTHANDLER *********/

    void oscEvent(OscMessage theOscMessage) {
        final Globals globalVariables=(Globals) getApplication();
        switch(theOscMessage.addrPattern()) {
            case "/hostconnect":
                Log.d(Client.class.getSimpleName(),"oscP5 received hostconnect");
                String remoteIpAdress = theOscMessage.get(0).stringValue();
                if(checkIfIp(remoteIpAdress)) {
                    globalVariables.setConnectState(true);
                    globalVariables.addIpTolist(remoteIpAdress);
                    globalVariables.setRemoteIpAdress(remoteIpAdress);
                }
                //sendClientConnect();
                break;

            case "/settings":
                globalVariables.setSettingsState(true);
                Log.d(Client.class.getSimpleName(),"oscP5 received settings");
                //value=theOscMessage.get(0).intValue();
                globalVariables.setNumberOfBalls(theOscMessage.get(0).stringValue());
                globalVariables.setFriction(theOscMessage.get(1).floatValue());
                Log.d(Client.class.getSimpleName(),"+++++++++++++Friction="+Float.toString(globalVariables.getFriction()));
                break;

            case "/hostready":
                Log.d(Client.class.getSimpleName(),"oscP5 received hostready");
                globalVariables.setReadyStateState(true);
                if(!globalVariables.getGameLaunched()) {
                    globalVariables.stopOscP5();
                    Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                    //Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                    startGame.putExtra("myipadress", globalVariables.getMyIpAdress());
                    startGame.putExtra("remoteipadress", globalVariables.getRemoteIpAdress());
                    startGame.putExtra("numberofballs", globalVariables.getNumberOfBalls());
                    startGame.putExtra("mode", "client");
                    startActivity(startGame);
                    //globalVariables.myThread.stop();
                    globalVariables.interruptUpdateThread();
                    globalVariables.stopOscP5();
                    //globalVariables.setGameLaunched(true);
                    finish();
                }
                break;
        }
    }

    /********* SEND FUNCTIONS *********/

    void sendClientConnect() {
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        Log.d(Client.class.getSimpleName(),"oscP5 send clientconnect");
        OscMessage connectMessage = new OscMessage("/clientconnect");
        connectMessage.add(globalVariables.getMyIpAdress());
        globalVariables.getOscP5().send(connectMessage, myRemoteLocation);
    }

    void sendClientReady() {
        Log.d(Client.class.getSimpleName(),"oscP5 send clientready");
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        OscMessage readyMessage = new OscMessage("/clientready");
        readyMessage.add(globalVariables.getMyIpAdress());
        globalVariables.getOscP5().send(readyMessage, myRemoteLocation);
    }

    /********* OTHER FUNCTIONS *********/


    protected String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    boolean checkIfIp(String teststring) {
        if(teststring != null) {
            String[] parts = teststring.split("\\."); //String wird bei jedem Punkt gesplittet
            if (parts.length == 4) {                        //String muss aus 4 Teilen bestehen
                return (true);
            }
        }

        return (false);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            Globals globalVariables = (Globals) getApplicationContext();
            globalVariables.interruptUpdateThread();
            globalVariables.stopOscP5();

        }
        return super.onKeyDown(keyCode, event);
    }


}


