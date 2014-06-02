package controller.mc_alg;

import gui.opengl.Vector3f;

public class Vertex {

    private Vector3f location;
    private Float weight;

    public Vertex(float x, float y, float z, Float weight) {
        this(new Vector3f(x, y, z), weight);
    }

    public Vertex(Vector3f location, Float weight) {
        this.location = location;
        this.weight = weight;
    }

    public Vector3f getLocation() {
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
