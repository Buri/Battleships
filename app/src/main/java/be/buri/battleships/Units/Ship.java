package be.buri.battleships.Units;

/**
 * Created by buri on 9.8.16.
 */
public class Ship extends Unit {
    private double speed = 0.075d, destLat = 0, destLon = 0;

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDestLat() {
        return destLat;
    }

    public void setDestLat(double destLat) {
        this.destLat = destLat;
    }

    public double getDestLon() {
        return destLon;
    }

    public void setDestLon(double destLon) {
        this.destLon = destLon;
    }

    public void move(int fps) {
        if (destLat != 0 && destLon != 0) {
            double dy    = destLat-gpsN, dx = destLon-gpsE;

            double distance = Math.hypot(destLat-gpsN, destLon-gpsE);
            if (distance > speed/fps) {
                double direction = Math.atan2(dx, dy);
                gpsN += speed/fps * Math.cos(direction);
                gpsE += speed/fps * Math.sin(direction);
            } else {
                gpsN = destLat;
                gpsE = destLon;
                destLon = 0;
                destLat = 0;
            }
        }
    }

    public boolean isMoving() {
        return destLat != 0 || destLon != 0;
    }

    @Override
    public void update(Unit u) {
        super.update(u);
        updateOrders((Ship)u);
    }

    public void updateOrders(Ship to) {
        destLat = to.destLat;
        destLon = to.destLon;
    }
}
