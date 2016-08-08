package be.buri.battleships;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import be.buri.battleships.Units.Harbor;
import be.buri.battleships.Units.Unit;

/**
 * Created by buri on 1.8.16.
 */
public class Player implements Serializable{
    private boolean connected = false;
    private String name;
    private int id;
    private ArrayList<Unit> units = new ArrayList<Unit>();
    private Harbor harbor;

    public Player(int i) {
        this.id = i;
        this.name = "Player " + id;
    }

    public void addUnit(Unit unit) {
        if (!units.contains(unit)) {
            units.add(unit);
            if (unit.getPlayer() != this) {
                unit.setPlayer(this);
            }
        }
    }

    public boolean hasUnit(Unit unit) {
        return units.contains(unit);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public Harbor getHarbor() {
        return harbor;
    }

    public void setHarbor(Harbor harbor) {
        if (harbor.getPlayer() != this) {
            harbor.setPlayer(this);
        }
        this.harbor = harbor;
    }
}
