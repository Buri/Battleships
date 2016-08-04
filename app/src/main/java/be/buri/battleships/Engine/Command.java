package be.buri.battleships.Engine;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by buri on 3.8.16.
 */
public class Command  implements Serializable{
    public String name;
    public Vector<Object> arguments = new Vector<Object>();
    public Socket socket;

    public static Command fromString(String source) throws ClassNotFoundException {
        byte b[] = Base64.decode(source.trim().getBytes(), Base64.DEFAULT);
        ByteArrayInputStream bi = new ByteArrayInputStream(b);
        ObjectInputStream si = null;
        try {
            si = new ObjectInputStream(bi);
            Command c = (Command)si.readObject();
            return c;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Command();
    }

    public static Command fromString(byte[] source) throws ClassNotFoundException {
        byte b[] = Base64.decode(source, Base64.DEFAULT);
        ByteArrayInputStream bi = new ByteArrayInputStream(b);
        ObjectInputStream si = null;
        try {
            si = new ObjectInputStream(bi);
            Command c = (Command)si.readObject();
            return c;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Command();
    }

    public byte[] serialize() {
        String s = "";
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(this);
            so.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //String t = bo.toString();
        return Base64.encode(bo.toByteArray(), Base64.DEFAULT);
        //return bo.toByteArray();
    }
}
