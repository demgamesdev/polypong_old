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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Options extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "MyActivity";
    int ballnum = 1;
    float fric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_options);

        final Globals globalVariables = (Globals) getApplicationContext();

        final Button devBtnn = (Button) findViewById(R.id.button12);
        final SeekBar ballSeekBar = (SeekBar) findViewById(R.id.seekBar2);
        final SeekBar frictionSeekBar = (SeekBar) findViewById(R.id.frictionSeekBar);

        final TextView ballTextView = (TextView) findViewById(R.id.ballll);
        final TextView frictionTextView = (TextView) findViewById(R.id.frictionTextView);

        final CheckBox gravity = (CheckBox) findViewById(R.id.gravitycheckBox);
        final CheckBox attraction = (CheckBox) findViewById(R.id.attractcheckBox);


        //Spielmodus Spinner
        final Spinner gamemode = (Spinner) findViewById(R.id.gamemodeSpinner);
        gamemode.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(this,
                R.array.gamemodes, R.layout.spinner_item);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gamemode.setAdapter(spinneradapter);

        ballTextView.setText( getString(R.string.numballs) + Integer.toString(ballnum));



        devBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /***Optionen an Global Übergeben'**/
                globalVariables.setNumberOfBalls(Integer.toString(ballnum));
                Log.d(TAG, "Anzahl der bälle: "+ globalVariables.getNumberOfBalls());
                globalVariables.setGravityState(gravity.isChecked());
                globalVariables.setAttractionState(attraction.isChecked());
                globalVariables.setFriction(fric);

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
                //Log.d(TAG, "onProgressChanged: Anzahl der Bälle auf " + Integer.toString(ballnum) + " geändert");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        frictionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                fric = ((float)i/10000)*2;
                frictionTextView.setText(Float.toString(fric) + " Reibung");
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Globals globalVariables = (Globals) getApplicationContext();
        switch (i){
            case 0:
                globalVariables.setGameMode(1);
                Log.d(TAG, "onItemSelected: Klassischer Spielmodus ausgewähöt");
                break;
            case 1:
                globalVariables.setGameMode(2);
                Log.d(TAG, "onItemSelected: Pong Spielmodus ausgewählt");
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.setGameMode(1);
    }
}
