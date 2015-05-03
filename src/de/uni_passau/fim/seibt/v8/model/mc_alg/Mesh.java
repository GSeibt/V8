package de.uni_passau.fim.seibt.v8.model.mc_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Container class for <code>Buffer</code> objects containing information that will be used by OpenGL in
 * <code>MeshView3D</code> to render the triangle mesh resulting from the marching cubes algorithm.
 */
public class Mesh {

    private FloatBuffer vertices; // vertices of the triangles interspersed with vertices used for drawing normal lines
    private FloatBuffer normals; // the normals at the vertices of the triangles
    private IntBuffer indices; // indices into the vertices and normals array (for glDrawElements(...))

    /**
     * Constructs a new <code>Mesh</code> containing the given buffers.
     *
     * @param vertices the vertices of the triangles
     * @param normals the normals at the vertices
     * @param indices the indices into <code>vertices</code> and <code>normals</code>
     */
    public Mesh(FloatBuffer vertices, FloatBuffer normals, IntBuffer indices) {
        this.vertices = vertices;
        this.normals = normals;
        this.indices = indices;
    }

    /**
     * Returns the vertices <code>FloatBuffer</code>. <br>
     * The buffer contains pairs of float-triples [p0x,p0y,p0z,p0nx,p0ny,p0nz, ...] where p0x/y/z are the coordinates
     * of one triangle vertex and p0nx/y/z the coordinates of a point such that the line between p0x/y/z and p0/nx/ny/nz
     * is a representation of the normal at p0.
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

    public int getNumIndices() {
        return indices.limit();
    }
}
