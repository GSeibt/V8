package controller.mc_alg;

import gui.opengl.Vector3f;

public class WeightedVertex extends Vertex {

    private Float weight;

    public WeightedVertex(float x, float y, float z, Float weight) {
        super(x, y, z);
        this.weight = weight;
    }

    public WeightedVertex(Vector3f location, Float weight) {
        super(location);
        this.weight = weight;
    }

    public Float getWeight() {
        return weight;
    }
}
