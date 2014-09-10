package controller.mc_alg;

import java.util.Arrays;
import java.util.List;

/**
 * A <code>Cube</code> used in the marching cubes algorithm. The class maintains its own set of vertices and edge
 * vectors, setter methods copy the given values.
 * <br>The indexing of the vertices and edges is illustrated in the image below: <br>
 * <center>
 * <img src="doc-files/cube.png" width="40%" height="40%" alt="Image not found!">
 * </center>
 * The cube index is formed by setting bit <code>i</code> in an int to 1 if vertex <code>i</code>
 * in the cube is less than or equal to the level. All other bits are 0. This results in a cube index between 0 and 255
 * that can be used for lookup in the {@link controller.mc_alg.Tables} class.
 */
public class Cube {

    // edge indices
    public static List<Integer> bottomIndices = Arrays.asList(0, 1, 2, 3);
    public static List<Integer> topIndices = Arrays.asList(4, 5, 6, 7);
    public static List<Integer> sideIndices = Arrays.asList(8, 9, 10, 11);

    private DensityVertex[] vertices; // the 8 vertices of the cube and the density at the vertex
    private Vertex[] edges; // the 12 triangle vertices that may lie on the edges of the cube, one vertex per edge

    /**
     * Constructs a new <code>Cube</code>. All its vertices will be (0, 0, 0) with density 0 and all its edges will be
     * (0, 0, 0).
     */
    public Cube() {
        this.vertices = new DensityVertex[8];
        this.edges = new Vertex[12];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DensityVertex(0f, 0f, 0f, 0f);
        }

        for (int i = 0; i < edges.length; i++) {
            edges[i] = new Vertex(0f, 0f, 0f);
        }
    }

    /**
     * Constructs a new <code>Cube</code> whose vertices have the given values. All its edges will be (0, 0, 0).
     *
     * @param vertices
     *         the values for the vertices
     */
    public Cube(DensityVertex... vertices) {
        this();

        if ((vertices == null) || (vertices.length != 8)) {
            System.err.println("Invalid vertices array. Cube will remain uninitialized.");
            return;
        }

        for (int i = 0; i < vertices.length; i++) {
            setVertex(i, vertices[i]);
        }
    }

    /**
     * Returns the vertex at the given position. See the class documentation for a description of the indexing
     * convention used.
     *
     * @param index
     *         the index of the vertex
     *
     * @return the vertex
     */
    public DensityVertex getVertex(int index) {
        return vertices[index];
    }

    /**
     * Copies the values from the given <code>WeightedVertex</code> into the internal vertex at <code>index</code>.
     * See the class documentation for a description of the indexing convention used.
     *
     * @param index
     *         the index of the vertex
     * @param vertex
     *         the <code>WeightedVertex</code> containing the data to be copied into the cube
     */
    public void setVertex(int index, DensityVertex vertex) {
        vertices[index].setLocation(vertex.getLocation());
        vertices[index].setNormal(vertex.getNormal());
        vertices[index].setDensity(vertex.getDensity());
    }

    /**
     * Returns the vertex of a triangle on the edge indexed by <code>index</code>.
     * See the class documentation for a description of the indexing convention used.
     *
     * @param index
     *         the index of the edge
     *
     * @return the <code>Vertex</code> of a triangle
     */
    public Vertex getEdge(int index) {
        return edges[index];
    }

    /**
     * Copies the values from the given <code>Vertex</code> into the internal vertex at <code>index</code>.
     * See the class documentation for a description of the indexing convention used.
     *
     * @param index
     *         the index of the edge
     * @param edge
     *         the <code>Vertex</code> containing the data to be copied into the cube
     */
    public void setEdge(int index, Vertex edge) {
        edges[index].setLocation(edge.getLocation());
        edges[index].setNormal(edge.getNormal());
    }

    /**
     * Gets the index of the cube (as described in the class documentation) resulting from the given level.
     *
     * @param level
     *         the level to be used
     *
     * @return the cube index
     *
     * @see controller.mc_alg.Tables#getEdgeIndex(int)
     * @see controller.mc_alg.Tables#getTriangleIndex(int)
     */
    public int getIndex(float level) {
        int index = 0;

        for (int i = 0; i < 8; i++) {
            if (getDensity(i) <= level) {
                index |= (int) Math.pow(2, i);
            }
        }

        return index;
    }

    /**
     * Returns the density of the vertex at the given <code>index</code>.
     * See the class documentation for a description of the indexing convention used.
     *
     * @param index
     *         the index of the vertex
     *
     * @return the density at the vertex
     */
    public Float getDensity(int index) {
        return vertices[index].getDensity();
    }
}
