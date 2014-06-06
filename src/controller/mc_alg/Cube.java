package controller.mc_alg;

public class Cube {

    private WeightedVertex[] vertexes;
    private Vertex[] edges;

    public Cube() {
        this.vertexes = new WeightedVertex[8];
        this.edges = new Vertex[12];

        for (int i = 0; i < vertexes.length; i++) {
            vertexes[i] = new WeightedVertex(0f, 0f, 0f, 0f);
        }

        for (int i = 0; i < edges.length; i++) {
            edges[i] = new Vertex(0f, 0f, 0f);
        }
    }

    public WeightedVertex getVertex(int index) {
        return vertexes[index];
    }

    public void setVertex(int index, WeightedVertex vertex) {
        vertexes[index].setLocation(vertex.getLocation());
        vertexes[index].setNormal(vertex.getNormal());
        vertexes[index].setWeight(vertex.getWeight());
    }


    public Vertex getEdge(int index) {
        return edges[index];
    }

    public void setEdge(int index, Vertex edge) {
        edges[index].setLocation(edge.getLocation());
        edges[index].setNormal(edge.getNormal());
    }

    public Float getWeight(int index) {
        return vertexes[index].getWeight();
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
}
