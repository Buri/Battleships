package be.buri.battleships.Services;

import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import be.buri.battleships.Engine.Command;
import be.buri.battleships.Engine.Const;
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


    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
        ServerService service;

        public GameThread(ServerService service) {
            this.service = service;
        }

        @Override
        public void run() {
            while (true) {
                long start_time = System.nanoTime();

                // Begin loop code here
                while (!this.service.incommingCommands.isEmpty()) {
                    Command command = (Command) this.service.incommingCommands.poll();
                    System.out.println("Command: " + command);
                    switch (command.name) {
                        case Const.CMD_LIST_PLAYERS:
                            try {
                                Command response = new Command();
                                response.name = Const.CMD_LIST_PLAYERS;
                                for (Player player : service.players) {
                                    response.arguments.add(player.getName());
                                }
                                command.socket.getOutputStream().write(response.toString().getBytes());
                                command.socket.getOutputStream().flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
        ServerService service;

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
                    synchronized (service.players) {
                        service.players.add(new Player("Player " + (++counter)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        ServerService service;
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket, ServerService serverService) {
            this.service = serverService;
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (null == read) {
                        this.clientSocket.close();
                        return;
                    }
                    Command command = Command.fromString(read);
                    command.socket = this.clientSocket;
                    synchronized (service.incommingCommands) {
                        service.incommingCommands.add(command);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
