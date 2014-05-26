package controller.mc_alg.service;

import controller.mc_alg.Cube;
import controller.mc_alg.Point3D;
import controller.mc_alg.Vertex;
import javafx.concurrent.Service;
import javafx.scene.shape.Mesh;

public abstract class MCService extends Service<Mesh> {

    protected float level;
    protected float[][][] data;
    protected Cube[][] lowerSlice;
    protected Cube[][] upperSlice;

    public MCService(float level, float[][][] data) {
        this.level = level;
        this.data = data;
    }

    public Point3D interpolate(Vertex v1, Vertex v2, float level) {
        float x, y, z;
        float alpha;

        if (Math.abs(level - v1.getWeight()) < Math.pow(10, -4)) {
            return v1.getLocation();
        }

        if (Math.abs(level - v2.getWeight()) < Math.pow(10, -4)) {
            return v2.getLocation();
        }

        if (Math.abs(v1.getWeight() - v2.getWeight()) < Math.pow(10, -4)) {
            return v1.getLocation();
        }

        alpha = (level - v2.getWeight()) / (v1.getWeight() - v2.getWeight());
        x = alpha * v1.getX() + (1 - alpha) * v2.getX();
        y = alpha * v1.getY() + (1 - alpha) * v2.getY();
        z = alpha * v1.getZ() + (1 - alpha) * v2.getZ();

        return new Point3D(x, y, z);
    }
}
