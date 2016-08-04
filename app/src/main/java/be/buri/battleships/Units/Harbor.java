package be.buri.battleships.Units;

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
}
