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

import static controller.mc_alg.MCRunner.Type.COMPLETE;
import static controller.mc_alg.MCRunner.Type.SLICE;

public class MCRunner implements Runnable {

    public enum Type {SLICE, COMPLETE}

    private final int SAVED_SLICES = 2;
    private final int POINTS_CAPACITY = 100000;

    private float level;
    private float[][][] data;
    private volatile boolean stop;
    private volatile boolean interrupted;
    private Type type;
    private Consumer<Mesh> meshConsumer;

    private Cube[][][] slices;
    private int sliceIndex;

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
        this.slices = new Cube[SAVED_SLICES][][];
        this.sliceIndex = 0;
        this.points = new LinkedHashMap<>(POINTS_CAPACITY);
        this.indices = new LinkedList<>();
        this.normals = new LinkedList<>();
    }

    @Override
    public void run() {
        int cubeIndex;
        Cube cube;

        for (int z = 0; z < data.length - 1 && !interrupted; z++) {

            updateSlices();

            for (int y = 0; y < data[z].length - 1; y++) {

                for (int x = 0; x < data[z][y].length - 1; x++) {

                    cube = computeVertexes(x, y, z);
                    slices[sliceIndex][y][x] = cube;

                    cubeIndex = cube.getIndex(level);

                    if ((cubeIndex != 0) && (cubeIndex != 255)) {
                        computeEdges(x, y, z, cube, cubeIndex);
                        updateMesh(cube, cubeIndex);
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
            points.put(p.getX());
            points.put(p.getY());
            points.put(p.getZ());
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

    private void updateSlices() {
        int lastArrayIndex = slices.length - 1;

        if (sliceIndex < lastArrayIndex) {
            sliceIndex++;
            slices[sliceIndex] = new Cube[data[0].length - 1][data[0][0].length - 1];
        } else {
            System.arraycopy(slices, 1, slices, 0, lastArrayIndex);
            slices[lastArrayIndex] = new Cube[data[0].length - 1][data[0][0].length - 1];
        }
//
//        if (lowerSlice == null) {
//            lowerSlice = new Cube[data[0].length - 1][data[0][0].length - 1];
//            currentSlice = lowerSlice;
//        } else if (upperSlice == null) {
//            upperSlice = new Cube[data[0].length - 1][data[0][0].length - 1];
//            currentSlice = upperSlice;
//        } else {
//            lowerSlice = upperSlice;
//            upperSlice = new Cube[data[0].length - 1][data[0][0].length - 1];
//            currentSlice = upperSlice;
//        }
    }

    private Cube computeVertexes(int x, int y, int z) {
        WeightedVertex v0, v1, v2, v3, v4, v5, v6, v7;

        if (x != 0) {
            v0 = slices[sliceIndex][y][x - 1].getVertex(1);
        } else if (y != 0) {
            v0 = slices[sliceIndex][y - 1][x].getVertex(3);
        } else if (z != 0) {
            v0 = slices[sliceIndex - 1][y][x].getVertex(4);
        } else {
            v0 = new WeightedVertex(x, y, z, weight(x, y, z));
            v0.setNormal(computeGradient(x, y, z));
        }

        if (y != 0) {
            v1 = slices[sliceIndex][y - 1][x].getVertex(2);
        } else if (z != 0) {
            v1 = slices[sliceIndex - 1][y][x].getVertex(5);
        } else {
            v1 = new WeightedVertex(x + 1, y, z, weight(x + 1, y, z));
            v1.setNormal(computeGradient(x + 1, y, z));
        }

        if (z != 0) {
            v2 = slices[sliceIndex - 1][y][x].getVertex(6);
        } else {
            v2 = new WeightedVertex(x + 1, y + 1, z, weight(x + 1, y + 1, z));
            v2.setNormal(computeGradient(x + 1, y + 1, z));
        }

        if (x != 0) {
            v3 = slices[sliceIndex][y][x - 1].getVertex(2);
        } else if (z != 0) {
            v3 = slices[sliceIndex - 1][y][x].getVertex(7);
        } else {
            v3 = new WeightedVertex(x, y + 1, z, weight(x, y + 1, z));
            v3.setNormal(computeGradient(x, y + 1, z));
        }

        if (x != 0) {
            v4 = slices[sliceIndex][y][x - 1].getVertex(5);
        } else if (y != 0) {
            v4 = slices[sliceIndex][y - 1][x].getVertex(7);
        } else {
            v4 = new WeightedVertex(x, y, z + 1, weight(x, y, z + 1));
            v4.setNormal(computeGradient(x, y, z + 1));
        }

        if (y != 0) {
            v5 = slices[sliceIndex][y - 1][x].getVertex(6);
        } else {
            v5 = new WeightedVertex(x + 1, y, z + 1, weight(x + 1, y, z + 1));
            v5.setNormal(computeGradient(x + 1, y, z + 1));
        }

        v6 = new WeightedVertex(x + 1, y + 1, z + 1, weight(x + 1, y + 1, z + 1));
        v6.setNormal(computeGradient(x + 1, y + 1, z + 1));

        if (x != 0) {
            v7 = slices[sliceIndex][y][x - 1].getVertex(6);
        } else {
            v7 = new WeightedVertex(x, y + 1, z + 1, weight(x, y + 1, z + 1));
            v7.setNormal(computeGradient(x, y + 1, z + 1));
        }

        return new Cube(v0, v1, v2, v3, v4, v5, v6, v7);
    }

    private Vector3f computeGradient(int x, int y, int z) {
        float gX = weight(x + 1, y, z) - weight(x - 1, y, z);
        float gY = weight(x, y + 1, z) - weight(x, y - 1, z);
        float gZ = weight(x, y, z + 1) - weight(x, y, z - 1);

        return new Vector3f(gX, gY, gZ);
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

    private void computeEdges(int x, int y, int z, Cube cube, int cubeIndex) {
        int edgeIndex = Tables.getEdgeIndex(cubeIndex);

        if ((edgeIndex & 1) == 1) { // Edge 0
            if (y != 0) {
                cube.setEdge(0, slices[sliceIndex][y - 1][x].getEdge(2));
            } else if (z != 0) {
                cube.setEdge(0, slices[sliceIndex - 1][y][x].getEdge(4));
            } else {
                cube.setEdge(0, interpolate(cube.getVertex(0), cube.getVertex(1), level));
            }
        }

        if ((edgeIndex & 2) == 2) { // Edge 1
            if (z != 0) {
                cube.setEdge(1, slices[sliceIndex - 1][y][x].getEdge(5));
            } else {
                cube.setEdge(1, interpolate(cube.getVertex(1), cube.getVertex(2), level));
            }
        }

        if ((edgeIndex & 4) == 4) { // Edge 2
            if (z != 0) {
                cube.setEdge(2, slices[sliceIndex - 1][y][x].getEdge(6));
            } else {
                cube.setEdge(2, interpolate(cube.getVertex(2), cube.getVertex(3), level));
            }
        }

        if ((edgeIndex & 8) == 8) { // Edge 3
            if (x != 0) {
                cube.setEdge(3, slices[sliceIndex][y][x - 1].getEdge(1));
            } else if (z != 0) {
                cube.setEdge(3, slices[sliceIndex - 1][y][x].getEdge(7));
            } else {
                cube.setEdge(3, interpolate(cube.getVertex(3), cube.getVertex(0), level));
            }
        }

        if ((edgeIndex & 16) == 16) { // Edge 4
            if (y != 0) {
                cube.setEdge(4, slices[sliceIndex][y - 1][x].getEdge(6));
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
                cube.setEdge(7, slices[sliceIndex][y][x - 1].getEdge(5));
            } else {
                cube.setEdge(7, interpolate(cube.getVertex(7), cube.getVertex(4), level));
            }
        }

        if ((edgeIndex & 256) == 256) { // Edge 8
            if (x != 0) {
                cube.setEdge(8, slices[sliceIndex][y][x - 1].getEdge(9));
            } else if (y != 0) {
                cube.setEdge(8, slices[sliceIndex][y - 1][x].getEdge(11));
            } else {
                cube.setEdge(8, interpolate(cube.getVertex(4), cube.getVertex(0), level));
            }
        }

        if ((edgeIndex & 512) == 512) { // Edge 9
            if (y != 0) {
                cube.setEdge(9, slices[sliceIndex][y - 1][x].getEdge(10));
            } else {
                cube.setEdge(9, interpolate(cube.getVertex(5), cube.getVertex(1), level));
            }
        }

        if ((edgeIndex & 1024) == 1024) { // Edge 10
            cube.setEdge(10, interpolate(cube.getVertex(6), cube.getVertex(2), level));
        }

        if ((edgeIndex & 2048) == 2048) { // Edge 11
            if (x != 0) {
                cube.setEdge(11, slices[sliceIndex][y][x - 1].getEdge(10));
            } else {
                cube.setEdge(11, interpolate(cube.getVertex(7), cube.getVertex(3), level));
            }
        }
    }

    private static Vertex interpolate(WeightedVertex v1, WeightedVertex v2, float level) {
        float edgeX, edgeY, edgeZ;
        float normalX, normalY, normalZ;
        double min = Math.pow(10, -4);
        float alpha;
        Vertex edge;

        if (Math.abs(level - v1.getWeight()) < min) {
            return v1;
        }

        if (Math.abs(level - v2.getWeight()) < min) {
            return v2;
        }

        if (Math.abs(v1.getWeight() - v2.getWeight()) < min) {
            return v1;
        }

        alpha = (level - v2.getWeight()) / (v1.getWeight() - v2.getWeight());

        normalX = alpha * v1.getNormal().getX() + (1 - alpha) * v2.getNormal().getX();
        normalY = alpha * v1.getNormal().getY() + (1 - alpha) * v2.getNormal().getY();
        normalZ = alpha * v1.getNormal().getZ() + (1 - alpha) * v2.getNormal().getZ();

        edgeX = alpha * v1.getX() + (1 - alpha) * v2.getX();
        edgeY = alpha * v1.getY() + (1 - alpha) * v2.getY();
        edgeZ = alpha * v1.getZ() + (1 - alpha) * v2.getZ();

        edge = new Vertex(edgeX, edgeY, edgeZ);
        edge.setNormal(new Vector3f(normalX, normalY, normalZ));

        return edge;
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
                edge = cube.getEdge(triangles[i + j]);
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
