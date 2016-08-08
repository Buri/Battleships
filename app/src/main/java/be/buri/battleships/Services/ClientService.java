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
import java.util.concurrent.ConcurrentLinkedQueue;

import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;
import be.buri.battleships.Network.Net;
import be.buri.battleships.Player;
import be.buri.battleships.Units.Harbor;

/**
 * Created by buri on 1.8.16.
 */
public class ClientService extends EngineService {
    public final static int CONNECT_TO_HOST = 0;
    public final static int FIND_SERVERS = 1;
    public final static int SET_PLAYER_NAME = 2;
    public final static int GET_PLAYER_LIST = 3;
    public final static String INTENT_TYPE = "IntentType";
    public final static String HOST_NAME = "HostName";
    private final IBinder mBinder = new ClientBinder();
    private static Player currentPlayer;
    private static Socket socket = null;
    private boolean working = false;
    public static ArrayList<ServerInfo> localNetworkServers = new ArrayList<>();
    protected static ConcurrentLinkedQueue incomingCommands = new ConcurrentLinkedQueue();
    protected static Thread gameThread = null;


    public class ClientBinder extends Binder {
        public ClientService getService() {
            return ClientService.this;
        }
    }

    public ClientService() {
        super("ClientService");
        running = true;
        currentPlayer = new Player(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        working = true;
        if (null == this.gameThread) {
            gameThread = new Thread(new GameThread(this));
            gameThread.start();
        }
        Log.d("BS.Client.intent", Integer.toString(intent.getIntExtra(INTENT_TYPE, -1)));
        switch (intent.getIntExtra(INTENT_TYPE, -1)) {
            case CONNECT_TO_HOST:
                String hostname = intent.getStringExtra(HOST_NAME);
                for (int i = 0; i < 10; i++) {
                    try {
                        socket = new Socket(hostname, ServerService.SERVERPORT);
                        break;
                    } catch (ConnectException e) {
                        Log.i("BS.Net.connect", "Connection refused (" + hostname + ")");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (socket.isConnected()) {
                        Log.d("Net", "Client connected");
                        CommunicationThread commThread = new CommunicationThread(socket, this);
                        new Thread(commThread).start();
                        Command serverCheck = new Command();
                        serverCheck.name = Const.CMD_IS_WARSHIPS;
                        Net.send(socket.getOutputStream(), serverCheck);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case FIND_SERVERS:
                getServersOnLocalNetwork();
                break;
            case SET_PLAYER_NAME:
                currentPlayer.setName(intent.getStringExtra("Player name"));
                for (Harbor item : harbors) {
                    if (item.getName().equals(intent.getStringExtra("Harbor name"))) {
                        item.setPlayer(currentPlayer);
                        break;
                    }
                }
                Command command = new Command();
                command.name = Const.CMD_SET_PLAYER_NAME;
                command.arguments.add(currentPlayer.getName());
                command.arguments.add(currentPlayer.getHarbor().getName());
                try {
                    Net.send(socket.getOutputStream(), command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case GET_PLAYER_LIST:
                Command c = new Command();
                c.name = Const.CMD_LIST_PLAYERS;
                try {
                    Net.send(socket.getOutputStream(), c);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        working = false;
    }

    private void handleListPlayers(Command response) throws IOException, ClassNotFoundException {
        Vector<Player> resp = (Vector<Player>) response.arguments.get(0);
        for (Player player : resp) {
            if (!players.contains(player)) {
                players.add(player);
            }
            Log.d("BS.Client", player.getName());
            if (response.arguments.size() == 2 && player.getId() == (int) response.arguments.get(1)) {
                currentPlayer = player;
            }
            for (Player p : players) {
                if (p.getId() == player.getId()) {
                    p.update(player);
                    break;
                }
            }
        }
        if (players.size() > resp.size()) {
            for (Player p : players) {
                if (!resp.contains(p)) {
                    players.remove(p);
                }
            }
        }
        Intent intent = new Intent(Const.BROADCAST_UPDATE_PLAYER_LIST);
        intent.setType("text/plain");
        sendBroadcast(intent);
    }

    public void getServersOnLocalNetwork() {
        localNetworkServers.clear();
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        for (int i = 125; i < 132; i++) {
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
                    tmp.close();
                }
            } catch (ConnectException e) {
                // Noop, we are just trying servers
            } catch (SocketTimeoutException e) {
                // Noop, we are just trying servers
                // MOCK
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ServerInfo si = new ServerInfo();
        si.version = "1.0.1";
        si.name = "localhost";
        si.ip = "127.0.0.1";
        localNetworkServers.add(si);
    }

    public Vector<Player> getPlayers() {
        Intent intent = new Intent(this, ClientService.class);
        intent.putExtra(ClientService.INTENT_TYPE, ClientService.GET_PLAYER_LIST);
        startService(intent);

        return this.players;
    }


    public boolean isWorking() {
        return working;
    }

    public class ServerInfo {
        public String name, version, ip;
    }

    @Override
    void handleCommand(Command command) {
        Log.d("BS.Client.handleCommand", command.name + " (" + command.arguments.size() + ")");
        switch (command.name) {
            case Const.CMD_LIST_PLAYERS:
                try {
                    handleListPlayers(command);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case Const.CMD_IS_WARSHIPS_POSITIVE:
                ServerInfo si = new ServerInfo();
                si.version = command.arguments.get(0).toString();
                si.name = command.arguments.get(1).toString();
                si.ip = command.socket.getRemoteSocketAddress().toString();
                localNetworkServers.add(si);
                Log.d("BS.Client.findServers", si.name + "(" + si.ip + "): " + si.version);
        }
    }

    @Override
    public ConcurrentLinkedQueue getCommandQueue() {
        return this.incomingCommands;
    }

    @Override
    public Thread getGameThread() {
        return gameThread;
    }

    @Override
    public void setGameThread(Thread t) {
        gameThread = t;
    }
}
