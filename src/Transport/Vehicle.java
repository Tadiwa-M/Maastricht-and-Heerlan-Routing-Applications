package Transport;

public abstract class Vehicle {
    public Vehicle(double distance) {
        this.distance = distance;
    }
    double distance;
    double speed;
    double vehicleConstant;

    public abstract double getDistance();
    public abstract double getSpeed();
    public double calculateTime() {
        return getDistance() / getSpeed();
    }
}
