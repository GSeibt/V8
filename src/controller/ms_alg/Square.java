package controller.ms_alg;

public class Square {

    private DensityVertex2D[] vertices;
    private Float[] densities;
    private Vertex2D[] edges;

    public Square() {
        this.vertices = new DensityVertex2D[8];
        this.edges = new Vertex2D[8];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DensityVertex2D();
        }

        for (int i = 0; i < densities.length; i++) {
            densities[i] = (float) 0;
        }

        for (int i = 0; i < edges.length; i++) {
            edges[i] = new Vertex2D();
        }
    }

    public DensityVertex2D getVertex(int index) {
        return vertices[index];
    }

    public Vertex2D getEdge(int index) {
        return edges[index];
    }

    public short getIndex(float level) {
        short index = 0;

        for (int i = 0; i < 4; i++) {
            if (densities[i] <= level) {
                index |= (short) Math.pow(2, i);
            }
        }

        return index;
    }

    public void setEdge(int index, Vertex2D edge) {
        edges[index].setXY(edge.getX(), edge.getY());
    }
}
