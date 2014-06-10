package controller.mc_alg;

import util.Vector3f;

public class Vertex implements Cloneable {

    private Vector3f location;
    private Vector3f normal;

    public Vertex(float x, float y, float z) {
        this.location = new Vector3f(x, y, z);
        this.normal = new Vector3f(0f, 0f, 0f);
    }

    public Vector3f getLocation() {
        return location;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public void setLocation(Vector3f location) {
        setLocation(location.getX(), location.getY(), location.getZ());
    }

    public void setNormal(Vector3f normal) {
        setNormal(normal.getX(), normal.getY(), normal.getZ());
    }

    public void setLocation(float x, float y, float z) {
        location.setXYZ(x, y, z);
    }

    public void setNormal(float x, float y, float z) {
        normal.setXYZ(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vertex vertex = (Vertex) o;

        if (location != null ? !location.equals(vertex.location) : vertex.location != null) {
            return false;
        }
        if (normal != null ? !normal.equals(vertex.normal) : vertex.normal != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (normal != null ? normal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("location=%s, normal=%s", location, normal);
    }

    @Override
    protected Vertex clone() throws CloneNotSupportedException {
        Vertex clone = (Vertex) super.clone();
        clone.location = this.location.clone();
        clone.normal = this.normal.clone();

        return clone;
    }
}
