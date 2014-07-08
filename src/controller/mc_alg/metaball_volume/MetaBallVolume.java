package controller.mc_alg.metaball_volume;

import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

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

    private DoubleProperty progress;

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
        this.progress = new SimpleDoubleProperty(0);
    }

    /**
     * Constructs a float array containing the density values resulting from all the <code>MetaBall</code>s in this
     * volume.
     *
     * @return the volume
     */
    public float[][][] getVolume() {
        float[][][] volume = new float[z_dim][y_dim][x_dim];
        float numValues = z_dim * y_dim * x_dim * metaBalls.size();
        int doneValues = 0;

        progress.set(0);
        for (MetaBall ball : metaBalls) {
            for (int z = 0; z < volume.length; z++) {
                for (int y = 0; y < volume[z].length; y++) {
                    for (int x = 0; x < volume[z][y].length; x++) {
                        volume[z][y][x] += ball.density(x, y, z);
                        doneValues++;
                    }
                }
                progress.set(doneValues / numValues);
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
     * The intensity will be between 1 and 300.
     */
    public void addRandomBall() {
        addRandomBall(1, 300);
    }

    /**
     * Adds a random <code>MetaBall</code> to the volume.
     *
     * @param minIntensity the minimum intensity for the ball
     * @param maxIntensity the maximum intensity for the ball
     */
    public void addRandomBall(int minIntensity, int maxIntensity) {
        int x = rInt(0, x_dim - 1);
        int y = rInt(0, y_dim - 1);
        int z = rInt(0, z_dim - 1);
        int intensity = rInt(minIntensity, maxIntensity);
        int posNeg = (Math.random() < 0.3) ? -1 : 1;

        addBall(x, y, z, intensity * posNeg);
    }

    /**
     * Sets the size in x of this <code>MetaBallVolume</code>.
     *
     * @param x_dim the new size
     */
    public void setX_dim(int x_dim) {
        this.x_dim = x_dim;
    }

    /**
     * Sets the size in y of this <code>MetaBallVolume</code>.
     *
     * @param y_dim the new size
     */
    public void setY_dim(int y_dim) {
        this.y_dim = y_dim;
    }

    /**
     * Sets the size in z of this <code>MetaBallVolume</code>.
     *
     * @param z_dim the new size
     */
    public void setZ_dim(int z_dim) {
        this.z_dim = z_dim;
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

    /**
     * Returns the progress property of this <code>MetaBallVolume</code>. Will have a value between 0 - 1 indicating
     * 0% to 100% done.
     *
     * @return the progress property
     */
    public DoubleProperty progressProperty() {
        return progress;
    }
}
