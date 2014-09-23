package model.ms_alg;

/**
 * A <code>Vertex2D</code> that additionally contains a density value.
 */
public class DensityVertex2D extends Vertex2D {

    private Float density;

    /**
     * Constructs a new <code>DensityVertex2D</code> at (0, 0) with density value 0.
     */
    public DensityVertex2D() {
        this(0, 0, 0f);
    }

    /**
     * Constructs a new <code>DensityVertex2D</code> with density 0.
     *
     * @param x the x-coordinate for the <code>DensityVertex2D</code>
     * @param y the y-coordinate for the <code>DensityVertex2D</code>
     */
    public DensityVertex2D(float x, float y) {
        this(x, y, 0f);
    }

    /**
     * Constructs a new <code>DensityVertex2D</code> with the given density.
     *
     * @param x the x-coordinate for the <code>DensityVertex2D</code>
     * @param y the y-coordinate for the <code>DensityVertex2D</code>
     * @param density the density for the <code>DensityVertex2D</code>
     */
    public DensityVertex2D(float x, float y, Float density) {
        super(x, y);
        this.density = density;
    }

    /**
     * Sets the density of this <code>DensityVertex2D</code>.
     *
     * @param density the new density
     */
    public void setDensity(Float density) {
        this.density = density;
    }

    /**
     * Returns the density of this <code>DensityVertex2D</code>.
     *
     * @return the density
     */
    public Float getDensity() {
        return density;
    }
}
