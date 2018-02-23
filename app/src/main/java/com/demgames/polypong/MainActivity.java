package com.demgames.polypong;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button startGame_Button = (Button) findViewById(R.id.startGame_Button);
        final EditText ipEditText = (EditText) findViewById(R.id.ipEditText);
        final EditText ballnumberEditText = (EditText) findViewById(R.id.ballnumberEditText);

        startGame_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("ipadress", ipEditText.getText().toString());
                startGame.putExtra("ballnumber", ballnumberEditText.getText().toString());
                startActivity(startGame);
            }
        });
    }

}