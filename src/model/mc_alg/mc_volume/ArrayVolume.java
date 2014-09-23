package model.mc_alg.mc_volume;

/**
 * A <code>MCVolume</code> backed by a <code>float[][][]</code>.
 */
public class ArrayVolume implements MCVolume {

    private final float[][][] data;

    /**
     * Constructs a new <code>ArrayVolume</code> backed by the given array.
     *
     * @param data
     *         the data array for this <code>ArrayVolume</code>
     */
    public ArrayVolume(float[][][] data) {
        this.data = data;
    }

    @Override
    public float density(int x, int y, int z) {

        if (z < 0 || z >= zSize()) {
            return 0f;
        }

        if (y < 0 || y >= ySize()) {
            return 0f;
        }

        if (x < 0 || x >= xSize()) {
            return 0f;
        }

        return data[z][y][x];
    }

    @Override
    public int xSize() {
        return data[0][0].length;
    }

    @Override
    public int ySize() {
        return data[0].length;
    }

    @Override
    public int zSize() {
        return data.length;
    }
}
