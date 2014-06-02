package controller.mc_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.lwjgl.BufferUtils;

import static controller.mc_alg.MCRunner.Type.*;

public class MCRunner implements Runnable {

    public enum Type {CUBE, SLICE, COMPLETE}

    private float level;
    private float[][][] data;
    private volatile boolean stop;
    private volatile boolean interrupted = false;
    private Type type;
    private BiConsumer<FloatBuffer, IntBuffer> meshConsumer;

    private Cube[][] lowerSlice;
    private Cube[][] upperSlice;

    private Map<Point3D, Integer> points;
    private ArrayList<Integer> indices;

    public MCRunner(float[][][] data, float level, Type type, BiConsumer<FloatBuffer, IntBuffer> meshConsumer) {
        this.level = level;
        this.data = data;
        this.type = type;
        this.meshConsumer = meshConsumer;
        this.points = new LinkedHashMap<>(100000);
        this.indices = new ArrayList<>();
        this.stop = false;
    }

    @Override
    public void run() {
        long time = System.nanoTime(); //TODO remove

        int cubeIndex;
        Cube cube;
        Cube[][] currentSlice;

        for (int z = 0; z < data.length - 1 && !interrupted; z++) {

            currentSlice = updateSlices();

            for (int y = 0; y < data[z].length - 1; y++) {

                for (int x = 0; x < data[z][y].length - 1; x++) {

                    cube = computeVertexes(x, y, z, currentSlice);
                    currentSlice[y][x] = cube;

                    cubeIndex = cube.getIndex(level);

                    if ((cubeIndex != 0) && (cubeIndex != 255)) {
                        computeEdges(x, y, z, cube, cubeIndex, currentSlice);
                        updateMesh(cube, cubeIndex);
                    }

                    if (type == CUBE) {
                        outputMesh();
                    }
                }
            }

            if (type == SLICE) {
                outputMesh();
            }

            interrupted = Thread.interrupted();
        }

        if ((type == COMPLETE) && !interrupted) {
            outputMesh();
        }

        System.out.println("Cubes: " + (System.nanoTime() - time) / Math.pow(10, 6) + " ms"); //TODO remove
    }

    private void outputMesh() {
        FloatBuffer points = BufferUtils.createFloatBuffer(this.points.size() * 3);
        IntBuffer indices = BufferUtils.createIntBuffer(this.indices.size());

        this.points.forEach((p, i) -> {
            points.put(p.getX());
            points.put(p.getY());
            points.put(p.getZ());
        });
        this.indices.forEach(indices::put);
        points.flip();
        indices.flip();

        System.out.println("Pushing " + indices.limit() / 3 + " triangles.");
        meshConsumer.accept(points, indices);
        stop = true;

        if (type == COMPLETE) {
            return;
        }

        synchronized (this) {
            while (stop) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                    break;
                }
            }
        }
    }

    private Cube[][] updateSlices() {
        Cube[][] currentSlice;

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

        return currentSlice;
    }

    private Cube computeVertexes(int x, int y, int z, Cube[][] currentSlice) {
        Vertex v0, v1, v2, v3, v4, v5, v6, v7;

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

        return new Cube(v0, v1, v2, v3, v4, v5, v6, v7);
    }

    private void computeEdges(int x, int y, int z, Cube cube, int cubeIndex, Cube[][] currentSlice) {
        int edgeIndex = Tables.getEdgeIndex(cubeIndex);

        if ((edgeIndex & 1) == 1) { // Edge 0
            if (y != 0) {
                cube.setEdge(0, currentSlice[y - 1][x].getEdge(2));
            } else if (z != 0) {
                cube.setEdge(0, lowerSlice[y][x].getEdge(4));
            } else {
                cube.setEdge(0, interpolate(cube.getVertex(0), cube.getVertex(1), level));
            }
        }

        if ((edgeIndex & 2) == 2) { // Edge 1
            if (z != 0) {
                cube.setEdge(1, lowerSlice[y][x].getEdge(5));
            } else {
                cube.setEdge(1, interpolate(cube.getVertex(1), cube.getVertex(2), level));
            }
        }

        if ((edgeIndex & 4) == 4) { // Edge 2
            if (z != 0) {
                cube.setEdge(2, lowerSlice[y][x].getEdge(6));
            } else {
                cube.setEdge(2, interpolate(cube.getVertex(2), cube.getVertex(3), level));
            }
        }

        if ((edgeIndex & 8) == 8) { // Edge 3
            if (x != 0) {
                cube.setEdge(3, currentSlice[y][x - 1].getEdge(1));
            } else if (z != 0) {
                cube.setEdge(3, lowerSlice[y][x].getEdge(7));
            } else {
                cube.setEdge(3, interpolate(cube.getVertex(3), cube.getVertex(0), level));
            }
        }

        if ((edgeIndex & 16) == 16) { // Edge 4
            if (y != 0) {
                cube.setEdge(4, currentSlice[y - 1][x].getEdge(6));
            } else {
                cube.setEdge(4, interpolate(cube.getVertex(4), cube.getVertex(5), level));
            }
        }

        if ((edgeIndex & 32) == 32) { // Edge 5
            cube.setEdge(5, interpolate(cube.getVertex(5), cube.getVertex(6), level));
        }

        if ((edgeIndex & 64) == 64) { // Edge 6
            cube.setEdge(6, interpolate(cube.getVertex(6), cube.getVertex(7), level));
        }

        if ((edgeIndex & 128) == 128) { // Edge 7
            if (x != 0) {
                cube.setEdge(7, currentSlice[y][x - 1].getEdge(5));
            } else {
                cube.setEdge(7, interpolate(cube.getVertex(7), cube.getVertex(4), level));
            }
        }

        if ((edgeIndex & 256) == 256) { // Edge 8
            if (x != 0) {
                cube.setEdge(8, currentSlice[y][x - 1].getEdge(9));
            } else if (y != 0) {
                cube.setEdge(8, currentSlice[y - 1][x].getEdge(11));
            } else {
                cube.setEdge(8, interpolate(cube.getVertex(4), cube.getVertex(0), level));
            }
        }

        if ((edgeIndex & 512) == 512) { // Edge 9
            if (y != 0) {
                cube.setEdge(9, currentSlice[y - 1][x].getEdge(10));
            } else {
                cube.setEdge(9, interpolate(cube.getVertex(5), cube.getVertex(1), level));
            }
        }

        if ((edgeIndex & 1024) == 1024) { // Edge 10
            cube.setEdge(10, interpolate(cube.getVertex(6), cube.getVertex(2), level));
        }

        if ((edgeIndex & 2048) == 2048) { // Edge 11
            if (x != 0) {
                cube.setEdge(11, currentSlice[y][x - 1].getEdge(10));
            } else {
                cube.setEdge(11, interpolate(cube.getVertex(7), cube.getVertex(3), level));
            }
        }
    }

    private static Point3D interpolate(Vertex v1, Vertex v2, float level) {
        float x, y, z;
        float alpha;
        double min = Math.pow(10, -4);

        if (Math.abs(level - v1.getWeight()) < min) {
            return v1.getLocation();
        }

        if (Math.abs(level - v2.getWeight()) < min) {
            return v2.getLocation();
        }

        if (Math.abs(v1.getWeight() - v2.getWeight()) < min) {
            return v1.getLocation();
        }

        alpha = (level - v2.getWeight()) / (v1.getWeight() - v2.getWeight());
        x = alpha * v1.getX() + (1 - alpha) * v2.getX();
        y = alpha * v1.getY() + (1 - alpha) * v2.getY();
        z = alpha * v1.getZ() + (1 - alpha) * v2.getZ();

        return new Point3D(x, y, z);
    }

    private void updateMesh(Cube cube, int cubeIndex) {
        Point3D edge;
        Integer index;
        int newIndex = points.size();
        int[] triangles = Tables.getTriangleIndex(cubeIndex);

        for (int i = 0; i < triangles.length; i += 3) {

            if (triangles[i] == -1) {
                break;
            }

            for (int j = 0; j < 3; j++) {
                edge = cube.getEdge(triangles[i + j]);
                index = points.get(edge);
                if (index == null) {
                    points.put(edge, newIndex);
                    indices.add(newIndex);
                    newIndex++;
                } else {
                    indices.add(index);
                }
            }
        }
    }

    public void continueRun() {

        synchronized (this) {
            stop = false;
            this.notify();
        }
    }
}
