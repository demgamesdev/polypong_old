package com.demgames.polypong;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Intent;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.util.Log;
import android.widget.TextView;

import java.net.InetAddress;
import java.nio.ByteOrder;
import java.math.BigInteger;
import java.net.UnknownHostException;

import android.os.PowerManager;

public class MainActivity extends AppCompatActivity {

    //Always On
    protected PowerManager.WakeLock mWakeLock;
    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ToDo Splash screen mit Pixellook integrieren?
        //ToDo Aufteilung des Menüs, Button Optik
        //ToDo eventuell nur Portrait modus für menu einstellen

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



         /* will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_main);


        final Button startHostButton = (Button) findViewById(R.id.startHostButton);
        final Button startClientButton = (Button) findViewById(R.id.startClientButton);


        startHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startHost = new Intent(getApplicationContext(), Options.class);
                startActivity(startHost);

            }

        });

        startClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startClient = new Intent(getApplicationContext(), Client.class);
                startActivity(startClient);
                //myThread.stop();

            }
        });
    }


}