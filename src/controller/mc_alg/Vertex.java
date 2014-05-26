package controller.mc_alg;

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

    public Point3D getLocation() {
        return location;
    }

    public float getX() {
        return location.getX();
    }

    public float getY() {
        return location.getY();
    }

    public float getZ() {
        return location.getZ();
    }

    public Float getWeight() {
        return weight;
    }
}
