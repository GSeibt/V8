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

            Cube cube;
            Cube[][] currentSlice;
            Vertex v0, v1, v2, v3, v4, v5, v6, v7;

            long numCubes = (data.length - 1) * (data[0].length - 1) * (data[0][0].length - 1);
            long doneCubes = 0;

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
                        } else if (y != 0) {
                            v0 = currentSlice[y - 1][x].getVertex(3);
                        } else if (z != 0) {
                            v0 = lowerSlice[y][x].getVertex(4);
                        } else {
                            v0 = new Vertex(x, y, z, data[z][y][x]);
                        }

                        if (y != 0) {
                            v1 = currentSlice[y - 1][x].getVertex(2);
                        } else if (z != 0) {
                            v1 = lowerSlice[y][x].getVertex(5);
                        } else {
                            v1 = new Vertex(x + 1, y, z, data[z][y][x + 1]);
                        }

                        if (z != 0) {
                            v2 = lowerSlice[y][x].getVertex(6);
                        } else {
                            v2 = new Vertex(x + 1, y + 1, z, data[z][y + 1][x + 1]);
                        }

                        if (x != 0) {
                            v3 = currentSlice[y][x - 1].getVertex(2);
                        } else if (z != 0) {
                            v3 = lowerSlice[y][x].getVertex(7);
                        } else {
                            v3 = new Vertex(x, y + 1, z, data[z][y + 1][x]);
                        }

                        if (x != 0) {
                            v4 = currentSlice[y][x - 1].getVertex(5);
                        } else if (y != 0) {
                            v4 = currentSlice[y - 1][x].getVertex(7);
                        } else {
                            v4 = new Vertex(x, y, z + 1, data[z + 1][y][x]);
                        }

                        if (y != 0) {
                            v5 = currentSlice[y - 1][x].getVertex(6);
                        } else {
                            v5 = new Vertex(x + 1, y, z + 1, data[z + 1][y][x + 1]);
                        }

                        v6 = new Vertex(x + 1, y + 1, z + 1, data[z + 1][y + 1][x + 1]);

                        if (x != 0) {
                            v7 = currentSlice[y][x - 1].getVertex(6);
                        } else {
                            v7 = new Vertex(x, y + 1, z + 1, data[z + 1][y + 1][x]);
                        }

                        cube = new Cube(v0, v1, v2, v3, v4, v5, v6, v7);
                        currentSlice[y][x] = cube;

                        doneCubes++;
                    }

                    updateProgress(doneCubes, numCubes);
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
