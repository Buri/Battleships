package be.buri.battleships.Services;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;

/**
 * Created by buri on 1.8.16.
 */
public class ClientService extends EngineService {
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
                try {
                    socket = new Socket(intent.getStringExtra(HOST_NAME), ServerService.SERVERPORT);
                    System.out.println("Connected: " + Boolean.toString(socket.isConnected()));
                    if (socket.isConnected()) {
                        System.out.println("Connected");
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                        Command c = new Command();
                        c.name = Const.CMD_LIST_PLAYERS;
                        out.println(c);

                        //OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
                        //writer.write("From client, hello!");
                        //writer.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
