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
    protected Vertex2D clone() throws CloneNotSupportedException {
        return (Vertex2D) super.clone();
    }
}
