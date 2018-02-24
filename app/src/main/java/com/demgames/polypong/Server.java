package com.demgames.polypong;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.demgames.polypong.R;
import com.demgames.polypong.gamelaunch;

import org.w3c.dom.Text;

public class Server extends AppCompatActivity {

    int balls=1;
    String ipAdress = "1.1.1.0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        final TextView anzahlBallText = (TextView) findViewById(R.id.ballsTextView);
        final SeekBar anzahlBallSeek = (SeekBar) findViewById(R.id.ballsSeekBar);
        final Button enterServer = (Button) findViewById(R.id.enterServerButton);

        enterServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("ipadress", ipAdress);
                balls++;
                startGame.putExtra("ballnumber", Integer.toString(balls));
                startActivity(startGame); //Regeln wird gestartet
            }
        });

        anzahlBallSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                balls = progress + 1;
                if (balls >= 10){
                    anzahlBallText.setText("Anzahl der Bälle: "+ balls);
                }
                else
                    anzahlBallText.setText("Anzahl der Bälle: "+ balls + "  ");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
