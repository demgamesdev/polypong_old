package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

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
        setContentView(R.layout.activity_server);

        final Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.connectState=false;
        globalVariables.readyState=false;

        final Button devBtn = (Button) findViewById(R.id.devBtn);
        final TextView myIpTextView = (TextView) findViewById(R.id.IPtextView);
        final ListView ServerLV = (ListView) findViewById(R.id.serverListView);

        globalVariables.numberOfBalls = getIntent().getExtras().getString("numberofballs");

        //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS

        globalVariables.setMyIpList(new String[] {});
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, globalVariables.getMyIpList());
        globalVariables.setArrayAdapter(arrayAdapter);
        ServerLV.setAdapter(arrayAdapter);

        globalVariables.oscP5 = new OscP5(this, globalVariables.getMyPort());

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
                    /*if(!globalVariables.connectState && !globalVariables.readyState
                            && checkIfIp(globalVariables.getMyIpAdress())) {
                        sendHostConnect();
                    }*/
                    sendHostConnect();

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
                sendHostReady();
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("myipadress", globalVariables.getMyIpAdress());
                startGame.putExtra("numberofballs", globalVariables.numberOfBalls);
                startGame.putExtra("mode", "host");
                startActivity(startGame);
                finish();
            }
        });

        ServerLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendSettings();
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
            case "/clientconnect":
                globalVariables.connectState=true;
                String remoteIpAdress = theOscMessage.get(0).stringValue();
                globalVariables.addIpTolist(remoteIpAdress);
                globalVariables.myRemoteLocation = new NetAddress(remoteIpAdress, globalVariables.getMyPort());
                break;

            case "/clientready":
                globalVariables.readyState=true;
                globalVariables.addPlayerNameTolist(theOscMessage.get(0).stringValue());
                //sendHostReady();

                break;

        }
    }

    void sendHostConnect() {
        Globals globalVariables = (Globals) getApplicationContext();
        String[] myIpParts = globalVariables.getMyIpAdress().split("\\.");
        NetAddress broadcastLocation = new NetAddress(myIpParts[0] + "." + myIpParts[1] + "." + myIpParts[2] + ".255", globalVariables.getMyPort());
        OscMessage connectMessage = new OscMessage("/hostconnect");
        connectMessage.add(globalVariables.getMyIpAdress());
        globalVariables.oscP5.send(connectMessage, broadcastLocation);
    }

    void sendSettings() {
        Globals globalVariables = (Globals) getApplicationContext();
        OscMessage settingsMessage = new OscMessage("/settings");
        settingsMessage.add(globalVariables.numberOfBalls);
        globalVariables.oscP5.send(settingsMessage, globalVariables.myRemoteLocation);

    }

    void sendHostReady() {
        Globals globalVariables = (Globals) getApplicationContext();
        OscMessage readyMessage = new OscMessage("/hostready");
        globalVariables.oscP5.send(readyMessage, globalVariables.myRemoteLocation);
    }
}
