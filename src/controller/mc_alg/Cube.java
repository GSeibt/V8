package controller.mc_alg;

import javafx.geometry.Point3D;

public class Cube {

    private Vertex[] vertexes;
    private Point3D[] edges;

    public Cube(Vertex... vertexes) {

        if (vertexes == null || vertexes.length != 8) {
            throw new IllegalArgumentException("You must supply exactly 8 vertexes that make up the Cube.");
        }

        this.vertexes = vertexes;
        this.edges = new Point3D[12];
    }

    public Vertex getVertex(int index) {
        return vertexes[index];
    }

    public Float getWeight(int index) {return vertexes[index].getWeight();}

    public void setEdge(int index, Point3D edge) {
        edges[index] = edge;
    }

    public Point3D getEdge(int index) {
        return edges[index];
    }
}
