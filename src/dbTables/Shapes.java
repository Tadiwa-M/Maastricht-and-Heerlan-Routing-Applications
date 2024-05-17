package dbTables;

import java.sql.*;

public class Shapes {
    private int shapeId;
    private int shapePtSequence;
    private float shapePtLat;
    private float shapePtLon;
    private float shapeDistTraveled;

    public int getShapeId() {
        return shapeId;
    }

    public void setShapeId(int shapeId) {
        this.shapeId = shapeId;
    }

    public int getShapePtSequence() {
        return shapePtSequence;
    }

    public void setShapePtSequence(int shapePtSequence) {
        this.shapePtSequence = shapePtSequence;
    }

    public float getShapePtLat() {
        return shapePtLat;
    }

    public void setShapePtLat(float shapePtLat) {
        this.shapePtLat = shapePtLat;
    }

    public float getShapePtLon() {
        return shapePtLon;
    }

    public void setShapePtLon(float shapePtLon) {
        this.shapePtLon = shapePtLon;
    }

    public float getShapeDistTraveled() {
        return shapeDistTraveled;
    }

    public void setShapeDistTraveled(float shapeDistTraveled) {
        this.shapeDistTraveled = shapeDistTraveled;
    }
}

