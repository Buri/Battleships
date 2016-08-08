package be.buri.battleships.Network;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import be.buri.battleships.Engine.Command;

/**
 * Created by buri on 4.8.16.
 */
public class Net {
    @Nullable
    public static Command recieve(InputStream input) throws IOException, ClassNotFoundException {
        ObjectInputStream ois;
        try{
             ois = new ObjectInputStream(input);
        } catch (EOFException e) {
            return null;
        }
        Command command = (Command) ois.readObject();
        Log.d("BS.Net.recieve", command.name + " (" + command.arguments.size() + ")");
        return command;
    }

    public static void send(OutputStream stream, Command message) {
        try {
            ObjectOutputStream so = new ObjectOutputStream(stream);
            so.writeObject(message);
            so.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("BS.Net.send", message.name + " (" + message.arguments.size() + ")");
    }

    public static void respond(Command where, Command message) throws IOException {
        send(where.socket.getOutputStream(), message);
    }
}
