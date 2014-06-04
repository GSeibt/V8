package controller.mc_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Mesh {

    private FloatBuffer vertexes;
    private FloatBuffer normals;
    private IntBuffer indices;

    public Mesh(FloatBuffer vertexes, FloatBuffer normals, IntBuffer indices) {
        this.vertexes = vertexes;
        this.normals = normals;
        this.indices = indices;
    }

    public FloatBuffer getVertexes() {
        return vertexes;
    }

    public FloatBuffer getNormals() {
        return normals;
    }

    public IntBuffer getIndices() {
        return indices;
    }
}
