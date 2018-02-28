package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class Options extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    int ballnum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_options);

        final Button devBtnn = (Button) findViewById(R.id.button12);
        final SeekBar ballSeekBar = (SeekBar) findViewById(R.id.seekBar2);
        final TextView ballTextView = (TextView) findViewById(R.id.ballll);

        final CheckBox gravity = (CheckBox) findViewById(R.id.gravitycheckBox);
        final CheckBox attraction = (CheckBox) findViewById(R.id.attractcheckBox);

        ballTextView.setText( getString(R.string.numballs) + Integer.toString(ballnum));

        final Globals globalVariables = (Globals) getApplicationContext();

        devBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /***Optionen an Global Übergeben'**/
                globalVariables.setNumberOfBalls(Integer.toString(ballnum));
                Log.d(TAG, "Anzahl der bälle: "+ globalVariables.getNumberOfBalls());
                globalVariables.setGravity(gravity.isChecked());
                globalVariables.setAttraction(attraction.isChecked());

                /***Server Activity starten***/
                Intent startServer = new Intent(getApplicationContext(), Server.class);
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

}
