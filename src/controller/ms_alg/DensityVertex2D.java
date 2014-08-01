package controller.ms_alg;

public class DensityVertex2D extends Vertex2D {

    private Float density;

    public DensityVertex2D() {
        this.density = 0f;
    }

    public DensityVertex2D(float x, float y) {
        super(x, y);
        this.density = 0f;
    }

    public void setDensity(Float density) {
        this.density = density;
    }

    public Float getDensity() {
        return density;
    }
}
