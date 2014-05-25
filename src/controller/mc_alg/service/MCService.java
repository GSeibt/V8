package controller.mc_alg.service;

import controller.mc_alg.Cube;
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
}
