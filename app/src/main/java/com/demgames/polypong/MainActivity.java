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

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_main);



        final EditText hostIpEditText = (EditText) findViewById(R.id.hostIpEditText);
        final EditText ballnumberEditText = (EditText) findViewById(R.id.ballnumberEditText);
        final EditText modeEditText = (EditText) findViewById(R.id.modeEditText);

        final TextView myIpTextView = (TextView) findViewById(R.id.myIpTextView);

        final Button startGameButton = (Button) findViewById(R.id.startGameButton);
        final Button updateIpButton = (Button) findViewById(R.id.updateIpButton);

        final String myIpAdress=wifiIpAddress(getApplicationContext());
        if(myIpAdress!=null) {
            myIpTextView.setText(myIpAdress);

        } else {
            myIpTextView.setText("Unable to get Ip-Adress");
        }

        updateIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myIpAdress=wifiIpAddress(getApplicationContext());
                if(myIpAdress!=null) {
                    myIpTextView.setText(myIpAdress);

                } else {
                    myIpTextView.setText("Unable to get Ip-Adress");
                }
            }
        });

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("hostipadress", hostIpEditText.getText().toString());
                startGame.putExtra("myipadress", myIpAdress);
                startGame.putExtra("ballnumber", ballnumberEditText.getText().toString());
                startGame.putExtra("mode", modeEditText.getText().toString());
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

}

