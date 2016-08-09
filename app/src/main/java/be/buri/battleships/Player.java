package be.buri.battleships;

import android.graphics.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import be.buri.battleships.Engine.Const;
import be.buri.battleships.Units.Harbor;
import be.buri.battleships.Units.Unit;

/**
 * Created by buri on 1.8.16.
 */
public class Player implements Serializable {
    private boolean connected = false;
    private String name;
    private int id;
    private int color = Color.GREEN;
    transient private ArrayList<Unit> units = new ArrayList<Unit>();
    private Harbor harbor;

    public Player(int i) {
        this.id = i;
        this.name = "Player " + id;
    }

    public void update(Player data) {
        name = data.getName();

        if (null == data.getHarbor()) {
            if (harbor != null) {
                harbor.setPlayer(null);
            }
            harbor = null;
        } else {
            for (Harbor h : Const.getHarbors()) {
                if (data.getHarbor().getName() == h.getName()) {
                    setHarbor(h);
                    break;
                }
            }
        }

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

    public int getUnitCount() {
        return units.size();
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

    public void setHarbor(Harbor h) {
        if (null != this.harbor) {
            units.remove(this.harbor);
        }
        if (null != h && h.getPlayer() != this) {
            h.setPlayer(this);
        }
        this.harbor = h;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        units = new ArrayList<>();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
