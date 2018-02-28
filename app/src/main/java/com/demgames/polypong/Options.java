package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Options extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    int ballnum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        final Button devBtnn = (Button) findViewById(R.id.button12);
        final SeekBar ballSeekBar = (SeekBar) findViewById(R.id.seekBar2);
        final TextView ballTextView = (TextView) findViewById(R.id.ballll);

        ballTextView.setText( getString(R.string.numballs) + Integer.toString(ballnum));

        final Globals globalVariables = (Globals) getApplicationContext();

        devBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startServer = new Intent(getApplicationContext(), Server.class);
                globalVariables.setNumberOfBalls(Integer.toString(ballnum));
                //startServer.putExtra("numberofballs", Integer.toString(ballnum));
                startActivity(startServer);
            }
        });


        Log.d(TAG, "onCreate: Alles safe bis hier");

        ballSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ballnum = i+1;
                ballTextView.setText(getString(R.string.numballs) + Integer.toString(ballnum));
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
