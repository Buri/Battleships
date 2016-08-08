package be.buri.battleships.Units;

import android.graphics.Point;

import java.io.Serializable;

import be.buri.battleships.Player;

/**
 * Created by buri on 1.8.16.
 *
 * Abstract class: a harbour / a ship
 */
public class Unit implements Serializable {
    protected Point position = new Point(0, 0);
    protected int hitpoints = 0;
    protected Player player;

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        if (!player.hasUnit(this)) {
            player.addUnit(this);
        }
    }
}
