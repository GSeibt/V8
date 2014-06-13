package controller.mc_alg.metaball_volume;

import java.util.LinkedList;
import java.util.List;

/**
 * A volume containing <code>MetaBall</code> instances.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Metaballs">Metaballs</a>
 */
public class MetaBallVolume {

    // dimension of the volume
    private int x_dim;
    private int y_dim;
    private int z_dim;

    // the MetaBall instances in the volume
    private List<MetaBall> metaBalls;

    /**
     * Constructs a new <code>MetaBallVolume</code> with the given dimensions.
     *
     * @param x_dim
     *         the size in x
     * @param y_dim
     *         the size in y
     * @param z_dim
     *         the size in z
     */
    public MetaBallVolume(int x_dim, int y_dim, int z_dim) {
        this.x_dim = x_dim;
        this.y_dim = y_dim;
        this.z_dim = z_dim;
        this.metaBalls = new LinkedList<>();
    }

    /**
     * Constructs a float array containing the density values resulting from all the <code>MetaBall</code>s in this
     * volume.
     *
     * @return the volume
     */
    public float[][][] getVolume() {
        float[][][] volume = new float[z_dim][y_dim][x_dim];

        for (MetaBall ball : metaBalls) {
            for (int z = 0; z < volume.length; z++) {
                for (int y = 0; y < volume[z].length; y++) {
                    for (int x = 0; x < volume[z][y].length; x++) {
                        volume[z][y][x] += ball.density(x, y, z);
                    }
                }
            }
        }

        return volume;
    }

    /**
     * Clears the current <code>MetaBall</code>s and sets the number of <code>MetaBall</code>s to the given value.
     *
     * @param num
     *         the new number of balls
     */
    public void setBalls(int num) {
        metaBalls.clear();

        for (int i = 0; i < num; i++) {
            addRandomBall();
        }
    }

    /**
     * Adds a random <code>MetaBall</code> to the volume.
     */
    public void addRandomBall() {
        int x = rInt(0, x_dim - 1);
        int y = rInt(0, y_dim - 1);
        int z = rInt(0, z_dim - 1);
        int intensity = rInt(1, 100);
        int posNeg = (Math.random() < 0.5) ? -1 : 1;

        addBall(x, y, z, intensity * posNeg);
    }

    /**
     * Adds a new <code>MetaBall</code> to the volume.
     *
     * @param x
     *         the x coordinate for the ball
     * @param y
     *         the y coordinate for the ball
     * @param z
     *         the z coordinate for the ball
     * @param intensity
     *         the intensity of the ball
     */
    public void addBall(int x, int y, int z, int intensity) {
        metaBalls.add(new MetaBall(x, y, z, intensity));
    }

    /**
     * Adds a new <code>MetaBall</code> to the volume.
     * Its intensity will be 1.
     *
     * @param x
     *         the x coordinate for the ball
     * @param y
     *         the y coordinate for the ball
     * @param z
     *         the z coordinate for the ball
     */
    public void addBall(int x, int y, int z) {
        metaBalls.add(new MetaBall(x, y, z));
    }

    /**
     * Returns a random integer from the range [minimum, maximum].
     *
     * @param minimum
     *         the lower bound of the range (included)
     * @param maximum
     *         the upper bound of the range (included)
     *
     * @return the integer
     */
    private static int rInt(int minimum, int maximum) {
        return minimum + (int) (Math.random() * ((maximum - minimum) + 1));
    }
}
