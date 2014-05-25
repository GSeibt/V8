package controller.mc_alg.service;

import javafx.concurrent.Task;
import javafx.scene.shape.Mesh;

public class MCCubeWise extends MCService {

    public MCCubeWise(float level, float[][][] data) {
        super(level, data);
    }

    @Override
    protected Task<Mesh> createTask() {
        return null;
    }
}
