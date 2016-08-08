package be.buri.battleships.Units;

import be.buri.battleships.Player;

/**
 * Created by slavka on 4. 8. 2016.
 */
public class Harbor extends Unit {
    private String name;

    public Harbor(String name, double gpsN, double gpsE) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setPlayer(Player player) {
        super.setPlayer(player);
        if (null != player && player.getHarbor() != this) {
            player.setHarbor(this);
        }
    }
}
