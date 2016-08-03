package be.buri.battleships.Services;

import android.app.IntentService;

import java.util.Vector;

import be.buri.battleships.Player;

/**
 * Created by buri on 1.8.16.
 */
public abstract class EngineService extends IntentService {
    /**
     * Players: beta version with only 2 players
     */
    protected Vector<Player> players = new Vector<Player>(2);

    public EngineService(String name) {
        super(name);
        for (int i = 1; i <= 2; i++) {
            Player p = new Player("Player " + i);
            players.add(p);
        }
    }

}
