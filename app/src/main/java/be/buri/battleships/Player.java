package be.buri.battleships;

import java.util.ArrayList;
import java.util.Vector;

import be.buri.battleships.Units.Unit;

/**
 * Created by buri on 1.8.16.
 */
public class Player {
    private boolean connected = false;
    private String name;
    private int id;
    private ArrayList<Unit> units = new ArrayList<Unit>();

    public Player(String name) {
        this.name = name;
    }

    public void addUnit(Unit unit)
    {
        if (!units.contains(unit)) {
            units.add(unit);
            if (unit.getPlayer() != this) {
                unit.setPlayer(this);
            }
        }
    }

    public boolean hasUnit(Unit unit)
    {
        return units.contains(unit);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
