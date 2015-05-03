package de.uni_passau.fim.seibt.v8.model.mc_alg;

/**
 * A <code>Vertex</code> that is used to represent a cube corner and as such has an additional value from the
 * <code>MCVolume</code> associated with it.
 */
public class CornerVertex extends Vertex implements Cloneable {

    private float value;

    /**
     * Constructs a new <code>CornerVertex</code> with the given position and value. Its normal will be (0, 0, 0).
     *
     * @param x the x coordinate of the <code>CornerVertex</code>
     * @param y the y coordinate of the <code>CornerVertex</code>
     * @param z the z coordinate of the <code>CornerVertex</code>
     * @param value the value of the <code>CornerVertex</code>
     */
    public CornerVertex(float x, float y, float z, Float value) {
        super(x, y, z);
        this.value = value;
    }

    /**
     * Sets the value of this <code>CornerVertex</code> to the given value.
     *
     * @param value the new value
     */
    public void setValue(float value) { this.value = value; }

    /**
     * Returns the value of this <code>CornerVertex</code>.
     *
     * @return the value
     */
    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("location=%s, normal=%s, value=%s", getLocation(), getNormal(), value);
    }

    @Override
    protected CornerVertex clone() throws CloneNotSupportedException {
        return (CornerVertex) super.clone();
    }
}
