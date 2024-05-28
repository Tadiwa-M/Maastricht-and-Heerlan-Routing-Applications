package Transport;

public class Car extends Vehicle {
    public Car(double distance) {
        super(distance);
        this.speed = 30 / 60;
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
