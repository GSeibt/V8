package controller.ms_alg;

/**
 * A <code>Square</code> used in the Marching Squares algorithm.
 * The vertices and edges are numbered 0 - 3 from the top left (or the top for edges) clockwise.
 */
public class Square {

    private DensityVertex2D[] vertices;
    private Vertex2D[] edges;

    /**
     * Constructs a new <code>Square</code>. All vertices and edges will be (0, 0).
     */
    public Square() {
        this.vertices = new DensityVertex2D[4];
        this.edges = new Vertex2D[4];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DensityVertex2D();
        }

        for (int i = 0; i < edges.length; i++) {
            edges[i] = new Vertex2D();
        }
    }

    /**
     * Returns the vertex at the given index.
     *
     * @param index the index of the vertex
     * @return the vertex
     */
    public DensityVertex2D getVertex(int index) {
        return vertices[index];
    }

    /**
     * Returns the edge vertex at the given index.
     *
     * @param index the index of the edge
     * @return the edge vertex
     */
    public Vertex2D getEdge(int index) {
        return edges[index];
    }

    /**
     * Returns the squares index analogous to {@link controller.mc_alg.Cube#getIndex(float)}.
     *
     * @param level the level to be used
     * @return the index
     */
    public short getIndex(float level) {
        short index = 0;

        for (int i = 0; i < 4; i++) {
            if (vertices[i].getDensity() <= level) {
                index |= (short) Math.pow(2, i);
            }
        }

        return index;
    }

    /**
     * Copies the values from the given <code>edge</code> into the squares edge at the given index.
     *
     * @param index the index of the edge
     * @param edge the <code>Vertex2D</code> containing the data for the edge
     */
    public void setEdge(int index, Vertex2D edge) {
        edges[index].setXY(edge.getX(), edge.getY());
    }
}
