package com.demgames.polypong;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.nio.ByteOrder;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oscP5.*;
import netP5.*;

import org.w3c.dom.Text;

public class Client extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        final Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.connectState=false;
        globalVariables.readyState=false;
        globalVariables.settingsState=false;
        globalVariables.gameLaunched=false;

        final TextView myIpTextView = (TextView) findViewById(R.id.IpAdressTextView);
        final Button devBtn = (Button) findViewById(R.id.devButton);
        final ListView ClientLV = (ListView) findViewById(R.id.ClientListView);

        globalVariables.setMyIpList(new String[] {});
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, globalVariables.getMyIpList());
        globalVariables.setArrayAdapter(arrayAdapter);

        ClientLV.setAdapter(globalVariables.arrayAdapter);

        globalVariables.setOscP5(new OscP5(this, globalVariables.getMyPort()));
        //automatically detect ip if available, create new thread for searching ip
        final Byte testByte = 0;
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                while (testByte == 0) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!checkIfIp(globalVariables.getMyIpAdress())) {
                        globalVariables.setMyIpAdress(wifiIpAddress(getApplicationContext()));
                    }

                    /*if(globalVariables.connectState && !globalVariables.readyState
                            && checkIfIp(globalVariables.getMyIpAdress())) {
                        sendClientConnect();
                    } else {
                        Log.d(Client.class.getSimpleName(),"sendClientConnect failed");
                    }*/

                    if(globalVariables.connectState && !globalVariables.settingsState &&
                            checkIfIp(globalVariables.getMyIpAdress()) && !globalVariables.gameLaunched) {
                        sendClientConnect();
                    } else if(globalVariables.settingsState && globalVariables.connectState && !globalVariables.gameLaunched) {
                        sendClientReady();
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
            }
        };

        globalVariables.setMyThread(myRunnable);
        globalVariables.myThread.start();


        /*Developer Button Listener*/
        devBtn.setOnClickListener(new View.OnClickListener() {
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
        });


    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

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
            String[] parts = teststring.split("\\.");
            if (parts.length == 4) {
                return (true);
            }
        }

        return (false);

    }


    void oscEvent(OscMessage theOscMessage) {
        final Globals globalVariables=(Globals) getApplication();
        switch(theOscMessage.addrPattern()) {
            case "/hostconnect":
                Log.d(Client.class.getSimpleName(),"oscP5 received hostconnect");
                String remoteIpAdress = theOscMessage.get(0).stringValue();
                if(checkIfIp(remoteIpAdress)) {
                    globalVariables.connectState=true;
                    globalVariables.addIpTolist(remoteIpAdress);
                    globalVariables.remoteIpAdress=remoteIpAdress;
                }
                //sendClientConnect();
                break;

            case "/settings":
                globalVariables.settingsState=true;
                Log.d(Client.class.getSimpleName(),"oscP5 received settings");
                //value=theOscMessage.get(0).intValue();
                globalVariables.numberOfBalls=theOscMessage.get(0).stringValue();
                break;

            case "/hostready":
                Log.d(Client.class.getSimpleName(),"oscP5 received hostready");
                globalVariables.readyState=true;
                if(!globalVariables.gameLaunched) {
                    globalVariables.stopOscP5();
                    Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                    //Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                    startGame.putExtra("myipadress", globalVariables.getMyIpAdress());
                    startGame.putExtra("remoteipadress", globalVariables.remoteIpAdress);
                    startGame.putExtra("numberofballs", globalVariables.numberOfBalls);
                    startGame.putExtra("mode", "client");
                    startActivity(startGame);
                    //globalVariables.myThread.stop();
                    globalVariables.stopOscP5();
                    globalVariables.gameLaunched=true;
                    finish();
                }
                break;
        }
    }

    void sendClientConnect() {
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.remoteIpAdress,globalVariables.getMyPort());
        Log.d(Client.class.getSimpleName(),"oscP5 send clientconnect");
        OscMessage connectMessage = new OscMessage("/clientconnect");
        connectMessage.add(globalVariables.getMyIpAdress());
        globalVariables.getOscP5().send(connectMessage, myRemoteLocation);
    }

    void sendClientReady() {
        Log.d(Client.class.getSimpleName(),"oscP5 send clientready");
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.remoteIpAdress,globalVariables.getMyPort());
        OscMessage readyMessage = new OscMessage("/clientready");
        readyMessage.add(globalVariables.getMyIpAdress());
        globalVariables.getOscP5().send(readyMessage, myRemoteLocation);
    }


}


