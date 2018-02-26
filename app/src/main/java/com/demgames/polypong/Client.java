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

        final TextView myIpTextView = (TextView) findViewById(R.id.IpAdressTextView);
        final Button devBtn = (Button) findViewById(R.id.devButton);
        final ListView ClientLV = (ListView) findViewById(R.id.ClientListView);

        globalVariables.setMyIpList(new String[] {});
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, globalVariables.getMyIpList());
        globalVariables.setArrayAdapter(arrayAdapter);

        ClientLV.setAdapter(globalVariables.arrayAdapter);

        globalVariables.oscP5 = new OscP5(this, globalVariables.getMyPort());

        //automatically detect ip if available, create new thread for searching ip
        final Byte testByte = 0;
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                while (testByte == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    globalVariables.setMyIpAdress(wifiIpAddress(getApplicationContext()));
                    if(globalVariables.connectState && !globalVariables.readyState
                            && checkIfIp(globalVariables.getMyIpAdress())) {
                        sendClientConnect();
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

        final Thread myThread = new Thread(myRunnable);
        myThread.start();

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
        String[] parts=teststring.split("\\.");
        if(parts.length==4) {
            return(true);
        }

        return (false);

    }


    void oscEvent(OscMessage theOscMessage) {
        final Globals globalVariables=(Globals) getApplication();
        switch(theOscMessage.addrPattern()) {
            case "/hostconnect":
                globalVariables.connectState=true;
                String remoteIpAdress = theOscMessage.get(0).stringValue();
                globalVariables.addIpTolist(remoteIpAdress);
                globalVariables.myRemoteLocation = new NetAddress(remoteIpAdress, globalVariables.getMyPort());
                sendClientConnect();
                break;

            case "/settings":
                //value=theOscMessage.get(0).intValue();
                globalVariables.numberOfBalls=theOscMessage.get(0).stringValue();
                sendClientReady();

                break;

            case "/hostready":
                globalVariables.readyState=true;
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                //Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("myipadress", globalVariables.getMyIpAdress());
                startGame.putExtra("numberofballs", globalVariables.numberOfBalls);
                startGame.putExtra("mode", "client");
                startActivity(startGame);
                finish();
                break;
        }
    }

    void sendClientConnect() {
        Globals globalVariables = (Globals) getApplicationContext();
        OscMessage connectMessage = new OscMessage("/clientconnect");
        connectMessage.add(globalVariables.getMyIpAdress());
        globalVariables.oscP5.send(connectMessage, globalVariables.myRemoteLocation);
    }

    void sendClientReady() {
        Globals globalVariables = (Globals) getApplicationContext();
        OscMessage readyMessage = new OscMessage("/clientready");
        readyMessage.add(globalVariables.getMyIpAdress());
        globalVariables.oscP5.send(readyMessage, globalVariables.myRemoteLocation);
    }


}


