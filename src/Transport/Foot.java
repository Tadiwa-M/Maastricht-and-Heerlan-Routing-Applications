package Transport;

public class Foot extends Vehicle {
    public Foot(double distance) {
        super(distance);
        this.speed = 0.0835;
        this.vehicleConstant = 3;
    }
    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

}
