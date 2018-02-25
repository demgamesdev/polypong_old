package com.demgames.polypong;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.util.Log;
import android.widget.TextView;

import java.net.InetAddress;
import java.nio.ByteOrder;
import java.math.BigInteger;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ToDo Splash screen mit Pixellook integrieren?

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


}