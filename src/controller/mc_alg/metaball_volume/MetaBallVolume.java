package controller.mc_alg.metaball_volume;

import java.util.LinkedList;
import java.util.List;

public class MetaBallVolume {

    private int x_dim;
    private int y_dim;
    private int z_dim;
    private List<MetaBall> metaBalls;

    public MetaBallVolume(int x_dim, int y_dim, int z_dim) {
        this.x_dim = x_dim;
        this.y_dim = y_dim;
        this.z_dim = z_dim;
        this.metaBalls = new LinkedList<>();
    }

    public float[][][] getVolume() {
        float[][][] volume = new float[z_dim][y_dim][x_dim];

        for (int z = 0; z < volume.length; z++) {
            for (int y = 0; y < volume[z].length; y++) {
                for (int x = 0; x < volume[z][y].length; x++) {
                    volume[z][y][x] = sumUpBalls(x, y, z);
                }
            }
        }

        return volume;
    }

    private float sumUpBalls(int x, int y, int z) {
        float sum = 0;

        for (MetaBall ball : metaBalls) {
            sum += ball.density(x, y, z);
        }

        return sum;
    }

    public void setBalls(int num) {
        metaBalls.clear();

        for (int i = 0; i < num; i++) {
            addRandomBall();
        }
    }

    public void addRandomBall() {
        int x = rInt(0, x_dim - 1);
        int y = rInt(0, y_dim - 1);
        int z = rInt(0, z_dim - 1);
        int intensity = rInt(1, 100);
        int posNeg = (Math.random() < 0.5) ? -1 : 1;

        addBall(x, y, z, intensity * posNeg);
    }

    public void addBall(int x, int y, int z, int intensity) {
        metaBalls.add(new MetaBall(x, y, z, intensity));
    }

    private static int rInt(int minimum, int maximum) {
        return minimum + (int)(Math.random() * ((maximum - minimum) + 1));
    }

    public void addBall(int x, int y, int z) {
        metaBalls.add(new MetaBall(x, y, z));
    }
}
