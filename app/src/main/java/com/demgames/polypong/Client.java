package com.demgames.polypong;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Intent;
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

import org.w3c.dom.Text;

public class Client extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        final TextView myIpTextView = (TextView) findViewById(R.id.IpAdressTextView);
        final Button devBtn = (Button) findViewById(R.id.devButton);
        final ListView ClientLV = (ListView) findViewById(R.id.ClientListView);

        //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
        String[] IPAdressen = new String[] {        };

        final List<String> server_list = new ArrayList<String>(Arrays.asList(IPAdressen));

        //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, server_list);
        ClientLV.setAdapter(arrayAdapter);


        //automatically detect ip if available, create new thread for searching ip
        final Byte testByte=0;
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                while (testByte == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final String myIpAdress=wifiIpAddress(getApplicationContext());
                    myIpTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            if(myIpAdress!=null) {
                                myIpTextView.setText("Deine IP-Adresse lautet: " + myIpAdress);
                                //IP Adresse wird in die Liste Hinzugefügt
                                addipTolist(myIpAdress, server_list, arrayAdapter);

                            } else {
                                myIpTextView.setText("Unable to get Ip-Adress");
                            }

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
                final String myIpAdress=wifiIpAddress(getApplicationContext());
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                //Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("myipadress", myIpAdress);
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
    //IP in die Liste hinzufügen
    public void addipTolist(String IP, List server_list, ArrayAdapter arrayAdapter){
        if(!server_list.contains(IP)){
            server_list.add(IP);
            //Liste wird aktualisiert
            arrayAdapter.notifyDataSetChanged();
        }
    }
}
