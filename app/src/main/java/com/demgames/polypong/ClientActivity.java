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

import com.demgames.polypong.network.ClientListener;
import com.demgames.polypong.network.ServerListener;
import com.demgames.polypong.network.sendclasses.SendBallKinetics;
import com.demgames.polypong.packages.request.PingRequest;
import com.demgames.polypong.packages.response.PingResponse;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;

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


public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "Client";
    private MyTaskClient MyTaskClient;

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
        globalVariables.setConnectionList(new Connection[]{});
        globalVariables.setIpAdressList(new String[] {});

        globalVariables.setMyPlayerScreen(1);


        //globalVariables.setArrayAdapter(new ArrayAdapter<String>
                //(this, R.layout.listview, globalVariables.getMyIpList()));

        //kryostuff--------------------------------------
        globalVariables.getClient().start();

        globalVariables.setClientListener(getApplicationContext());
        globalVariables.getClient().addListener(globalVariables.getClientListener());

        globalVariables.registerKryoClasses(globalVariables.getClient().getKryo());

        //--------------------------------------------------
        final TextView myIpTextView = (TextView) findViewById(R.id.IpAdressTextView);
        final Button devBtn = (Button) findViewById(R.id.devButton);
        final ListView ClientLV = (ListView) findViewById(R.id.ClientListView);

        //ClientLV.setAdapter(globalVariables.getArrayAdapter());


        //Thread für den Verbindungsaufbau
        MyTaskClient = new MyTaskClient();
        MyTaskClient.execute();

        //automatically detect ip if available, create new thread for searching ip
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(1000);

                        globalVariables.setHostsList(globalVariables.getClient().discoverHosts(11000,2000));
                        if(globalVariables.getHostsList().toArray().length!=0) {
                            for (int i = 0; i < globalVariables.getHostsList().toArray().length; i++) {
                                String tempIPAdress = globalVariables.getHostsList().toArray()[i].toString();
                                tempIPAdress = tempIPAdress.substring(1, tempIPAdress.length());
                                Log.d("discovery", tempIPAdress);
                                if (globalVariables.addIpTolist(tempIPAdress)) {
                                    globalVariables.setUpdateListViewState(true);
                                }
                            }
                        }

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
                    Log.d(ClientActivity.class.getSimpleName(),"Eigene Ip-Adresse Thread interrupted");
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

        MyTaskClient.cancel(true);
        Log.d(TAG, "onDestroy: MyTask beendet");
        final Globals globalVariables=(Globals) getApplication();
        //globalVariables.getClient().stop();
        //Log.d(TAG, "onDestroy: Kryoclient stopped");
        globalVariables.interruptUpdateThread();
        Log.d(TAG, "onDestroy: Activity geschlossen");

        super.onDestroy();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            Globals globalVariables = (Globals) getApplicationContext();
            globalVariables.getClient().stop();
        }
        return super.onKeyDown(keyCode, event);
    }



    /********* Thread Function - Searching IP and displaying *********/
    class MyTaskClient extends AsyncTask<Void,Void,Void> {

        Globals globalVariables = (Globals) getApplicationContext();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (ClientActivity.this, R.layout.listview, globalVariables.getIpAdressList());

        @Override
        protected Void doInBackground(Void... voids) {
            //Background Thread

            Log.d(TAG, "doInBackground: Anfang Suche");


            while (!globalVariables.getConnectState()&& !isCancelled()) {
                //sendClientConnect();
                try {
                    Thread.currentThread().sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                globalVariables.setMyIpAdress(wifiIpAddress(getApplicationContext()));
                publishProgress();
            }


            Log.d(TAG, "doInBackground: Ende Suche");

            Log.d(TAG, "onPostExecute: Anfang Settings Senden");

            while(!globalVariables.getReadyState() && !isCancelled()) {

            }

            if(!isCancelled()) {
                globalVariables.getClient().removeListener(globalVariables.getClientListener());
                Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                //Intent startGame = new Intent(getApplicationContext(), gamelaunch.class);
                startGame.putExtra("mode", "client");
                startActivity(startGame);
                //globalVariables.myThread.stop();
                globalVariables.interruptUpdateThread();
                globalVariables.setGameLaunched(true);
                MyTaskClient.cancel(true);
                finish();

                Log.d(TAG, "onPostExecute: Ende Settings Senden");

                Log.d(TAG, "onPostExecute:  MyTask Abgeschlossen");
            } else {
                Log.d(TAG,"skipped do in background due to cancelling");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

                //Vor dem Thread Initialisierung
                ListView ClientLV = (ListView) findViewById(R.id.ClientListView);
                ClientLV.setAdapter(adapter);
                //globalVariables.setSearchConnecState(true);
                ClientLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Toast.makeText(Client.this, globalVariables.getMyIpList().get(i), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onItemClick: " + Integer.toString(i));
                        Log.d(TAG, "onItemClick: " + globalVariables.getIpAdressList().get(i));
                        Toast.makeText(ClientActivity.this, "Zu \"" + globalVariables.getIpAdressList().get(i) + "\" wird verbunden", Toast.LENGTH_SHORT).show();
                        globalVariables.setConnectState(true);
                        globalVariables.setRemoteIpAdress(globalVariables.getIpAdressList().get(i));

                        try {
                            globalVariables.getClient().connect(5000,globalVariables.getRemoteIpAdress(),11000,11000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        globalVariables.getClient().sendTCP(new PingRequest());

                        //sendClientConnect();
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

            if(globalVariables.getUpdateListViewState()) {
                adapter.notifyDataSetChanged();
                globalVariables.setUpdateListViewState(false);
                //
            }

        }

        //@Override
        protected void onPostExecute(Void Void) {
            ClientActivity m_activity = null;
        }

    }


}