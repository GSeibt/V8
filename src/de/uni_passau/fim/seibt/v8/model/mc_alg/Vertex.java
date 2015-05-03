package de.uni_passau.fim.seibt.v8.model.mc_alg;

import de.uni_passau.fim.seibt.v8.util.Vector3f;

/**
 * A <code>Vertex</code> is a point in 3D space and its associated normal.
 */
public class Vertex implements Cloneable {

    private Vector3f location;
    private Vector3f normal;

    /**
     * Constructs a new <code>Vertex</code> with the given position. Its normal will be (0, 0, 0).
     *
     * @param x the x coordinate of the <code>Vertex</code>
     * @param y the y coordinate of the <code>Vertex</code>
     * @param z the z coordinate of the <code>Vertex</code>
     */
    public Vertex(float x, float y, float z) {
        this.location = new Vector3f(x, y, z);
        this.normal = new Vector3f(0f, 0f, 0f);
    }

    /**
     * Returns the location of the <code>Vertex</code>.
     *
     * @return the location
     */
    public Vector3f getLocation() {
        return location;
    }

    /**
     * Returns the normal of the <code>Vertex</code>.
     *
     * @return the normal
     */
    public Vector3f getNormal() {
        return normal;
    }

    /**
     * Copies the x, y, and z coordinates of the given <code>Vector3f</code> and sets the location of this
     * <code>Vertex</code> to them.
     *
     * @param location the new location
     */
    public void setLocation(Vector3f location) {
        setLocation(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Copies the x, y, and z coordinates of the given <code>Vector3f</code> and sets the normal of this
     * <code>Vertex</code> to them.
     *
     * @param normal the new normal
     */
    public void setNormal(Vector3f normal) {
        setNormal(normal.getX(), normal.getY(), normal.getZ());
    }

    /**
     * Sets the location of this <code>Vertex</code> to the new values.
     *
     * @param x the x coordinate of the <code>Vertex</code>
     * @param y the y coordinate of the <code>Vertex</code>
     * @param z the z coordinate of the <code>Vertex</code>
     */
    public void setLocation(float x, float y, float z) {
        location.setXYZ(x, y, z);
    }

    /**
     * Sets the normal of this <code>Vertex</code> to the new values.
     *
     * @param x the x coordinate of the normal
     * @param y the y coordinate of the normal
     * @param z the z coordinate of the normal
     */
    public void setNormal(float x, float y, float z) {
        normal.setXYZ(x, y, z);
    }

    /**
     * Overwritten for value equality.
     *
     * @param o the object to compare <code>this</code> to
     * @return whether the <code>this</code> is equal to <code>o</code>
     */
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
