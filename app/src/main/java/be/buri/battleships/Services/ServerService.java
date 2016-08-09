package be.buri.battleships.Services;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import be.buri.battleships.Activities.MapActivity;
import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;
import be.buri.battleships.Network.Helper;
import be.buri.battleships.Network.Net;
import be.buri.battleships.Player;
import be.buri.battleships.Units.Harbor;
import be.buri.battleships.Units.Ship;
import be.buri.battleships.Units.Unit;

/**
 * Created by buri on 1.8.16.
 */
public class ServerService extends EngineService {
    private ServerSocket serverSocket;
    private Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    public static Map<Integer, Socket> playerSocketMap = Collections.synchronizedMap(new HashMap<Integer, Socket>());
    protected static ConcurrentLinkedQueue incomingCommands = new ConcurrentLinkedQueue();
    protected static Thread gameThread = null;

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (running) return;

        this.running = true;
        if (null == this.serverThread) {
            this.serverThread = new Thread(new ServerThread(this));
            this.serverThread.start();
        }
        if (null == this.gameThread) {
            gameThread = new Thread(new GameThread(this));
            gameThread.start();
        }
        stopSelf();
    }

    class ServerThread implements Runnable {
        final ServerService service;

        public ServerThread(ServerService service) {
            this.service = service;
        }

        public void run() {
            int counter = 0;
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket, service);
                    new Thread(commThread).start();
                    Player newPlayer = new Player(++counter);
                    newPlayer.setColor(Helper.getRandomColor());
                    service.players.add(newPlayer);
                    service.playerSocketMap.put(newPlayer.getId(), socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleListPlayers(Command command) {
        try {
            Command response = new Command();
            response.name = Const.CMD_LIST_PLAYERS;
            response.arguments.add(this.players);
            for (Player player : this.players) {
                Log.d("BS.Server.listPlayers", player.getName() + (player.getHarbor() != null ? " / " + player.getHarbor().getName() : ""));
                if (playerSocketMap.get(player.getId()) == command.socket) {
                    Log.d("BS.Server.listPlayers", "Current player: " + player.getName());
                    response.arguments.add(player.getId());
                }
            }
            Net.respond(command, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIsWarships(Command command) {
        try {
            Command response = new Command();
            response.name = Const.CMD_IS_WARSHIPS_POSITIVE;
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            response.arguments.add(pInfo.versionName);
            Log.d("BS.Server.isWarships", pInfo.versionName);
            String hostname = getHostname();
            response.arguments.add(hostname);
            Log.d("BS.Server.isWarships", hostname);
            Net.respond(command, response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleDisconnect(Command command) {
        for (Player player : players) {
            if (command.socket == playerSocketMap.get(player.getId())) {
                players.remove(player);
                playerSocketMap.remove(player.getId());
                break;
            }
        }
        broadcastPlayerList();
    }

    private String getHostname() {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return "localhost";
        }
    }

    @Override
    void handleCommand(Command command) {
        Log.d("BS.Server.handleCommand", command.name);
        switch (command.name) {
            case Const.CMD_LIST_PLAYERS:
                handleListPlayers(command);
                break;
            case Const.CMD_IS_WARSHIPS:
                handleIsWarships(command);
                break;
            case Const.CMD_DISCONNECT:
                handleDisconnect(command);
                break;
            case Const.CMD_SET_PLAYER_NAME:
                for (Player player : players) {
                    if (playerSocketMap.get(player.getId()) == command.socket) {
                        player.setName((String) command.arguments.get(0));
                        for (Harbor harbor : harbors) {
                            if (harbor.getName().equals((String) command.arguments.get(1))) {
                                player.setHarbor(harbor);
                                break;
                            }
                        }
                        break;
                    }
                }
                handleListPlayers(command);
                break;
            case Const.CMD_REQUEST_NEW_UNIT:
                handleRequestNewUnit(command);
                break;
            case Const.CMD_REQUEST_UNIT_ORDER:
                Ship unit = ((Ship)units.get((int)command.arguments.get(0)));
                unit.setDestLat((double)command.arguments.get(1));
                unit.setDestLon((double)command.arguments.get(2));
                break;
        }
    }

    Random mRand = new Random();

    private void handleRequestNewUnit(Command command) {
        Command response = new Command();
        Ship unit = new Ship();
        Player player = findPlayerById(command.playerId);
        // Limit to 5 ships+
        if (player.getUnitCount() >= 7) {
            return;
        }
        unit.setPlayer(player);
        Harbor harbor = player.getHarbor();
        unit.setGpsE(harbor.getGpsE());
        unit.setGpsN(harbor.getGpsN());
        unit.setDestLat(mRand.nextDouble()*(MapActivity.LAT_MAX - MapActivity.LAT_MIN)+MapActivity.LAT_MIN);
        unit.setDestLon(mRand.nextDouble()*(MapActivity.LON_MAX - MapActivity.LON_MIN)+MapActivity.LON_MIN);
        units.put(unit.getId(), unit);
        response.name = Const.CMD_ADD_UNIT;
        response.arguments.add(unit);
        response.arguments.add(unit.getPlayer().getId());
        netBroadcast(response);
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

    private void broadcastPlayerList() {
        Command command = new Command();
        for (Player player : players) {
            command.socket = playerSocketMap.get(player.getId());
            if (null != command.socket && !command.socket.isClosed()) {
                handleListPlayers(command);
            }
        }
    }

    public void broadcastUnitUpdate(Unit unit) {
        Command command = new Command();
        command.name = Const.CMD_UPDATE_UNIT;
        command.arguments.add(unit);
        netBroadcast(command);
    }

    private void netBroadcast(Command command) {
        Iterator it = playerSocketMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Socket socket = ((Socket) pair.getValue());
            try {
                if (null != socket) {
                    Net.send(socket.getOutputStream(), command);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected Player findPlayerById(int i) {
        for (Player player : players) {
            if (player.getId() == i) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected void execute() {
        // Move units
        //Log.i("BS.Server.execute", "Running execute");
        Iterator it = units.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Unit unit = (Unit) pair.getValue();
            if (unit instanceof Ship) {
                if (((Ship) unit).isMoving()) {
                    ((Ship) unit).move(FPS);
                    Log.d("BS.Server.execute", Integer.toString(unit.getId()) + " (" + Double.toString(unit.getGpsN()) + ", " + Double.toString(unit.getGpsE()) + ")");
                    broadcastUnitUpdate(unit);
                }
            }
        }
    }
}
