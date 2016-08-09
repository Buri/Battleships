package be.buri.battleships.Services;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;
import be.buri.battleships.Network.Net;
import be.buri.battleships.Player;
import be.buri.battleships.Units.Harbor;
import be.buri.battleships.Units.Ship;
import be.buri.battleships.Units.Unit;

/**
 * Created by buri on 1.8.16.
 */
public class ClientService extends EngineService {
    public final static int CONNECT_TO_HOST = 0;
    public final static int FIND_SERVERS = 1;
    public final static int SET_PLAYER_NAME = 2;
    public final static int GET_PLAYER_LIST = 3;
    public final static int REQUEST_NEW_UNIT = 4;
    public final static int UNIT_ORDER_UPDATE = 5;
    public final static String INTENT_TYPE = "IntentType";
    public final static String HOST_NAME = "HostName";
    public final static String INTENT_SELECT_HARBOR = "SelectHarbor";
    private final IBinder mBinder = new ClientBinder();
    private static Player currentPlayer;
    private static Socket socket = new Socket();
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
        Command command = new Command();
        switch (intent.getIntExtra(INTENT_TYPE, -1)) {
            case CONNECT_TO_HOST:
                String hostname = intent.getStringExtra(HOST_NAME);
                for (int i = 0; i < 10; i++) {
                    try {
                        socket.connect(new InetSocketAddress(hostname, ServerService.SERVERPORT));
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
                        send(socket.getOutputStream(), serverCheck);
                        sendBroadcast(new Intent(INTENT_SELECT_HARBOR));
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
                command.name = Const.CMD_SET_PLAYER_NAME;
                command.arguments.add(currentPlayer.getName());
                command.arguments.add(currentPlayer.getHarbor().getName());
                try {
                    send(socket.getOutputStream(), command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case GET_PLAYER_LIST:
                command.name = Const.CMD_LIST_PLAYERS;
                try {
                    send(socket.getOutputStream(), command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_NEW_UNIT:
                command.name = Const.CMD_REQUEST_NEW_UNIT;
                command.arguments.add("Ship");
                command.playerId = currentPlayer.getId();
                try {
                    send(socket.getOutputStream(), command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case UNIT_ORDER_UPDATE:
                command.name = Const.CMD_REQUEST_UNIT_ORDER;
                command.arguments.add(intent.getIntExtra("UNIT_ID", -1));
                command.arguments.add(intent.getDoubleExtra("LAT", 0));
                command.arguments.add(intent.getDoubleExtra("LON", 0));
                try {
                    send(socket.getOutputStream(), command);
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
        for (Harbor harbor : harbors) {
            Log.d("BS.Client.hListPlayer", harbor.getName() + " " + (harbor.getPlayer() != null ? harbor.getPlayer().getName() : "---"));
        }
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
                break;
            case Const.CMD_ADD_UNIT: {
                Unit unit = (Unit) command.arguments.get(0);
                int playerId = (int) command.arguments.get(1);
                for (Player player : players) {
                    if (player.getId() == playerId) {
                        unit.setPlayer(player);
                        break;
                    }
                }
                this.units.put(unit.getId(), unit);
                Intent intent = new Intent(Const.BROADCAST_ADD_UNITS);
                intent.putExtra("UNIT_ID", unit.getId());
                sendBroadcast(intent);
            }
            break;
            case Const.CMD_UPDATE_UNIT: {
                Unit unit = (Unit) command.arguments.get(0), old = this.units.get(unit.getId());
                old.update(unit);
                Log.d("BS.Client.updateUnit", Integer.toString(old.getId()) + " (" + Double.toString(old.getGpsN()) + ", " + Double.toString(old.getGpsE()) + ")");
                Intent intent = new Intent(Const.BROADCAST_UPDATE_UNITS);
                intent.putExtra("UNIT_ID", unit.getId());
                sendBroadcast(intent);
            }
            break;
            case Const.CMD_REMOVE_UNIT: {
                Unit unit = (Unit) command.arguments.get(0);
                this.units.remove(unit.getId());
            }
            break;
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

    public void requestNewUnit(String unit)
    {
        Intent intent = new Intent(this, this.getClass());
        intent.putExtra(INTENT_TYPE, REQUEST_NEW_UNIT);
        intent.putExtra("UNIT_TYPE", unit);
        startService(intent);
    }

    public void send(OutputStream stream, Command command) {
        if (null != currentPlayer) {
            command.playerId = currentPlayer.getId();
        }
        Net.send(stream, command);
    }

    public static Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Ship findShipByMarker(Marker marker) {
        Iterator it = units.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Unit unit = (Unit) pair.getValue();
            Log.d("BS.Client.FSM", unit.getMarker().getId() + " == " + marker.getId());
            if (unit.getMarker().getId().equals(marker.getId())) {
                return (Ship)unit;
            }
        }
        return null;
    }

    public void orderUnitMovement(LatLng position, Ship unit) {
        Intent intent = new Intent(this, this.getClass());
        intent.putExtra(INTENT_TYPE, UNIT_ORDER_UPDATE);
        intent.putExtra("UNIT_ID", unit.getId());
        intent.putExtra("LAT", position.latitude);
        intent.putExtra("LON", position.longitude);
        startService(intent);
    }

    @Override
    protected void execute() {
        // Noop
    }
}
