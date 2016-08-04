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
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;
import be.buri.battleships.Network.Net;
import be.buri.battleships.Player;

/**
 * Created by buri on 1.8.16.
 */
public class ServerService extends EngineService {
    private ServerSocket serverSocket;
    private Thread serverThread = null, gameThread = null;
    private ConcurrentLinkedQueue incommingCommands = new ConcurrentLinkedQueue();
    public static final int SERVERPORT = 6000;
    public static final int FPS = 3;
    public static boolean running = false;


    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (running) return;
        ;

        this.running = true;
        if (null == this.serverThread) {
            this.serverThread = new Thread(new ServerThread(this));
            this.serverThread.start();
        }
        if (null == this.gameThread) {
            gameThread = new Thread(new GameThread(this));
            gameThread.start();
        }
    }

    class GameThread implements Runnable {
        final ServerService service;

        public GameThread(ServerService service) {
            this.service = service;
        }

        private void respond(Command command, Object response) {

        }

        @Override
        public void run() {
            while (true) {
                long start_time = System.nanoTime();

                // Begin loop code here
                while (!this.service.incommingCommands.isEmpty()) {
                    Command command = (Command) this.service.incommingCommands.poll();
                    Log.d("BS.Server.handleCommand", command.name);
                    switch (command.name) {
                        case Const.CMD_LIST_PLAYERS:
                            handleListPlayers(command);
                            break;
                        case Const.CMD_IS_WARSHIPS:
                            handleIsWarships(command);
                            break;
                    }
                }
                // End loop code here

                long end_time = System.nanoTime();
                long difference = (long) (1000 / FPS - (end_time - start_time) / 1e6);
                try {
                    if (difference > 0) {
                        Thread.sleep(difference);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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
                    synchronized (service) {
                        service.players.add(new Player("Player " + (++counter)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        final ServerService service;
        private Socket clientSocket;

        public CommunicationThread(Socket clientSocket, ServerService serverService) {
            this.service = serverService;
            this.clientSocket = clientSocket;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Command command = Net.recieve(clientSocket.getInputStream());
                    command.socket = this.clientSocket;
                    synchronized (service) {
                        service.incommingCommands.add(command);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleListPlayers(Command command) {
        try {
            Command response = new Command();
            response.name = Const.CMD_LIST_PLAYERS;
            for (Player player : this.players) {
                response.arguments.add(player.getName());
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

    private String getHostname() {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return "localhost";
        }
    }
}
