package be.buri.battleships.Units;

import android.graphics.Point;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

import be.buri.battleships.Player;

/**
 * Created by buri on 1.8.16.
 * <p/>
 * Abstract class: a harbour / a ship
 */
public class Unit implements Serializable {
    protected static int unitCount = 0;
    protected int id;
    protected int hitpoints = 0;
    protected Player player;
    protected double gpsN;
    protected double gpsE;
    protected transient Marker marker;

    public Unit() {
        id = unitCount++;
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
        if (null != player && !player.hasUnit(this)) {
            player.addUnit(this);
        }
    }

    public double getGpsN() {
        return gpsN;
    }

    public double getGpsE() {
        return gpsE;
    }


    public int getId() {
        return id;
    }

    public void update(Unit u) {
        this.hitpoints = u.hitpoints;
        this.gpsE = u.gpsE;
        this.gpsN = u.gpsN;
    }

    public boolean hasMarker() {
        return null != marker;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public void setGpsN(double gpsN) {
        this.gpsN = gpsN;
    }

    public void setGpsE(double gpsE) {
        this.gpsE = gpsE;
    }
}
