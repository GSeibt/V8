package controller.ms_alg.ms_volume;

public class GridVolume implements MSGrid {

    private final float[][] data;

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
