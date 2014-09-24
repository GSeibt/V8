package model.mc_alg;

/**
 * A <code>Vertex</code> that is used to represent a cube corner and as such has an additional value from the
 * <code>MCVolume</code> associated with it.
 */
public class Vertex4f extends Vertex implements Cloneable {

    private Float value;

    /**
     * Constructs a new <code>Vertex4f</code> with the given position and value. Its normal will be (0, 0, 0).
     *
     * @param x the x coordinate of the <code>Vertex4f</code>
     * @param y the y coordinate of the <code>Vertex4f</code>
     * @param z the z coordinate of the <code>Vertex4f</code>
     * @param value the value of the <code>Vertex4f</code>
     */
    public Vertex4f(float x, float y, float z, Float value) {
        super(x, y, z);
        this.value = value;
    }

    /**
     * Sets the value of this <code>Vertex4f</code> to the given value.
     *
     * @param value the new value
     */
    public void setValue(Float value) { this.value = value; }

    /**
     * Returns the value of this <code>Vertex4f</code>.
     *
     * @return the value
     */
    public Float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("location=%s, normal=%s, value=%s", getLocation(), getNormal(), value);
    }

    @Override
    protected Vertex4f clone() throws CloneNotSupportedException {
        return (Vertex4f) super.clone();
    }
}
