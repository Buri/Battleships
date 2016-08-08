package be.buri.battleships.Services;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;
import be.buri.battleships.Network.Net;
import be.buri.battleships.Player;
import be.buri.battleships.Units.Harbor;
import be.buri.battleships.Units.Unit;

/**
 * Created by buri on 1.8.16.
 */
public class ServerService extends EngineService {
    private ServerSocket serverSocket;
    private Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    public Map<Player, Socket> playerSocketMap = new HashMap<Player, Socket>();
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
                    service.players.add(newPlayer);
                    playerSocketMap.put(newPlayer, socket);
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
                if (playerSocketMap.get(player) == command.socket) {
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
            if (command.socket == playerSocketMap.get(player)) {
                players.remove(player);
                playerSocketMap.remove(player);
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
                    if (playerSocketMap.get(player) == command.socket) {
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
        }
    }

    private void handleRequestNewUnit(Command command) {
        Command response = new Command();
        Unit unit = new Unit();
        Player player = findPlayerById(command.playerId);
        unit.setPlayer(player);
        Harbor harbor = player.getHarbor();
        unit.setGpsE(harbor.getGpsE());
        unit.setGpsN(harbor.getGpsN());
        units.put(unit.getId(), unit);
        response.name = Const.CMD_ADD_UNIT;
        response.arguments.add(unit);
        response.arguments.add(unit.getPlayer().getId());
        try {
            Net.respond(command, response);
        } catch (IOException e) {
            e.printStackTrace();
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

    private void broadcastPlayerList() {
        Command command = new Command();
        for (Player player : players) {
            command.socket = playerSocketMap.get(player);
            if (null != command.socket && !command.socket.isClosed()) {
                handleListPlayers(command);
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
    protected Player findPlayerBySocket(Socket socket) {
        for (Player player : players) {
            if (playerSocketMap.get(player) == socket) {
                return player;
            }
        }
        return null;
    }
}
