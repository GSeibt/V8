package controller.mc_alg;

import gui.opengl.Vector3f;

public class Vertex {

    private Vector3f location;
    private Vector3f normal;

    public Vertex(float x, float y, float z) {
        this(new Vector3f(x, y, z));
    }

    public Vertex(Vector3f location) {
        this.location = location;
    }

    public Vector3f getLocation() {
        return location;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public void setNormal(Vector3f normal) {
        this.normal = normal;
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
}
