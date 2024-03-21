package Transport;

public class Bike extends Vehicle {
    public Bike(double distance) {
        super(distance);
        this.speed = 15.0 / 60;
        this.vehicleConstant = 1.5;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public double getSpeed() {
        return speed;
    }
    @Override
    public double calculateTime() {
        return distance / speed;
    }

}
