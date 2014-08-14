package controller.ms_alg.ms_volume;

/**
 * A <code>MSGrid</code> backed by a <code>float[][]</code>.
 */
public class GridVolume implements MSGrid {

    private final float[][] data;

    /**
     * Constructs a new <code>GridVolume</code> backed by the given <code>float[][]</code>.
     *
     * @param data the data for the <code>MSGrid</code>
     */
    public GridVolume(float[][] data) {
        this.data = data;
    }

    @Override
    public float density(int x, int y) {

        if (x < 0 || x >= xSize()) {
            return 0f;
        }

        if (y < 0 || y >= ySize()) {
            return 0f;
        }

        return data[y][x];
    }

    @Override
    public int xSize() {
        return data[0].length;
    }

    @Override
    public int ySize() {
        return data.length;
    }
}
