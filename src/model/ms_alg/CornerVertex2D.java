package model.ms_alg;

/**
 * A <code>Vertex2D</code> that is used to represent a square corner and as such has an additional value from the
 * <code>MGrid</code> associated with it.
 */
public class CornerVertex2D extends Vertex2D {

    private Float value;

    /**
     * Constructs a new <code>CornerVertex2D</code> at (0, 0) with value 0.
     */
    public CornerVertex2D() {
        this(0, 0, 0f);
    }

    /**
     * Constructs a new <code>CornerVertex2D</code> with value 0.
     *
     * @param x the x-coordinate for the <code>CornerVertex2D</code>
     * @param y the y-coordinate for the <code>CornerVertex2D</code>
     */
    public CornerVertex2D(float x, float y) {
        this(x, y, 0f);
    }

    /**
     * Constructs a new <code>CornerVertex2D</code> with the given value.
     *
     * @param x the x-coordinate for the <code>CornerVertex2D</code>
     * @param y the y-coordinate for the <code>CornerVertex2D</code>
     * @param value the value for the <code>CornerVertex2D</code>
     */
    public CornerVertex2D(float x, float y, Float value) {
        super(x, y);
        this.value = value;
    }

    /**
     * Sets the value of this <code>CornerVertex2D</code>.
     *
     * @param value the new value
     */
    public void setValue(Float value) {
        this.value = value;
    }

    /**
     * Returns the value of this <code>CornerVertex2D</code>.
     *
     * @return the value
     */
    public Float getValue() {
        return value;
    }
}
