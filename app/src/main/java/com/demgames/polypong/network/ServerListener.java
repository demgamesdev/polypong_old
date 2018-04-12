package com.demgames.polypong.network;

import com.demgames.polypong.Globals;
import com.demgames.polypong.network.sendclasses.SendPlayerName;
import com.demgames.polypong.packages.request.PingRequest;
import com.demgames.polypong.packages.response.PingResponse;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import android.content.Context;
import android.util.Log;

public class ServerListener extends Listener{

    Globals globalVariables;

    public ServerListener(Context myContext) {
        globalVariables=(Globals) myContext;
    }

    private static final String TAG = "ServerListener";

    @Override
    public void connected(Connection connection) {
        String tempIpAdress=connection.getRemoteAddressTCP().toString();
        tempIpAdress=tempIpAdress.substring(1,tempIpAdress.length()).split(":")[0];
        Log.e(TAG, tempIpAdress+" connected.");
        globalVariables.addToConnectionList(connection);

        if (globalVariables.addIpTolist(tempIpAdress)) {
            globalVariables.setUpdateListViewState(true);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        Log.e(TAG, "disconnected.");
    }

    @Override
    public void received(Connection connection,Object object) {
        Log.e(TAG, "Package received.");

        if(object instanceof PingRequest) {

            PingResponse pingResponse= new PingResponse();
            connection.sendTCP(pingResponse);
            Log.e(TAG, "Send PingResponse.");
        }
        else if(object instanceof SendPlayerName) {
            Log.d(TAG,"received PlayerName");
            SendPlayerName PlayerName=(SendPlayerName) object;
            String enemyName = PlayerName.PlayerName;
            Log.d(TAG, "received: EnemyName: " + enemyName);
            globalVariables.addPlayerNameTolist(enemyName);

            Log.d(TAG, "received: Name: " + globalVariables.getPlayerNamesList().get(0));
            Log.d(TAG, "received: Name: " + globalVariables.getPlayerNamesList());

        }
    }
}
