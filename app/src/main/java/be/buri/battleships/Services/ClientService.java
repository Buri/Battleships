package be.buri.battleships.Services;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;
import be.buri.battleships.Network.Net;

/**
 * Created by buri on 1.8.16.
 */
public class ClientService extends EngineService {
    public static boolean running = false;
    public final static int CONNECT_TO_HOST = 0;
    public final static String INTENT_TYPE = "IntentType";
    public final static String HOST_NAME = "HostName";
    private final IBinder mBinder = new ClientBinder();
    private Socket socket = null;

    public class ClientBinder extends Binder {
        ClientService getService() {
            return ClientService.this;
        }
    }

    public ClientService() {
        super("ClientService");
        running = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getIntExtra(INTENT_TYPE, -1)) {
            case CONNECT_TO_HOST:
                for (int i = 0; i < 10; i++) {
                    try {
                        socket = new Socket(intent.getStringExtra(HOST_NAME), ServerService.SERVERPORT);
                        break;
                    } catch (ConnectException e) {
                        Log.i("BS.Net.connect", "Connection refused (" + i + ")");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (socket.isConnected()) {
                        Log.d("Net", "Client connected");
                        Command c = new Command();
                        c.name = Const.CMD_LIST_PLAYERS;
                        c.arguments.add(3);
                        Net.send(socket.getOutputStream(), c);
                        Command response = Net.recieve(socket.getInputStream());
                        for (Object name : response.arguments) {
                            Log.d("BS.Client", name.toString());
                        }
                        Net.send(socket.getOutputStream(), c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
