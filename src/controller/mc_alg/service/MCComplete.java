package controller.mc_alg.service;

import controller.mc_alg.Cube;
import controller.mc_alg.Vertex;
import javafx.concurrent.Task;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;

public class MCComplete extends MCService {

    private class CompleteTask extends Task<Mesh> {

        @Override
        protected Mesh call() throws Exception {
            long time = System.nanoTime(); //TODO remove

            TriangleMesh mesh = new TriangleMesh();

            Vertex v0 = null;
            Vertex v1 = null;
            Vertex v2 = null;
            Vertex v3 = null;
            Vertex v4 = null;
            Vertex v5 = null;
            Vertex v6 = null;
            Vertex v7 = null;
            Cube cube;
            Cube[][] currentSlice;

            for (int z = 0; z < data.length - 1; z++) {

                if (lowerSlice == null) {
                    lowerSlice = new Cube[data[0].length - 1][data[0][0].length - 1];
                    currentSlice = lowerSlice;
                } else if (upperSlice == null) {
                    upperSlice = new Cube[data[0].length - 1][data[0][0].length - 1];
                    currentSlice = upperSlice;
                } else {
                    lowerSlice = upperSlice;
                    upperSlice = new Cube[data[0].length - 1][data[0][0].length - 1];
                    currentSlice = upperSlice;
                }

                for (int y = 0; y < data[z].length - 1; y++) {

                    for (int x = 0; x < data[z][y].length - 1; x++) {

                        if (x != 0) {
                            v0 = currentSlice[y][x - 1].getVertex(1);
                            v3 = currentSlice[y][x - 1].getVertex(2);
                            v4 = currentSlice[y][x - 1].getVertex(5);
                            v7 = currentSlice[y][x - 1].getVertex(6);
                        } else {
                            v7 = new Vertex(x, y + 1, z + 1, data[z + 1][y + 1][x]);
                        }

                        if (y != 0) {
                            if (v0 == null) v0 = currentSlice[y - 1][x].getVertex(3);
                                            v1 = currentSlice[y - 1][x].getVertex(2);
                            if (v4 == null) v4 = currentSlice[y - 1][x].getVertex(7);
                                            v5 = currentSlice[y - 1][x].getVertex(6);
                        } else {
                            v5 = new Vertex(x + 1, y,     z + 1, data[z + 1][y]    [x + 1]);
                            v6 = new Vertex(x + 1, y + 1, z + 1, data[z + 1][y + 1][x + 1]);
                        }

                        if (z != 0) {
                            if (v0 == null) v0 = lowerSlice[y][x].getVertex(4);
                            if (v1 == null) v1 = lowerSlice[y][x].getVertex(5);
                                            v2 = lowerSlice[y][x].getVertex(6);
                            if (v3 == null) v3 = lowerSlice[y][x].getVertex(7);
                        } else {
                            if (v6 == null) v6 = new Vertex(x + 1, y + 1, z + 1, data[z + 1][y + 1][x + 1]);
                                            v2 = new Vertex(x + 1, y + 1, z,     data[z    ][y + 1][x + 1]);
                        }

                        if (v0 == null) v0 = new Vertex(x,     y,     z,     data[z]    [y]    [x]);
                        if (v1 == null) v1 = new Vertex(x + 1, y,     z,     data[z]    [y]    [x + 1]);
                        if (v2 == null) v2 = new Vertex(x + 1, y + 1, z,     data[z]    [y + 1][x + 1]);
                        if (v3 == null) v3 = new Vertex(x,     y + 1, z,     data[z]    [y + 1][x]);
                        if (v4 == null) v4 = new Vertex(x,     y,     z + 1, data[z + 1][y]    [x]);
                        if (v5 == null) v5 = new Vertex(x + 1, y,     z + 1, data[z + 1][y]    [x + 1]);
                        if (v6 == null) v6 = new Vertex(x + 1, y + 1, z + 1, data[z + 1][y + 1][x + 1]);
                        if (v7 == null) v7 = new Vertex(x,     y + 1, z + 1, data[z + 1][y + 1][x]);

                        cube = new Cube(v0, v1, v2, v3, v4, v5, v6, v7);
                        currentSlice[y][x] = cube;

                        v0 = v1 = v2 = v3 = v4 = v5 = v6 = v7 = null;
                    }
                }
            }

            System.out.println("Cubes: " + (System.nanoTime() - time) / Math.pow(10, 6) + " ms"); //TODO remove

            return mesh;
        }
    }

    public MCComplete(float level, float[][][] data) {
        super(level, data);
    }

    @Override
    protected Task<Mesh> createTask() {
        return new CompleteTask();
    }
}
