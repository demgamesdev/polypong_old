package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.KeyEvent;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oscP5.*;
import netP5.*;

public class Server extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_server);

        /***Decklarationen***/

        final Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.setConnectState(false);
        globalVariables.setReadyStateState(false);
        globalVariables.setSettingsState(false);
        globalVariables.setGameLaunched(false);
        //globalVariables.setNumberOfBalls(getIntent().getExtras().getString("numberofballs"));  Wird bereits in Options Activity in Globals gespeichert
        globalVariables.setMyIpList(new String[] {});

        globalVariables.setArrayAdapter(new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, globalVariables.getMyIpList()));

        globalVariables.setOscP5(new OscP5(this, globalVariables.getMyPort()));

        final Button devBtn = (Button) findViewById(R.id.devBtn);
        final TextView myIpTextView = (TextView) findViewById(R.id.IPtextView);
        final ListView ServerLV = (ListView) findViewById(R.id.serverListView);

        ServerLV.setAdapter(globalVariables.getArrayAdapter());

        //update runnable
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()) {

                        Thread.sleep(1000);
                        globalVariables.setMyIpAdress(wifiIpAddress(getApplicationContext()));

                        if(!globalVariables.getConnectState() && checkIfIp(globalVariables.getMyIpAdress())) {
                            sendHostConnect();
                        } else if(!globalVariables.getSettingsState() && globalVariables.getConnectState()) {
                            sendSettings();
                        } else if(globalVariables.getReadyState() && globalVariables.getSettingsState()) {
                            sendHostReady();
                        }

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
                    Log.d(Server.class.getSimpleName(),"myThread interrupted");
                    e.printStackTrace();
                }
            }

        };

        globalVariables.setUpdateThread(updateRunnable);
        globalVariables.getUpdateThread().start();


        //listview clickevent
        ServerLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            sendSettings();
                            //Your code goes here
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
            }
        });


    }

    /********* OSCP5 EVENTHANDLER *********/


    void oscEvent(OscMessage theOscMessage) {
        final Globals globalVariables=(Globals) getApplication();
        switch(theOscMessage.addrPattern()) {
            case "/clientconnect":
                Log.d(Server.class.getSimpleName(),"oscP5 received clientconnect");
                globalVariables.setConnectState(true);
                String remoteIpAdress = theOscMessage.get(0).stringValue();
                globalVariables.addIpTolist(remoteIpAdress);
                globalVariables.setRemoteIpAdress(remoteIpAdress);
                break;

            case "/clientready":
                Log.d(Server.class.getSimpleName(),"oscP5 received clientready");
                globalVariables.setSettingsState(true);
                globalVariables.setReadyStateState(true);
                globalVariables.addPlayerNameTolist(theOscMessage.get(0).stringValue());
                //sendHostReady();

                break;

        }
    }

    /********* SEND FUNCTIONS *********/

    void sendHostConnect() {
        Log.d(Server.class.getSimpleName(),"oscP5 send hostconnect");
        Globals globalVariables = (Globals) getApplicationContext();
        String[] myIpParts = globalVariables.getMyIpAdress().split("\\.");
        NetAddress broadcastLocation = new NetAddress(myIpParts[0] + "." + myIpParts[1] + "." + myIpParts[2] + ".255", globalVariables.getMyPort());
        OscMessage connectMessage = new OscMessage("/hostconnect");
        connectMessage.add(globalVariables.getMyIpAdress());
        globalVariables.getOscP5().send(connectMessage, broadcastLocation);
    }

    void sendSettings() {
        Log.d(Server.class.getSimpleName(),"oscP5 send settings");
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        OscMessage settingsMessage = new OscMessage("/settings");
        settingsMessage.add(globalVariables.getNumberOfBalls());
        settingsMessage.add(globalVariables.getFriction());
        globalVariables.getOscP5().send(settingsMessage, myRemoteLocation);

    }

    //Spiel wird gestartet
    void sendHostReady() {
        Log.d(Server.class.getSimpleName(),"oscP5 send hostready");
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        OscMessage readyMessage = new OscMessage("/hostready");
        globalVariables.getOscP5().send(readyMessage, myRemoteLocation);

        if(!globalVariables.getGameLaunched()) {
            globalVariables.interruptUpdateThread();
            globalVariables.stopOscP5();

            Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
            startGame.putExtra("myipadress", globalVariables.getMyIpAdress());
            startGame.putExtra("remoteipadress", globalVariables.getRemoteIpAdress());
            startGame.putExtra("numberofballs", globalVariables.getNumberOfBalls());
            startGame.putExtra("mode", "host");
            startActivity(startGame);
            //globalVariables.myThread.stop();

            //globalVariables.setGameLaunched(true);
            finish();
        }
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
            //ipAddressString = null;
            ipAddressString = "192.168.43.1";
        }

        return ipAddressString;
    }

    boolean checkIfIp(String teststring) {
        if(teststring != null) {
            String[] parts = teststring.split("\\.");
            if (parts.length == 4) {
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
