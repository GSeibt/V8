package controller.ms_alg;

public class Vertex2D implements Cloneable {

    private float x;
    private float y;

    public Vertex2D() {
        this(0, 0);
    }

    public  Vertex2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

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
