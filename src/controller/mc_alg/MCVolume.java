package controller.mc_alg;

/**
 * A data source for the <code>MCRunner</code>.
 */
public interface MCVolume {

    /**
     * Returns the density at the given position. If the position is out of bounds the method will return 0.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the density
     */
    float density(int x, int y, int z);

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

    /**
     * Returns the z size of the <code>MCVolume</code>.
     *
     * @return the z size
     */
    int zSize();
}
