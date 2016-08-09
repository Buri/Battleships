package be.buri.battleships.Services;

import android.app.IntentService;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
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
public abstract class EngineService extends IntentService {
    /**
     * Players: beta version with only 2 players
     */
    public static boolean running = false;
    public static final int FPS = 10;
    public static Vector<Player> players = new Vector<Player>(2);
    public static Vector<Harbor> harbors = Const.getHarbors();
    public static Map<Integer, Unit> units = Collections.synchronizedMap(new HashMap<Integer, Unit>(16));

    public EngineService(String name) {
        super(name);
    }

    abstract void handleCommand(Command command);
    public abstract ConcurrentLinkedQueue getCommandQueue();
    public abstract Thread getGameThread();
    public abstract void setGameThread(Thread t);
    protected abstract void execute();

    class CommunicationThread implements Runnable {
        final EngineService service;
        private Socket clientSocket;

        public CommunicationThread(Socket clientSocket, EngineService serverService) {
            this.service = serverService;
            this.clientSocket = clientSocket;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted() && !clientSocket.isClosed()) {
                try {
                    Command command = Net.recieve(clientSocket.getInputStream());
                    if (null == command) {
                        clientSocket.close();
                        command = new Command();
                        command.name = Const.CMD_DISCONNECT;
                        Thread.currentThread().interrupt();
                    }
                    command.socket = this.clientSocket;
                    synchronized (service) {
                        service.getCommandQueue().add(command);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class GameThread implements Runnable {
        final EngineService service;

        public GameThread(EngineService service) {
            this.service = service;
        }

        @Override
        public void run() {
            while (true) {
                long start_time = System.nanoTime();

                // Begin loop code here
                while (!this.service.getCommandQueue().isEmpty()) {
                    Command command = (Command) this.service.getCommandQueue().poll();
                    handleCommand(command);
                }
                execute();
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
}
