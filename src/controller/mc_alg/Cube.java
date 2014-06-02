package controller.mc_alg;

import gui.opengl.Vector3f;

public class Cube {

    private Vertex[] vertexes;
    private Vector3f[] edges;

    public Cube(Vertex... vertexes) {

        if (vertexes == null || vertexes.length != 8) {
            throw new IllegalArgumentException("You must supply exactly 8 vertexes that make up the Cube.");
        }

        this.vertexes = vertexes;
        this.edges = new Vector3f[12];
    }

    public Vertex getVertex(int index) {
        return vertexes[index];
    }

    public void setEdge(int index, Vector3f edge) {
        edges[index] = edge;
    }

    public Vector3f getEdge(int index) {
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
