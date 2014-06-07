package controller.mc_alg.metaball_volume;

public class MetaBall {

    private final int x_pos;
    private final int y_pos;
    private final int z_pos;
    private final int intensity;

    public MetaBall(int x, int y, int z) {
        this(x, y, z, 1);
    }

    public MetaBall(int x, int y, int z, int intensity) {
        this.x_pos = x;
        this.y_pos = y;
        this.z_pos = z;
        this.intensity = intensity;
    }

    public float density(int x, int y, int z) {
        int numerator = intensity;
        float denominator = (float) (Math.pow(x - x_pos, 2) + Math.pow(y - y_pos, 2) + Math.pow(z - z_pos, 2));

        return numerator / denominator;
    }
}
