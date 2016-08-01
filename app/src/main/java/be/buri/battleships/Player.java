package be.buri.battleships;

import java.util.Vector;

import be.buri.battleships.Units.Unit;

/**
 * Created by buri on 1.8.16.
 */
public class Player {
    private boolean connected = false;
    private String name;
    private Vector<Unit> units;

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
}
