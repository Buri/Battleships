package be.buri.battleships.Engine;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by buri on 3.8.16.
 */
public class Command  implements Serializable{
    public String name;
    public ArrayList<Object> arguments = new ArrayList<Object>();
    public Socket socket;
    public int playerId;
}
