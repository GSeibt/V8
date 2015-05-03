package de.uni_passau.fim.seibt.v8.model.ms_alg;

/**
 * A vertex in 2D space.
 */
public class Vertex2D implements Cloneable {

    private float x;
    private float y;

    /**
     * Constructs a new <code>Vertex2D</code> at (0, 0).
     */
    public Vertex2D() {
        this(0, 0);
    }

    /**
     * Constructs a new <code>Vertex2D</code> at (x, y).
     *
     * @param x the x-coordinate for the <code>Vertex2D</code>
     * @param y the y-coordinate for the <code>Vertex2D</code>
     */
    public  Vertex2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the x/y-coordinate of the <code>Vertex2D</code> to the given values.
     *
     * @param x the new x-coordinate
     * @param y the new y-coordinate
     */
    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-coordinate of the <code>Vertex2D</code>.
     *
     * @return the x-coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the <code>Vertex2D</code>.
     *
     * @return the y-coordinate
     */
    public float getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vertex2D vertex2D = (Vertex2D) o;

        if (Float.compare(vertex2D.x, x) != 0) {
            return false;
        }
        if (Float.compare(vertex2D.y, y) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    @Override
    protected Vertex2D clone() throws CloneNotSupportedException {
        return (Vertex2D) super.clone();
    }
}
