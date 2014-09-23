package model.mc_alg.metaball_volume;

/**
 * A <code>MetaBall</code> in a <code>MetaBallVolume</code>.
 * The intensity is a multiplicative term in the density equation. It may be negative if a <code>MetaBall</code>
 * that's affecting the density of the volume negatively is desired.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Metaballs">Metaballs</a>
 */
public class MetaBall {

    private final int x_pos;
    private final int y_pos;
    private final int z_pos;
    private final int intensity;

    /**
     * Constructs a new <code>MetaBall</code> of density 1.
     *
     * @param x the x coordinate of the center of the ball
     * @param y the y coordinate of the center of the ball
     * @param z the z coordinate of the center of the ball
     */
    public MetaBall(int x, int y, int z) {
        this(x, y, z, 1);
    }

    /**
     * Constructs a new <code>MetaBall</code> of the given density.
     *
     * @param x the x coordinate of the center of the ball
     * @param y the y coordinate of the center of the ball
     * @param z the z coordinate of the center of the ball
     * @param intensity the intensity for the ball (see class documentation for a description of its affect)
     */
    public MetaBall(int x, int y, int z, int intensity) {
        this.x_pos = x;
        this.y_pos = y;
        this.z_pos = z;
        this.intensity = intensity;
    }

    /**
     * Computes the density of this <code>MetaBall</code> at the given coordinate.
     *
     * @param x the x coordinate for which the density is to be computed
     * @param y the y coordinate for which the density is to be computed
     * @param z the z coordinate for which the density is to be computed
     * @return the density
     */
    public float density(int x, int y, int z) {
        int numerator = intensity;
        float denominator = (float) (Math.pow(x - x_pos, 2) + Math.pow(y - y_pos, 2) + Math.pow(z - z_pos, 2));

        if (Float.compare(denominator, 0f) == 0) {
            return intensity;
        } else {
            return numerator / denominator;
        }
    }
}
