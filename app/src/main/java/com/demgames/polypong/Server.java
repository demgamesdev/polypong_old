package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class Server extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        final Button devBtn = (Button) findViewById(R.id.devBtn);
        final TextView myIpTextView = (TextView) findViewById(R.id.IPtextView);

        final String ballnum = getIntent().getExtras().getString("ballnumber");

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
                final String myIpAdress=wifiIpAddress(getApplicationContext());
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("myipadress", myIpAdress);
                startGame.putExtra("ballnumber", ballnum);
                startGame.putExtra("mode", "host");
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
}
