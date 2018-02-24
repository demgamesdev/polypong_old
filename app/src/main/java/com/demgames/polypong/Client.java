package com.demgames.polypong;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.demgames.polypong.R;

public class Client extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String balls = "20";

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_client);


        final Button startGame_Button = (Button) findViewById(R.id.joinBtn);
        final EditText ipAdEditText = (EditText) findViewById(R.id.IPeditText);

        startGame_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkIP(ipAdEditText.getText().toString())) {
                    Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                    startGame.putExtra("ipadress", ipAdEditText.getText().toString());
                    startGame.putExtra("ballnumber",balls);
                    startActivity(startGame);
                }
                else{
                    ipAdEditText.setText("");
                    ipAdEditText.setHint("IP Adresse ungültig");
                }

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

    public boolean checkIP(String IP) {
        // ToDo Schaun ob der String positive Integer hat und Sonderzeichen
        String[] parts = IP.split("\\.");
        int[] numbers = new int[parts.length];
        if (parts.length == 4) {
            for (int i = 0; i < parts.length; i++) {
                numbers[i] = Integer.parseInt(parts[i]);
            }
            //Arrays.asList(numbers).contains(num);
            return true;
        } else {
            return false;
        }
    }
}