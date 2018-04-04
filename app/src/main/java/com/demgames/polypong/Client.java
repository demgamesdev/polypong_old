package com.demgames.polypong;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Intent;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import oscP5.*;
import netP5.*;


public class Client extends AppCompatActivity {

    private static final String TAG = "Client";
    private MyTaskClient MyTaskClient1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Todo proper network handling in asynctask or thread
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }



        //Vollbildmodus
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_client);

        /***Deklarationen***/
        final Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.setConnectState(false);
        globalVariables.setReadyStateState(false);
        globalVariables.setSettingsState(false);
        globalVariables.setGameLaunched(false);
        globalVariables.setRemoteIpAdress(null);

        globalVariables.setMyIpList(new String[] {});
        //globalVariables.setArrayAdapter(new ArrayAdapter<String>
                //(this, R.layout.listview, globalVariables.getMyIpList()));

        globalVariables.setOscP5(new OscP5(this, globalVariables.getMyPort()));

        final TextView myIpTextView = (TextView) findViewById(R.id.IpAdressTextView);
        final Button devBtn = (Button) findViewById(R.id.devButton);
        final ListView ClientLV = (ListView) findViewById(R.id.ClientListView);

        //ClientLV.setAdapter(globalVariables.getArrayAdapter());


        //Thread für den Verbindungsaufbau
        MyTaskClient1 = new MyTaskClient();
        MyTaskClient1.execute();


        //automatically detect ip if available, create new thread for searching ip
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(1000);

                        //Eigene IP-Adresse
                        myIpTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (checkIfIp(globalVariables.getMyIpAdress())) {
                                    myIpTextView.setText("Deine IP-Adresse lautet: " + globalVariables.getMyIpAdress());
                                    //IP Adresse wird in die Liste Hinzugefügt
                                    //globalVariables.addIpTolist(globalVariables.getMyIpAdress());

                                } else {
                                    myIpTextView.setText("Unable to get Ip-Adress");
                                }
                                //globalVariables.updateListView();

                            }
                        });


                    }

                } catch (InterruptedException e) {
                    Log.d(Client.class.getSimpleName(),"Eigene Ip-Adresse Thread interrupted");
                    e.printStackTrace();
                }

            }
        };

        globalVariables.setUpdateThread(updateRunnable);
        globalVariables.getUpdateThread().start();


        /***Developer Button Listener***/
        /*devBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String balls = "20";
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                //Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("myipadress", globalVariables.getMyIpAdress());
                startGame.putExtra("ballnumber", balls);
                startGame.putExtra("mode", "client");
                startActivity(startGame);
            }
        });*/

    }



    @Override
    protected void onDestroy() {

        MyTaskClient1.cancel(true);
        Log.d(TAG, "onDestroy: MyTask beendet");
        final Globals globalVariables=(Globals) getApplication();
        globalVariables.stopOscP5();
        Log.d(TAG, "onDestroy: OscP5 beendet");
        globalVariables.interruptUpdateThread();
        Log.d(TAG, "onDestroy: Activity geschlossen");

        super.onDestroy();
    }

    /********* OSCP5 EVENTHANDLER *********/

    void oscEvent(OscMessage theOscMessage) {
        final String TAG = "MyActivity";
        final Globals globalVariables=(Globals) getApplication();
        switch(theOscMessage.addrPattern()) {
            case "/hostconnect":
                //Log.d(Client.class.getSimpleName(),"oscP5 received hostconnect");
                String remoteIpAdress = theOscMessage.get(0).stringValue();
                if(checkIfIp(remoteIpAdress)) {
                    //globalVariables.setConnectState(true);
                    //globalVariables.addIpTolist(remoteIpAdress);
                    globalVariables.setRemoteIpAdress(remoteIpAdress);
                }
                //sendClientConnect();
                break;

            case "/settings":
                Log.d(Client.class.getSimpleName(),"oscP5 received settings");
                //value=theOscMessage.get(0).intValue();

                globalVariables.setNumberOfBalls(theOscMessage.get(0).stringValue());
                globalVariables.setFriction(theOscMessage.get(1).floatValue());
                globalVariables.setGameMode(theOscMessage.get(2).intValue());

                //Spielername empfangen und speichern
                String[] playerName = new String[2];
                List<String> nameList = new ArrayList<String>();
                nameList=globalVariables.getPlayerNamesList();
                playerName[0] = nameList.get(0); //Eigener Name
                playerName[1] = (theOscMessage.get(3).stringValue());

                Log.d(TAG, "oscEvent: Name" +playerName[0] + playerName[1]);

                globalVariables.setPlayerNamesList(playerName);



                if(theOscMessage.get(4).intValue()==1) {
                    globalVariables.setGravityState(true);
                }
                if(theOscMessage.get(5).intValue()==1) {
                    globalVariables.setAttractionState(true);
                }

                globalVariables.setBalls(false);
                int offset=6;
                for (int i=0; i<Integer.parseInt(globalVariables.getNumberOfBalls());i++) {
                    globalVariables.setBallsXPositions(i,theOscMessage.get(offset+i).floatValue());
                    globalVariables.setBallsYPositions(i,theOscMessage.get(offset+Integer.parseInt(globalVariables.getNumberOfBalls())+i).floatValue());
                    globalVariables.setBallsSizes(i,theOscMessage.get(offset+2*Integer.parseInt(globalVariables.getNumberOfBalls())+i).floatValue());

                }

                //Log.d(Client.class.getSimpleName(),"+++++++++++++oscP5 received ball settings ++++++++++++"+Boolean.toString(theOscMessage.get(3).booleanValue()));
                for (int i=0; i<Integer.parseInt(globalVariables.getNumberOfBalls());i++) {
                    //Log.d(Client.class.getSimpleName(),"oscP5 received ball settings "+Boolean.toString(theOscMessage.get(3).booleanValue()));
                }


                Log.d(TAG, "oscEvent: Client Gamemode" + theOscMessage.get(2).stringValue());
                //globalVariables.setGravity(theOscMessage.get(3).booleanValue());
                Log.d(Client.class.getSimpleName(),"+++++++++++++Friction="+Float.toString(globalVariables.getFriction()));

                globalVariables.setSettingsState(true);
                break;

            case "/hostready":
                Log.d(Client.class.getSimpleName(),"oscP5 received hostready");
                Log.d(TAG, "oscEvent: hostready - spiel wird gestartet");
                if(!globalVariables.getGameLaunched()) {
                    globalVariables.stopOscP5();
                    Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                    //Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                    startGame.putExtra("mode", "client");
                    startActivity(startGame);
                    //globalVariables.myThread.stop();
                    globalVariables.interruptUpdateThread();
                    globalVariables.stopOscP5();
                    globalVariables.setGameLaunched(true);
                    MyTaskClient1.cancel(true);
                    finish();
                }

                globalVariables.setReadyStateState(true);
                break;
        }
    }

    /********* SEND FUNCTIONS *********/

    void sendClientConnect() {
        Globals globalVariables = (Globals) getApplicationContext();
        if ( globalVariables.getMyIpAdress()==null){
            Log.e(TAG, "sendClientConnect: myIP == null ");
        }
        else{
            NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
            //Log.d(Client.class.getSimpleName(),"oscP5 send clientconnect");
            OscMessage connectMessage = new OscMessage("/clientconnect");
            connectMessage.add(globalVariables.getMyIpAdress());
            //Log.e(TAG, "sendClientConnect: Message " + connectMessage );
            globalVariables.getOscP5().send(connectMessage, myRemoteLocation);
        }


    }
    void sendClient2Host() {

        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        Log.d(Client.class.getSimpleName(),"oscP5 send client2host");
        OscMessage connectMessage = new OscMessage("/clientTohost");
        connectMessage.add(globalVariables.getMyIpAdress());
        Log.d(Client.class.getSimpleName(),"oscP5 send client2host 3 " + connectMessage);
        globalVariables.getOscP5().send(connectMessage, myRemoteLocation);
    }


    void sendClientReady() {
        Log.d(Client.class.getSimpleName(),"oscP5 send clientready");
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        OscMessage readyMessage = new OscMessage("/clientready");
        readyMessage.add(globalVariables.getMyIpAdress());
        globalVariables.getOscP5().send(readyMessage, myRemoteLocation);
    }

    void sendSettings() {
        Log.d(Client.class.getSimpleName(),"oscP5 send settings");
        Globals globalVariables = (Globals) getApplicationContext();
        NetAddress myRemoteLocation=new NetAddress(globalVariables.getRemoteIpAdress(),globalVariables.getMyPort());
        OscMessage settingsMessage = new OscMessage("/settings");
        //Namen an Server senden
        List<String> playerName = new ArrayList<String>();
        playerName = globalVariables.getPlayerNamesList();

        Log.d(Client.class.getSimpleName(),"oscP5 send "+playerName.get(0));
        settingsMessage.add(playerName.get(0));
        globalVariables.getOscP5().send(settingsMessage, myRemoteLocation);
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
            ipAddressString = null;
        }

        return ipAddressString;
    }

    boolean checkIfIp(String teststring) {
        if(teststring != null) {
            String[] parts = teststring.split("\\."); //String wird bei jedem Punkt gesplittet
            if (parts.length == 4) {                        //String muss aus 4 Teilen bestehen
                return (true);
            }
        }

        return (false);

    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            Globals globalVariables = (Globals) getApplicationContext();
            //globalVariables.interruptUpdateThread();
            //globalVariables.stopOscP5();

        }
        return super.onKeyDown(keyCode, event);
    }*/



    /********* Thread Function - Searching IP and displaying *********/
    class MyTaskClient extends AsyncTask<Void,Void,Void> {

        Globals globalVariables = (Globals) getApplicationContext();
        String[] ListElements = new String[]{};
        List<String> ipAdressList = new ArrayList<String>(Arrays.asList(ListElements));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (Client.this, R.layout.listview, ipAdressList);

        @Override
        protected Void doInBackground(Void... voids) {
            //Background Thread

            Log.d(TAG, "doInBackground: Anfang Suche");
            globalVariables.setMyIpAdress(wifiIpAddress(getApplicationContext()));

            while (!globalVariables.getConnectState()) {
                //sendClientConnect();
                try {
                    Thread.currentThread().sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress();
            }


            Log.d(TAG, "doInBackground: Ende Suche");

            Log.d(TAG, "onPostExecute: Anfang Settings Senden");
            sendSettings();
            /*if(globalVariables.getSettingsState()) {
                sendSettings();
                Log.d(TAG, "onPostExecute: Sending Settings");
            }*/
            Log.d(TAG, "onPostExecute: Ende Settings Senden");
            sendClientReady();

            Log.d(TAG, "onPostExecute:  MyTask Abgeschlossen");

            return null;
        }

        @Override
        protected void onPreExecute() {

                //Vor dem Thread Initialisierung
                ListView ServerLV = (ListView) findViewById(R.id.ClientListView);
                ServerLV.setAdapter(adapter);
                //globalVariables.setSearchConnecState(true);
                ServerLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Toast.makeText(Client.this, globalVariables.getMyIpList().get(i), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onItemClick: " + Integer.toString(i));
                        Log.d(TAG, "onItemClick: " + ipAdressList.get(i));
                        Toast.makeText(Client.this, "Zu \"" + ipAdressList.get(i) + "\" wird verbunden", Toast.LENGTH_SHORT).show();
                        globalVariables.setConnectState(true);
                        globalVariables.setRemoteIpAdress(ipAdressList.get(i));
                        sendClientConnect();
                    }
                });


        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: Asynctask canceled");
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

            //Neue IP Adresse wird in die Listview geschrieben
            if(globalVariables.getRemoteIpAdress()!=null){
                if(!ipAdressList.contains(globalVariables.getRemoteIpAdress())){
                    Log.d(TAG, "onProgressUpdate: Update neue IP: " + globalVariables.getRemoteIpAdress());
                    ipAdressList.add(globalVariables.getRemoteIpAdress());
                    adapter.notifyDataSetChanged();
                    globalVariables.setMyIpList(ListElements);
                    Log.d(TAG, "onProgressUpdate: IP-Liste: " + ipAdressList);
                }
            }

        }

        //@Override
        protected void onPostExecute(Void Void) {
            Client m_activity = null;
        }

    }


}