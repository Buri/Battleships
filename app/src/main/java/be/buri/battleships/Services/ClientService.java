package be.buri.battleships.Services;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Vector;

import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;
import be.buri.battleships.Network.Net;
import be.buri.battleships.Player;
import be.buri.battleships.Units.Harbor;

/**
 * Created by buri on 1.8.16.
 */
public class ClientService extends EngineService {
    public static boolean running = false;
    public final static int CONNECT_TO_HOST = 0;
    public final static int FIND_SERVERS = 1;
    public final static int SET_PLAYER_NAME = 2;
    public final static String INTENT_TYPE = "IntentType";
    public final static String HOST_NAME = "HostName";
    private final IBinder mBinder = new ClientBinder();
    private final Player currentPlayer;
    private Socket socket = null;
    private boolean working = false;
    public ArrayList<ServerInfo> localNetworkServers = new ArrayList<>();
    public Vector<Harbor> harbors = Const.getHarbors();

    public class ClientBinder extends Binder {
        public ClientService getService() {
            return ClientService.this;
        }
    }

    public ClientService() {
        super("ClientService");
        running = true;
        currentPlayer = new Player("Your Name");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        working = true;
        Log.d("BS.Client.intent", Integer.toString(intent.getIntExtra(INTENT_TYPE, -1)));
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
                        Command serverCheck = new Command();
                        serverCheck.name = Const.CMD_IS_WARSHIPS;
                        Net.send(socket.getOutputStream(), serverCheck);
                        Command response = Net.recieve(socket.getInputStream());
                        String version = (String) response.arguments.get(1);
                        if (!Const.CMD_IS_WARSHIPS_POSITIVE.equals(response.name)){
                            // exit
                        }

                        Command c = new Command();
                        c.name = Const.CMD_LIST_PLAYERS;
                        Net.send(socket.getOutputStream(), c);
                        response = Net.recieve(socket.getInputStream());
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
            case FIND_SERVERS:
                getServersOnLocalNetwork();
                break;
            case SET_PLAYER_NAME:
                currentPlayer.setName(intent.getStringExtra("Player name"));
                for (Harbor item : harbors) {
                    if (item.getName().equals(intent.getStringExtra("Harbor name")) ) {
                        item.setPlayer(currentPlayer);
                        break;
                    }
                }
                break;
        }
        working = false;
    }

    public void getServersOnLocalNetwork() {
        localNetworkServers.clear();
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        for (byte i = 24; i < 28; i++) {
            String ipString = String.format(
                    "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (i));
            try {
                Log.d("BS.Client.findServers", ipString);
                Socket tmp = new Socket();
                SocketAddress ep = new InetSocketAddress(ipString, ServerService.SERVERPORT);
                tmp.connect(ep, 1000);
                if (tmp.isConnected()) {
                    Command c = new Command();
                    c.name = Const.CMD_IS_WARSHIPS;
                    Net.send(tmp.getOutputStream(), c);
                    Command response = Net.recieve(tmp.getInputStream());
                    tmp.close();
                    ServerInfo si = new ServerInfo();
                    si.version = response.arguments.get(0).toString();
                    si.name = response.arguments.get(1).toString();
                    si.ip = ipString;
                    localNetworkServers.add(si);
                    Log.d("BS.Client.findServers", si.name + "(" + si.ip + "): " + si.version);
                }
            } catch (ConnectException e) {
                // Noop, we are just trying servers
            } catch (SocketTimeoutException e) {
                // Noop, we are just trying servers
                // MOCK
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        ServerInfo si = new ServerInfo();
        si.version = "1.0.1";
        si.name = "localhost";
        si.ip = "127.0.0.1";
        localNetworkServers.add(si);
    }


    public boolean isWorking()
    {
        return working;
    }

    public class ServerInfo {
        public String name, version, ip;
    }
}
