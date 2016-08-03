package be.buri.battleships.Engine;

import java.net.Socket;
import java.util.Vector;

/**
 * Created by buri on 3.8.16.
 */
public class Command {
    public String name;
    public Vector<Object> arguments = new Vector<Object>();
    public Socket socket;

    public static Command fromString(String source) {
        String[] parts = source.split(";");
        Command n = new Command();
        n.name = parts[0];
        for (int i = 1; i < parts.length; i++) {
            n.arguments.add(parts[i]);
        }
        return n;
    }

    @Override
    public String toString() {
        String s = name + ';';
        for (Object o : arguments) {
            s += o.toString() + ";";
        }
        return s;
    }
}
