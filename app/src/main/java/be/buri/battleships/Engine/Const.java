package be.buri.battleships.Engine;

import java.util.Vector;

import be.buri.battleships.Units.Harbor;

/**
 * Created by buri on 3.8.16.
 */
public class Const {
    public final static String CMD_DISCONNECT = "DISCONNECT";
    public final static String CMD_LIST_PLAYERS = "LIST_PLAYERS";
    public final static String CMD_IS_WARSHIPS = "GAME_IDENTIFY";
    public final static String CMD_IS_WARSHIPS_POSITIVE = "GAME_IDENTIFY_YES";
    public final static String CMD_SET_PLAYER_NAME = "SET_PLAYER_NAME";
    public final static String CMD_ADD_UNIT = "ADD_UNIT";
    public final static String CMD_UPDATE_UNIT = "UPDATE_UNIT";
    public final static String CMD_REMOVE_UNIT = "REMOVE_UNIT";
    public final static String CMD_REQUEST_NEW_UNIT = "REQUEST_NEW_UNIT";


    public static final String BROADCAST_UPDATE_PLAYER_LIST = "BROADCAST_UPDATE_PLAYER_LIST";

    private static Vector<Harbor> harbors = new Vector<Harbor>() {
        {
            add(new Harbor("Aarhus", 56.156998, 10.213376));
            add(new Harbor("Copenhagen", 55.676087, 12.587616));
            add(new Harbor("Esbjerg", 55.460393, 8.436720));
            add(new Harbor("Frederikshavn", 57.439275, 10.544074));
            add(new Harbor("Hanstholm", 57.120962, 8.598468));
            add(new Harbor("Hirtshals", 57.594133, 9.969568));
            add(new Harbor("Grenaa", 56.408487, 10.922023));
            add(new Harbor("Skagen", 57.718120, 10.588968));
        }
    };
    public static Vector<Harbor> getHarbors(){
        return harbors;
    }
}
