package controller.mc_alg;

/**
 * A <code>Vertex</code> that has a density associated with it.
 */
public class DensityVertex extends Vertex implements Cloneable {

    private Float density;

    /**
     * Constructs a new <code>DensityVertex</code> with the given position and density. Its normal will be (0, 0, 0).
     *
     * @param x the x coordinate of the <code>DensityVertex</code>
     * @param y the y coordinate of the <code>DensityVertex</code>
     * @param z the z coordinate of the <code>DensityVertex</code>
     * @param density the density of the <code>DensityVertex</code>
     */
    public DensityVertex(float x, float y, float z, Float density) {
        super(x, y, z);
        this.density = density;
    }

    /**
     * Sets the density of this <code>DensityVertex</code> to the given value.
     *
     * @param density the new density
     */
    public void setDensity(Float density) { this.density = density; }

    /**
     * Returns the density of this <code>WeightedVertex</code>.
     *
     * @return the density
     */
    public Float getDensity() {
        return density;
    }

    @Override
    public String toString() {
        return String.format("location=%s, normal=%s, density=%s", getLocation(), getNormal(), density);
    }

    @Override
    protected DensityVertex clone() throws CloneNotSupportedException {
        return (DensityVertex) super.clone();
    }
}
