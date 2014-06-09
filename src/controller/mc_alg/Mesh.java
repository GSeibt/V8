package controller.mc_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Mesh {

    private FloatBuffer vertexes;
    private FloatBuffer normals;
    private IntBuffer indices;

    private FloatBuffer normalLines;

    public Mesh(FloatBuffer vertexes, FloatBuffer normals, IntBuffer indices, FloatBuffer normalLines) {
        this.vertexes = vertexes;
        this.normals = normals;
        this.indices = indices;
        this.normalLines = normalLines;
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

    public FloatBuffer getNormalLines() {
        return normalLines;
    }
}
