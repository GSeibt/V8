package model.ms_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A 2D mesh made up of lines.
 */
public class Mesh2D {

    private FloatBuffer vertices; // the vertices of the lines
    private IntBuffer indices; // indices into the vertices buffer

    /**
     * Constructs a new <code>Mesh2D</code> containing the given buffers.
     *
     * @param vertices
     *         the vertices of the lines
     * @param indices
     *         the indices into the <code>vertices</code>
     */
    public Mesh2D(FloatBuffer vertices, IntBuffer indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    /**
     * Returns the vertices <code>FloatBuffer</code>. <br>
     * The buffer contains float-pairs [p0x,p0y,p1x,p1y,...] that represent line vertices.
     *
     * @return the vertices
     */
    public FloatBuffer getVertices() {
        return vertices;
    }

    /**
     * Returns the indices <code>IntBuffer</code>.
     *
     * @return the indices
     */
    public IntBuffer getIndices() {
        return indices;
    }
}
