package controller.mc_alg;

import javafx.geometry.Point3D;

public class Vertex {

    private Point3D location;
    private Float weight;

    public Vertex(float x, float y, float z, Float weight) {
        this(new Point3D(x, y, z), weight);
    }

    public Vertex(Point3D location, Float weight) {
        this.location = location;
        this.weight = weight;
    }

    public double getX() {
        return location.getX();
    }

    public double getY() {
        return location.getY();
    }

    public double getZ() {
        return location.getZ();
    }

    public Float getWeight() {
        return weight;
    }
}
