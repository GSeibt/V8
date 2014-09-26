package model.ms_alg.ms_volume;

/**
 * A data source for the <code>MSRunner</code>.
 */
public interface MSGrid {

    /**
     * Returns the value at the given position. If the position is out of bounds the method will return 0.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the value
     */
    float value(int x, int y);

    /**
     * Returns the x size of the <code>MCVolume</code>.
     *
     * @return the x size
     */
    int xSize();

    /**
     * Returns the y size of the <code>MCVolume</code>.
     *
     * @return the y size
     */
    int ySize();
}
