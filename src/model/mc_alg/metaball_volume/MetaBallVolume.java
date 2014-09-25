package model.mc_alg.metaball_volume;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import model.mc_alg.mc_volume.MCVolume;

/**
 * A volume containing <code>MetaBall</code> instances.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Metaballs">Metaballs</a>
 */
public class MetaBallVolume implements MCVolume {

    // dimension of the volume
    private int xSize;
    private int ySize;
    private int zSize;

    // the MetaBall instances in the volume
    private List<MetaBall> metaBalls;

    private final DoubleProperty progress;

    /**
     * Constructs a new <code>MetaBallVolume</code> with the given dimensions.
     *
     * @param xSize
     *         the size in x
     * @param ySize
     *         the size in y
     * @param zSize
     *         the size in z
     */
    public MetaBallVolume(int xSize, int ySize, int zSize) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.metaBalls = new LinkedList<>();
        this.progress = new SimpleDoubleProperty(0);
    }

    /**
     * Constructs a float array containing the values resulting from all the <code>MetaBall</code>s in this
     * volume.
     *
     * @return the volume
     */
    public float[][][] getVolume() {
        float[][][] volume = new float[zSize][ySize][xSize];
        float numValues = zSize * ySize * xSize * metaBalls.size();
        AtomicInteger doneValues = new AtomicInteger(0);

        IntStream.range(0, volume.length).parallel().forEach(z -> {
            for (MetaBall metaBall : metaBalls) {
                for (int y = 0; y < volume[z].length; y++) {
                    for (int x = 0; x < volume[z][y].length; x++) {
                        volume[z][y][x] += metaBall.value(x, y, z);
                        doneValues.incrementAndGet();
                    }
                }
            }

            synchronized (progress) {
                progress.setValue(doneValues.get() / numValues);
            }
        });

        return volume;
    }

    /**
     * Returns the number of balls currently in the volume.
     *
     * @return the number of balls
     */
    public int getNumBalls() {
        return metaBalls.size();
    }

    /**
     * Adds the given ball to the volume.
     *
     * @param metaBall the ball to be added
     */
    public void addBall(MetaBall metaBall) {
        metaBalls.add(metaBall);
    }

    /**
     * Adds all given <code>MetaBall</code>s to the volume.
     * Additionally this method provides easy opportunities for jokes to anyone interested.
     *
     * @param balls the
     */
    public void addAllBalls(Collection<? extends MetaBall> balls) {
        metaBalls.addAll(balls);
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
        int x = rInt(0, xSize - 1);
        int y = rInt(0, ySize - 1);
        int z = rInt(0, zSize - 1);
        int intensity = rInt(minIntensity, maxIntensity);
        int posNeg = (Math.random() < 0.3) ? -1 : 1;

        addBall(x, y, z, intensity * posNeg);
    }

    /**
     * Sets the size in x of this <code>MetaBallVolume</code>.
     *
     * @param xSize the new size
     */
    public void setXSize(int xSize) {
        this.xSize = xSize;
    }

    /**
     * Sets the size in y of this <code>MetaBallVolume</code>.
     *
     * @param ySize the new size
     */
    public void setYSize(int ySize) {
        this.ySize = ySize;
    }

    /**
     * Sets the size in z of this <code>MetaBallVolume</code>.
     *
     * @param zSize the new size
     */
    public void setZSize(int zSize) {
        this.zSize = zSize;
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

    @Override
    public float value(int x, int y, int z) {

        if (z < 0 || z >= zSize()) {
            return 0f;
        }

        if (y < 0 || y >= ySize()) {
            return 0f;
        }

        if (x < 0 || x >= xSize()) {
            return 0f;
        }

        return metaBalls.stream().collect(Collectors.summingDouble(ball -> ball.value(x, y, z))).floatValue();
    }

    @Override
    public int xSize() {
        return xSize;
    }

    @Override
    public int ySize() {
        return ySize;
    }

    @Override
    public int zSize() {
        return zSize;
    }
}
