package be.buri.battleships.Units;

import be.buri.battleships.Player;

/**
 * Created by slavka on 4. 8. 2016.
 */
public class Harbor extends Unit {
    private String name;
    private double gpsN;
    private double gpsE;

    public Harbor(String name, double gpsN, double gpsE) {
        this.name = name;
        this.gpsN = gpsN;
        this.gpsE = gpsE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGpsN() {
        return gpsN;
    }

    public double getGpsE() {
        return gpsE;
    }

    @Override
    public void setPlayer(Player player) {
        super.setPlayer(player);
        if (player.getHarbor() != this) {
            player.setHarbor(this);
        }
    }
}
