package controller.mc_alg;

public class Cube {

    private WeightedVertex[] vertexes;
    private Vertex[] edges;

    public Cube(WeightedVertex... vertexes) {

        if (vertexes == null || vertexes.length != 8) {
            throw new IllegalArgumentException("You must supply exactly 8 vertexes that make up the Cube.");
        }

        this.vertexes = vertexes;
        this.edges = new Vertex[12];
    }

    public WeightedVertex getVertex(int index) {
        return vertexes[index];
    }

    public void setEdge(int index, Vertex edge) {
        edges[index] = edge;
    }

    public Vertex getEdge(int index) {
        return edges[index];
    }

    public int getIndex(float level) {
        int index = 0;

        for (int i = 0; i < 8; i++) {
            if (getWeight(i) < level) {
                index |= (int) Math.pow(2, i);
            }
        }

        return index;
    }

    public Float getWeight(int index) {return vertexes[index].getWeight();}
}
