package controller.mc_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import gui.opengl.Vector3f;
import org.lwjgl.BufferUtils;

import static controller.mc_alg.MCRunner.Type.*;

public class MCRunner implements Runnable {

    public enum Type {CUBE, SLICE, COMPLETE}

    private final int SAVED_SLICES = 2;
    private final int POINTS_CAPACITY = 100000;

    private float level;
    private float[][][] data;
    private volatile boolean stop;
    private volatile boolean interrupted;
    private Type type;
    private Consumer<Mesh> meshConsumer;

    private Cube[][] upperSlice;
    private Cube[][] lowerSlice;

    private Map<Vertex, Integer> points;
    private List<Integer> indices;
    private List<Vector3f> normals;

    public MCRunner(float[][][] data, float level, Type type, Consumer<Mesh> meshConsumer) {
        this.level = level;
        this.data = data;
        this.stop = false;
        this.interrupted = false;
        this.type = type;
        this.meshConsumer = meshConsumer;

        this.points = new LinkedHashMap<>(POINTS_CAPACITY);
        this.indices = new LinkedList<>();
        this.normals = new LinkedList<>();
    }

    @Override
    public void run() {
        int cubeIndex;
        Cube[][] currentSlice;
        Cube cube;

        for (int z = 0; z < data.length - 1 && !interrupted; z++) {

            currentSlice = updateSlices();

            for (int y = 0; y < data[z].length - 1; y++) {

                for (int x = 0; x < data[z][y].length - 1; x++) {

                    computeVertexes(x, y, z, currentSlice);
                    cube = currentSlice[y][x];
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
    }

    private void outputMesh() {
        FloatBuffer points = BufferUtils.createFloatBuffer(this.points.size() * 3);
        FloatBuffer normals = BufferUtils.createFloatBuffer(this.normals.size() * 3);
        IntBuffer indices = BufferUtils.createIntBuffer(this.indices.size());

        this.points.forEach((p, i) -> {
            points.put(p.getLocation().getX());
            points.put(p.getLocation().getY());
            points.put(p.getLocation().getZ());
        });
        this.normals.forEach(v -> normals.put(v.getX()).put(v.getY()).put(v.getZ()));
        this.indices.forEach(indices::put);
        points.flip();
        normals.flip();
        indices.flip();

        System.out.println("Pushing " + indices.limit() / 3 + " triangles.");
        meshConsumer.accept(new Mesh(points, normals, indices));
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

            for (Cube[] cubes : lowerSlice) {
                for (int i = 0; i < cubes.length; i++) {
                    cubes[i] = new Cube();
                }
            }

            currentSlice = lowerSlice;
        } else if (upperSlice == null) {
            upperSlice = new Cube[data[0].length - 1][data[0][0].length - 1];

            for (Cube[] cubes : upperSlice) {
                for (int i = 0; i < cubes.length; i++) {
                    cubes[i] = new Cube();
                }
            }

            currentSlice = upperSlice;
        } else {
            currentSlice = lowerSlice;
            lowerSlice = upperSlice;
            upperSlice = currentSlice;
        }

        return currentSlice;
    }

    private void computeVertexes(int x, int y, int z, Cube[][] currentSlice) {
        WeightedVertex v;
        Cube cube = currentSlice[y][x];

        if (x != 0) {
            cube.setVertex(0, currentSlice[y][x - 1].getVertex(1));
        } else if (y != 0) {
            cube.setVertex(0, currentSlice[y - 1][x].getVertex(3));
        } else if (z != 0) {
            cube.setVertex(0, lowerSlice[y][x].getVertex(4));
        } else {
            v = cube.getVertex(0);
            v.setLocation(x, y, z);
            v.setWeight(weight(x, y, z));
            computeGradient(x, y, z, v);
        }

        if (y != 0) {
            cube.setVertex(1, currentSlice[y - 1][x].getVertex(2));
        } else if (z != 0) {
            cube.setVertex(1, lowerSlice[y][x].getVertex(5));
        } else {
            v = cube.getVertex(1);
            v.setLocation(x + 1, y, z);
            v.setWeight(weight(x + 1, y, z));
            computeGradient(x + 1, y, z, v);
        }

        if (z != 0) {
            cube.setVertex(2, lowerSlice[y][x].getVertex(6));
        } else {
            v = cube.getVertex(2);
            v.setLocation(x + 1, y + 1, z);
            v.setWeight(weight(x + 1, y + 1, z));
            computeGradient(x + 1, y + 1, z, v);
        }

        if (x != 0) {
            cube.setVertex(3, currentSlice[y][x - 1].getVertex(2));
        } else if (z != 0) {
            cube.setVertex(3, lowerSlice[y][x].getVertex(7));
        } else {
            v = cube.getVertex(3);
            v.setLocation(x, y + 1, z);
            v.setWeight(weight(x, y + 1, z));
            computeGradient(x, y + 1, z, v);
        }

        if (x != 0) {
            cube.setVertex(4, currentSlice[y][x - 1].getVertex(5));
        } else if (y != 0) {
            cube.setVertex(4, currentSlice[y - 1][x].getVertex(7));
        } else {
            v = cube.getVertex(4);
            v.setLocation(x, y, z + 1);
            v.setWeight(weight(x, y, z + 1));
            computeGradient(x, y, z + 1, v);
        }

        if (y != 0) {
            cube.setVertex(5, currentSlice[y - 1][x].getVertex(6));
        } else {
            v = cube.getVertex(5);
            v.setLocation(x + 1, y, z + 1);
            v.setWeight(weight(x + 1, y, z + 1));
            computeGradient(x + 1, y, z + 1, v);
        }

        v = cube.getVertex(6);
        v.setLocation(x + 1, y + 1, z + 1);
        v.setWeight(weight(x + 1, y + 1, z + 1));
        computeGradient(x + 1, y + 1, z + 1, v);

        if (x != 0) {
            cube.setVertex(7, currentSlice[y][x - 1].getVertex(6));
        } else {
            v = cube.getVertex(7);
            v.setLocation(x, y + 1, z + 1);
            v.setWeight(weight(x, y + 1, z + 1));
            computeGradient(x, y + 1, z + 1, v);
        }
    }

    private void computeGradient(int x, int y, int z, WeightedVertex v) {
        float gX = weight(x + 1, y, z) - weight(x - 1, y, z);
        float gY = weight(x, y + 1, z) - weight(x, y - 1, z);
        float gZ = weight(x, y, z + 1) - weight(x, y, z - 1);

        v.setNormal(gX, gY, gZ);
    }

    private float weight(int x, int y, int z) {

        if (z < 0 || z >= data.length) {
            return 0f;
        }

        if (y < 0 || y >= data[z].length) {
            return 0f;
        }

        if (x < 0 || x >= data[z][y].length) {
            return 0f;
        }

        return data[z][y][x];
    }

    private void computeEdges(int x, int y, int z, Cube cube, int cubeIndex, Cube[][] currentSlice) {
        int edgeIndex = Tables.getEdgeIndex(cubeIndex);

        if ((edgeIndex & 1) == 1) { // Edge 0
            if (y != 0) {
                cube.setEdge(0, currentSlice[y - 1][x].getEdge(2));
            } else if (z != 0) {
                cube.setEdge(0, lowerSlice[y][x].getEdge(4));
            } else {
                interpolate(cube.getVertex(0), cube.getVertex(1), cube.getEdge(0), level);
            }
        }

        if ((edgeIndex & 2) == 2) { // Edge 1
            if (z != 0) {
                cube.setEdge(1, lowerSlice[y][x].getEdge(5));
            } else {
                interpolate(cube.getVertex(1), cube.getVertex(2), cube.getEdge(1), level);
            }
        }

        if ((edgeIndex & 4) == 4) { // Edge 2
            if (z != 0) {
                cube.setEdge(2, lowerSlice[y][x].getEdge(6));
            } else {
                interpolate(cube.getVertex(2), cube.getVertex(3), cube.getEdge(2), level);
            }
        }

        if ((edgeIndex & 8) == 8) { // Edge 3
            if (x != 0) {
                cube.setEdge(3, currentSlice[y][x - 1].getEdge(1));
            } else if (z != 0) {
                cube.setEdge(3, lowerSlice[y][x].getEdge(7));
            } else {
                interpolate(cube.getVertex(3), cube.getVertex(0), cube.getEdge(3), level);
            }
        }

        if ((edgeIndex & 16) == 16) { // Edge 4
            if (y != 0) {
                cube.setEdge(4, currentSlice[y - 1][x].getEdge(6));
            } else {
                interpolate(cube.getVertex(4), cube.getVertex(5), cube.getEdge(4), level);
            }
        }

        if ((edgeIndex & 32) == 32) { // Edge 5
            interpolate(cube.getVertex(5), cube.getVertex(6), cube.getEdge(5), level);
        }

        if ((edgeIndex & 64) == 64) { // Edge 6
            interpolate(cube.getVertex(6), cube.getVertex(7), cube.getEdge(6), level);
        }

        if ((edgeIndex & 128) == 128) { // Edge 7
            if (x != 0) {
                cube.setEdge(7, currentSlice[y][x - 1].getEdge(5));
            } else {
                interpolate(cube.getVertex(7), cube.getVertex(4), cube.getEdge(7), level);
            }
        }

        if ((edgeIndex & 256) == 256) { // Edge 8
            if (x != 0) {
                cube.setEdge(8, currentSlice[y][x - 1].getEdge(9));
            } else if (y != 0) {
                cube.setEdge(8, currentSlice[y - 1][x].getEdge(11));
            } else {
                interpolate(cube.getVertex(4), cube.getVertex(0), cube.getEdge(8), level);
            }
        }

        if ((edgeIndex & 512) == 512) { // Edge 9
            if (y != 0) {
                cube.setEdge(9, currentSlice[y - 1][x].getEdge(10));
            } else {
                interpolate(cube.getVertex(5), cube.getVertex(1), cube.getEdge(9), level);
            }
        }

        if ((edgeIndex & 1024) == 1024) { // Edge 10
            interpolate(cube.getVertex(6), cube.getVertex(2), cube.getEdge(10), level);
        }

        if ((edgeIndex & 2048) == 2048) { // Edge 11
            if (x != 0) {
                cube.setEdge(11, currentSlice[y][x - 1].getEdge(10));
            } else {
                interpolate(cube.getVertex(7), cube.getVertex(3), cube.getEdge(11), level);
            }
        }
    }

    private static void interpolate(WeightedVertex v1, WeightedVertex v2, Vertex edge, float level) {
        float edgeX, edgeY, edgeZ;
        float normalX, normalY, normalZ;
        double min = Math.pow(10, -4);
        float alpha;

        if (Math.abs(level - v1.getWeight()) < min) {
            edge.setLocation(v1.getLocation());
            edge.setNormal(v1.getNormal());
            return;
        }

        if (Math.abs(level - v2.getWeight()) < min) {
            edge.setLocation(v2.getLocation());
            edge.setNormal(v2.getNormal());
            return;
        }

        if (Math.abs(v1.getWeight() - v2.getWeight()) < min) {
            edge.setLocation(v1.getLocation());
            edge.setNormal(v1.getNormal());
            return;
        }

        alpha = (level - v2.getWeight()) / (v1.getWeight() - v2.getWeight());

        normalX = alpha * v1.getNormal().getX() + (1 - alpha) * v2.getNormal().getX();
        normalY = alpha * v1.getNormal().getY() + (1 - alpha) * v2.getNormal().getY();
        normalZ = alpha * v1.getNormal().getZ() + (1 - alpha) * v2.getNormal().getZ();

        edgeX = alpha * v1.getLocation().getX() + (1 - alpha) * v2.getLocation().getX();
        edgeY = alpha * v1.getLocation().getY() + (1 - alpha) * v2.getLocation().getY();
        edgeZ = alpha * v1.getLocation().getZ() + (1 - alpha) * v2.getLocation().getZ();

        edge.setLocation(edgeX, edgeY, edgeZ);
        edge.setNormal(normalX, normalY, normalZ);
    }

    private void updateMesh(Cube cube, int cubeIndex) {
        Vertex edge;
        Integer index;
        int newIndex = points.size();
        int[] triangles = Tables.getTriangleIndex(cubeIndex);

        for (int i = 0; i < triangles.length; i += 3) {

            if (triangles[i] == -1) {
                break;
            }

            for (int j = 0; j < 3; j++) {

                try {
                    edge = cube.getEdge(triangles[i + j]).clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    return;
                }

                index = points.get(edge);

                if (index == null) {
                    points.put(edge, newIndex);
                    normals.add(edge.getNormal());
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
