package com.demgames.polypong;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oscP5.*;
import netP5.*;



public class Server extends AppCompatActivity {

    private static final String TAG = "Server";
    private MyTask MyTaskServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_server);

        /***Decklarationen***/

        final Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.setConnectState(false);
        globalVariables.setReadyStateState(false);
        globalVariables.setSettingsState(false);
        globalVariables.setGameLaunched(false);
        //globalVariables.setNumberOfBalls(getIntent().getExtras().getString("numberofballs"));  Wird bereits in Options Activity in Globals gespeichert
        globalVariables.setMyIpList(new String[] {});
        globalVariables.setRemoteIpAdress(null);

        //globalVariables.setArrayAdapter(new ArrayAdapter<String>
                //(this, R.layout.listview, globalVariables.getMyIpList()));

        globalVariables.setOscP5(new OscP5(this, globalVariables.getMyPort()));

        final Button devBtn = (Button) findViewById(R.id.devBtn);
        final TextView myIpTextView = (TextView) findViewById(R.id.IPtextView);


        final ListView ServerLV = (ListView) findViewById(R.id.serverListView);

        //IP Suche
       MyTaskServer= new MyTask();
        MyTaskServer.execute();

        //update runnable
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()) {

                        Thread.sleep(1000);
                        myIpTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (checkIfIp(globalVariables.getMyIpAdress())) {
                                    myIpTextView.setText("Deine IP-Adresse lautet: " + globalVariables.getMyIpAdress());
                                }
                                else {
                                    myIpTextView.setText("Unable to get Ip-Adress");
                                }
                            }
                        });

                    }
                }
                catch (InterruptedException e) {
                    Log.d(Server.class.getSimpleName(),"myThread interrupted");
                    e.printStackTrace();
                }
            }
        };

        globalVariables.setUpdateThread(updateRunnable);
        globalVariables.getUpdateThread().start();

    }

    @Override
    protected void onDestroy() {
        MyTaskServer.cancel(true);
        Log.d(TAG, "onDestroy: ");

        super.onDestroy();
    }

    /********* OSCP5 EVENTHANDLER *********/


    void oscEvent(OscMessage theOscMessage) {
        final Globals globalVariables=(Globals) getApplication();
        String TAG = "Server";
        switch(theOscMessage.addrPattern()) {
            case "/clientconnect":
                Log.d(Server.class.getSimpleName(),"oscP5 received clientconnect");
                //globalVariables.setConnectState(true);
                String remoteIpAdress = theOscMessage.get(0).stringValue();

                //globalVariables.addIpTolist(remoteIpAdress);
                globalVariables.setRemoteIpAdress(remoteIpAdress);
                //addIPList(remoteIpAdress);
                Log.d(TAG, "clientconnect: "+ remoteIpAdress);

                break;

            case "/clientTohost":
                Log.d(Server.class.getSimpleName(),"oscP5 received client2host");
                remoteIpAdress = theOscMessage.get(0).stringValue();
                globalVariables.setRemoteIpAdress(remoteIpAdress);
                Log.d(Server.class.getSimpleName(),"client2host: "+remoteIpAdress);
                globalVariables.setConnectState(true);

            case "/clientready":
                Log.d(Server.class.getSimpleName(),"oscP5 received clientready");
                globalVariables.setSettingsState(true);
                //globalVariables.setReadyStateState(true);
                globalVariables.addPlayerNameTolist(theOscMessage.get(0).stringValue());
                //sendHostReady();

                break;
            case "/settings":
                Log.d(Server.class.getSimpleName(),"oscP5 received settings");
                //Spielername empfangen und speichern
                String[] playerName = new String[2];
                List<String> nameList = new ArrayList<String>();
                nameList=globalVariables.getPlayerNamesList();
                playerName[0] = nameList.get(0); //Eigener Name
                playerName[1] = (theOscMessage.get(0).stringValue());
                globalVariables.setPlayerNamesList(playerName);
                Log.d(TAG, "oscEvent: Name" + playerName[1]);
                //sendHostReady();
                break;
        }
    }


    /********* SEND FUNCTIONS *********/

    void sendHostConnect() {
        Log.d(Server.class.getSimpleName(),"oscP5 send hostconnect");
        Globals globalVariables = (Globals) getApplicationContext();
        String[] myIpParts = globalVariables.getMyIpAdress().split("\\.");
        NetAddress broadcastLocation = new NetAddress(myIpParts[0] + "." + myIpParts[1] + "." + myIpParts[2] + ".255", globalVariables.getMyPort());
        OscMessage connectMessage = new OscMessage("/hostconnect");
        connectMessage.add(globalVariables.getMyIpAdress());
        globalVariables.getOscP5().send(connectMessage, broadcastLocation);
    }

    void sendSettings() {
        Log.d(Server.class.getSimpleName(),"oscP5 send settings");
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        OscMessage settingsMessage = new OscMessage("/settings");
        settingsMessage.add(globalVariables.getNumberOfBalls());
        settingsMessage.add(globalVariables.getFriction());
        settingsMessage.add(globalVariables.getGameMode());
        settingsMessage.add(globalVariables.getPlayerNamesList().get(0));
        //settingsMessage.add(globalVariables.getGravity());
        globalVariables.getOscP5().send(settingsMessage, myRemoteLocation);
        globalVariables.setReadyStateState(true);
        sendHostReady();

    }

    //Spiel wird gestartet
    void sendHostReady() {
        Log.d(Server.class.getSimpleName(),"oscP5 send hostready");
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        OscMessage readyMessage = new OscMessage("/hostready");
        globalVariables.getOscP5().send(readyMessage, myRemoteLocation);

        if(!globalVariables.getGameLaunched()) {
            globalVariables.interruptUpdateThread();
            globalVariables.stopOscP5();

            Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
            startGame.putExtra("myipadress", globalVariables.getMyIpAdress());
            startGame.putExtra("remoteipadress", globalVariables.getRemoteIpAdress());
            startGame.putExtra("numberofballs", globalVariables.getNumberOfBalls());
            startGame.putExtra("mode", "host");
            startActivity(startGame);
            //globalVariables.myThread.stop();

            globalVariables.setGameLaunched(true);
            finish();
        }
    }

    /********* OTHER FUNCTIONS *********/


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
            //ipAddressString = null;
            ipAddressString = "192.168.43.1";
        }
        return ipAddressString;
    }

    boolean checkIfIp(String teststring) {
        if(teststring != null) {
            String[] parts = teststring.split("\\.");
            if (parts.length == 4) {
                return (true);
            }
        }
        return (false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            Globals globalVariables = (Globals) getApplicationContext();
            globalVariables.interruptUpdateThread();
            globalVariables.stopOscP5();
        }
        return super.onKeyDown(keyCode, event);
    }


    /********* Thread Function - Searching IP and displaying *********/
    //Zeigt die IP Adresse an während dem Suchen
    class MyTask extends AsyncTask<Void,Void,Void>{

        Globals globalVariables = (Globals) getApplicationContext();
        String[] ListElements = new String[]{};
        List<String> ipAdressList = new ArrayList<String>(Arrays.asList(ListElements));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (Server.this, R.layout.listview, ipAdressList);


        @Override
        protected Void doInBackground(Void... voids) {

            Log.d(TAG, "doInBackground: Anfang Suche");
            globalVariables.setMyIpAdress(wifiIpAddress(getApplicationContext()));

            while(!globalVariables.getConnectState()){
                sendHostConnect();
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while(!globalVariables.getReadyState()){
                Log.d(TAG, "doInBackground: Sende Settings");
                sendSettings();
            }

            Log.d(TAG, "doInBackground: Ende Suche");
            return null;
        }

        @Override
        protected void onPreExecute() {
            ListView ServerLV = (ListView) findViewById(R.id.serverListView);
            ServerLV.setAdapter(adapter);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(globalVariables.getRemoteIpAdress()!=null){
                if(!ipAdressList.contains(globalVariables.getRemoteIpAdress())){
                    Log.d(TAG, "onProgressUpdate: Update" + globalVariables.getRemoteIpAdress());
                    ipAdressList.add(globalVariables.getRemoteIpAdress());
                    adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: canceld");
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void Void) {


            Log.d(TAG, "onPostExecute:  MyTask Abgeschlossen");

        }
        Server m_activity = null;
    }


}
