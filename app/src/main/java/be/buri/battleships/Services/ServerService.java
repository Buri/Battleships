package be.buri.battleships.Services;

import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import be.buri.battleships.Activities.PlayerListActivity;

/**
 * Created by buri on 1.8.16.
 */
public class ServerService extends EngineService {
    private ServerSocket serverSocket;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;


    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (null == this.serverThread) {
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket) {
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
                    System.out.println("state: " + Boolean.toString(input.ready()) + ", " + Boolean.toString(read == null));
                    System.out.println("RCV: " + read);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
