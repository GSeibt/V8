package controller.ms_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Mesh2D {

    private FloatBuffer vertices;
    private IntBuffer indices;

    public Mesh2D(FloatBuffer vertices, IntBuffer indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public FloatBuffer getVertices() {
        return vertices;
    }

    public IntBuffer getIndices() {
        return indices;
    }
}
