package controller.mc_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Container class for <code>Buffer</code> objects containing information that will be used by OpenGL in
 * <code>OpenGL_V8</code> to render the triangle mesh resulting from the marching cubes algorithm.
 */
public class Mesh {

    private FloatBuffer vertices; // the vertices of the triangles
    private FloatBuffer normals; // the normals at the vertices of the triangles
    private IntBuffer indices; // indices into the vertices and normals array (for glDrawElements(...))
    private FloatBuffer normalLines; // pairs vertices describing lines, the normals to be drawn with glDrawArrays(...)

    /**
     * Constructs a new <code>Mesh</code> containing the given buffers.
     *
     * @param vertices the vertices of the triangles
     * @param normals the normals at the vertices
     * @param indices the indices into <code>vertices</code> and <code>normals</code>
     * @param normalLines pairs of two vertices describing lines symbolising the normals
     */
    public Mesh(FloatBuffer vertices, FloatBuffer normals, IntBuffer indices, FloatBuffer normalLines) {
        this.vertices = vertices;
        this.normals = normals;
        this.indices = indices;
        this.normalLines = normalLines;
    }

    /**
     * Returns the vertices <code>FloatBuffer</code>.
     *
     * @return the vertices
     */
    public FloatBuffer getVertices() {
        return vertices;
    }

    /**
     * Returns the normals <code>FloatBuffer</code>.
     *
     * @return the normals
     */
    public FloatBuffer getNormals() {
        return normals;
    }

    /**
     * Returns the indices <code>IntBuffer</code>.
     *
     * @return the indices
     */
    public IntBuffer getIndices() {
        return indices;
    }

    /**
     * Returns the normal lines <code>FloatBuffer</code>.
     *
     * @return the normal lines
     */
    public FloatBuffer getNormalLines() {
        return normalLines;
    }
}
