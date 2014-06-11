package controller.mc_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.lwjgl.BufferUtils;
import util.Vector3f;

import static controller.mc_alg.MCRunner.Type.*;

/**
 * Runnable that performs the Marching Cubes algorithm over a 3D array of floats that are interpreted as density data.
 * Can be configured to update the resulting triangle mesh after every cube, slice, or after the whole computation is
 * finished. Optionally the computation can pause after every update.
 */
public class MCRunner implements Runnable {

    /**
     * The type for the <code>MCRunner</code>. Determines when mesh updates take place.
     */
    public enum Type {

        /**
         * Mesh updates will take place after every single cube.
         */
        CUBE,

        /**
         * Mesh updates will take place after every slice.
         */
        SLICE,

        /**
         * There will be a single mesh update after the computation is finished.
         */
        COMPLETE
    }

    private DoubleProperty progressProperty; // 0 or negative => 0%, 1 or greater => 100%

    // the initial capacity for the points HashMap, fairly high to reduce rehashing
    private final int POINTS_CAPACITY = 100000;

    private float[][][] data;
    private float level;
    private int gridSize;
    private Type type;
    private Consumer<Mesh> meshConsumer; // will be called with the current mesh after every mesh update

    private volatile boolean stopping; // whether this MCRunner stops after every mesh update
    private volatile boolean stopped; // whether this MCRunner has stopped
    private boolean interrupted; // whether the executing Thread was interrupted

    /*
     * During the execution of the algorithm two slices of the geometry (calculated vertices of cubes, normals,
     * gradients and triangle vertices) will remain in memory. After every slice of data the two references (upperSlice,
     * lowerSlice) will be swapped. The upperSlice will be worked on by the algorithm (overwriting the values that
     * are stored in the cubes from the previous iteration), the lowerSlice (and parts of upperSlice that have already
     * been updated) will be used for lookup of previously calculated information.
     */
    private Cube[][] upperSlice;
    private Cube[][] lowerSlice;

    private int numLastTriangles; // how many triangles were pushed in the last mesh update
    private Map<Vertex, Integer> points; // the mesh vertices, a Map with iteration order = insertion order is used
    private List<Integer> indices; // indices into the points and normals, defines triangles that make up the mesh
    private List<Vector3f> normals; // the normals at the mesh vertices

    /**
     * Constructs a new <code>MCRunner</code> that performs the Marching Cubes algorithm over the given data.
     * GridSize will be 1 and the type will be COMPLETE.
     *
     * @param data
     *         the data for the Marching Cubes algorithm
     * @param level
     *         the density level for the Marching Cubes algorithm
     *
     * @throws NullPointerException
     *         if <code>data</code> or <code>type</code> is <code>null</code>
     * @throws IllegalArgumentException
     *         if <code>level</code> is smaller than 0 or <code>gridSize</code> is smaller than 1
     */
    public MCRunner(float[][][] data, float level) {
        this(data, level, 1, COMPLETE);
    }

    /**
     * Constructs a new <code>MCRunner</code> that performs the Marching Cubes algorithm over the given data.
     *
     * @param data
     *         the data for the Marching Cubes algorithm
     * @param level
     *         the density level for the Marching Cubes algorithm
     * @param gridSize
     *         the grid size (that is the x/y/z dimensions of the cubes)
     * @param type
     *         the type for the <code>MCRunner</code>
     *
     * @throws NullPointerException
     *         if <code>data</code> or <code>type</code> is <code>null</code>
     * @throws IllegalArgumentException
     *         if <code>level</code> is smaller than 0 or <code>gridSize</code> is smaller than 1
     */
    public MCRunner(float[][][] data, float level, int gridSize, Type type) {
        Objects.requireNonNull(data, "data must not be null!");

        if (!(level >= 0)) {
            throw new IllegalArgumentException("level must be greater or equal to 0!");
        }

        if (!(gridSize >= 1)) {
            throw new IllegalArgumentException("gridSize must be greater or equal to 1!");
        }

        Objects.requireNonNull(type, "type must not be null!");

        progressProperty = new SimpleDoubleProperty();

        this.data = data;
        this.level = level;
        this.gridSize = gridSize;
        this.type = type;

        this.stopping = false;
        this.stopped = false;
        this.interrupted = false;

        this.numLastTriangles = 0;
        this.points = new LinkedHashMap<>(POINTS_CAPACITY);
        this.indices = new LinkedList<>();
        this.normals = new LinkedList<>();
    }

    /**
     * Returns the progress property of this <code>MCRunner</code>. Will have a value between 0 - 1 indicating
     * 0% to 100% done.
     *
     * @return the progress property
     */
    public DoubleProperty progressProperty() {
        return progressProperty;
    }

    /**
     * Returns whether this <code>MCRunner</code> is stopping after every mesh update.
     * The default is <code>false</code>.
     *
     * @return true iff the <code>MCRunner</code> is stopping after every mesh update
     */
    public boolean isStopping() {
        return stopping;
    }

    /**
     * Sets whether this <code>MCRunner</code> is stopping after every mesh update.
     * The default is <code>false</code>.
     *
     * @param stopping
     *         whether this <code>MCRunner</code> is stopping after every mesh update
     */
    public void setStopping(boolean stopping) {
        this.stopping = stopping;
    }

    /**
     * Sets the method that will be called with the resulting <code>Mesh</code> after every mesh update.
     *
     * @param meshConsumer
     *         the <code>Consumer</code> that should accept the <code>Mesh</code>
     */
    public void setOnMeshFinished(Consumer<Mesh> meshConsumer) {
        this.meshConsumer = meshConsumer;
    }

    @Override
    public void run() {
        int cubesInSlice = data[0].length * data[0][0].length;
        double numCubes = (data.length * cubesInSlice) / Math.pow(gridSize, 3);
        int doneCubes = 0;
        int cubeIndex;
        Cube[][] currentSlice;
        Cube cube;

        for (int z = 0; z < data.length; z += gridSize) {

            currentSlice = updateSlices();

            for (int y = 0; y < data[z].length; y += gridSize) {

                for (int x = 0; x < data[z][y].length; x += gridSize) {

                    cube = computeVertices(x, y, z, currentSlice);
                    cubeIndex = cube.getIndex(level);

                    if ((cubeIndex != 0) && (cubeIndex != 255)) {
                        computeEdges(x, y, z, cube, cubeIndex, currentSlice);
                        updateMesh(cube, cubeIndex);
                    }

                    if (type == CUBE) {
                        outputMesh();
                    }


                    interrupted = Thread.interrupted();
                    if (interrupted) {
                        return;
                    }
                }
            }

            if (type == SLICE) {
                outputMesh();
            }

            progressProperty.set((doneCubes += cubesInSlice) / numCubes);
        }

        if (type == COMPLETE) {
            outputMesh();
        }
    }

    /**
     * Converts the <code>points</code>, <code>normals</code> and <code>indices</code> into a <code>Mesh</code> and
     * feeds the <code>meshConsumer</code> with it. If no new triangles were created or the consumer is
     * <code>null</code> no update will be performed. If the type is not <code>COMPLETE</code> (in which case this
     * method is called only once) and this <code>MCRunner</code> is stopping this method stops the run.
     */
    private void outputMesh() {

        if (this.indices.size() <= numLastTriangles) {
            return;
        } else {
            numLastTriangles = this.indices.size();
        }

        if (meshConsumer != null) {
            FloatBuffer points = BufferUtils.createFloatBuffer(this.points.size() * 3);
            FloatBuffer normals = BufferUtils.createFloatBuffer(this.normals.size() * 3);
            FloatBuffer normalLines = BufferUtils.createFloatBuffer(this.normals.size() * 6);
            IntBuffer indices = BufferUtils.createIntBuffer(this.indices.size());

            Iterator<Map.Entry<Vertex, Integer>> pointsIt = this.points.entrySet().iterator();
            Iterator<Vector3f> normalsIt = this.normals.iterator();

            Vertex point;
            Vector3f normal;
            Vector3f normalLinePoint;
            while (pointsIt.hasNext() && normalsIt.hasNext()) {
                point = pointsIt.next().getKey();
                normal = normalsIt.next();
                normalLinePoint = point.getLocation().add(normal);

                points.put(point.getLocation().getX());
                points.put(point.getLocation().getY());
                points.put(point.getLocation().getZ());

                normals.put(normal.getX());
                normals.put(normal.getY());
                normals.put(normal.getZ());

                normalLines.put(point.getLocation().getX());
                normalLines.put(point.getLocation().getY());
                normalLines.put(point.getLocation().getZ());
                normalLines.put(normalLinePoint.getX());
                normalLines.put(normalLinePoint.getY());
                normalLines.put(normalLinePoint.getZ());
            }
            this.indices.forEach(indices::put);

            points.flip();
            normals.flip();
            indices.flip();
            normalLines.flip();

            System.out.println("Pushing " + indices.limit() / 3 + " triangles."); //TODO remove
            meshConsumer.accept(new Mesh(points, normals, indices, normalLines));
        }

        if (type == COMPLETE) {
            return;
        }

        if (stopping) {
            stopRun();
        }
    }

    /**
     * Updates upper- and lowerSlice references as described at their declaration.
     * If this is the first or second time the method is called, upper- or lowerSlice will be initialized.
     *
     * @return the slice that the algorithm should currently work on
     */
    private Cube[][] updateSlices() {
        Cube[][] currentSlice;

        if (lowerSlice == null) {
            int yDim = (int) Math.ceil(data[0].length / (float) gridSize);
            int xDim = (int) Math.ceil(data[0][0].length / (float) gridSize);

            lowerSlice = new Cube[yDim][xDim];

            for (Cube[] cubes : lowerSlice) {
                for (int i = 0; i < cubes.length; i++) {
                    cubes[i] = new Cube();
                }
            }

            currentSlice = lowerSlice;
        } else if (upperSlice == null) {
            int yDim = (int) Math.ceil(data[0].length / (float) gridSize);
            int xDim = (int) Math.ceil(data[0][0].length / (float) gridSize);

            upperSlice = new Cube[yDim][xDim];

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

    /**
     * Computes the locations, weights and gradients of the vertices of the cube whose vertex 0 is at the given
     * position in the <code>data</code>
     *
     * @param x the x coordinate of the cubes vertex 0
     * @param y the y coordinate of the cubes vertex 0
     * @param z the z coordinate of the cubes vertex 0
     * @param currentSlice the slice the algorithm is currently working on
     * @return the <code>Cube</code> whose vertices were computed
     */
    private Cube computeVertices(int x, int y, int z, Cube[][] currentSlice) {
        WeightedVertex v;
        int cubeX = x / gridSize;
        int cubeY = y / gridSize;

        Cube cube = currentSlice[cubeY][cubeX];

        if (x != 0) {
            cube.setVertex(0, currentSlice[cubeY][cubeX - 1].getVertex(1));
        } else if (y != 0) {
            cube.setVertex(0, currentSlice[cubeY - 1][cubeX].getVertex(3));
        } else if (z != 0) {
            cube.setVertex(0, lowerSlice[cubeY][cubeX].getVertex(4));
        } else {
            v = cube.getVertex(0);
            v.setLocation(x, y, z);
            v.setWeight(weight(x, y, z));
            computeGradient(x, y, z, v);
        }

        if (y != 0) {
            cube.setVertex(1, currentSlice[cubeY - 1][cubeX].getVertex(2));
        } else if (z != 0) {
            cube.setVertex(1, lowerSlice[cubeY][cubeX].getVertex(5));
        } else {
            v = cube.getVertex(1);
            v.setLocation(x + gridSize, y, z);
            v.setWeight(weight(x + gridSize, y, z));
            computeGradient(x + gridSize, y, z, v);
        }

        if (z != 0) {
            cube.setVertex(2, lowerSlice[cubeY][cubeX].getVertex(6));
        } else {
            v = cube.getVertex(2);
            v.setLocation(x + gridSize, y + gridSize, z);
            v.setWeight(weight(x + gridSize, y + gridSize, z));
            computeGradient(x + gridSize, y + gridSize, z, v);
        }

        if (x != 0) {
            cube.setVertex(3, currentSlice[cubeY][cubeX - 1].getVertex(2));
        } else if (z != 0) {
            cube.setVertex(3, lowerSlice[cubeY][cubeX].getVertex(7));
        } else {
            v = cube.getVertex(3);
            v.setLocation(x, y + gridSize, z);
            v.setWeight(weight(x, y + gridSize, z));
            computeGradient(x, y + gridSize, z, v);
        }

        if (x != 0) {
            cube.setVertex(4, currentSlice[cubeY][cubeX - 1].getVertex(5));
        } else if (y != 0) {
            cube.setVertex(4, currentSlice[cubeY - 1][cubeX].getVertex(7));
        } else {
            v = cube.getVertex(4);
            v.setLocation(x, y, z + gridSize);
            v.setWeight(weight(x, y, z + gridSize));
            computeGradient(x, y, z + gridSize, v);
        }

        if (y != 0) {
            cube.setVertex(5, currentSlice[cubeY - 1][cubeX].getVertex(6));
        } else {
            v = cube.getVertex(5);
            v.setLocation(x + gridSize, y, z + gridSize);
            v.setWeight(weight(x + gridSize, y, z + gridSize));
            computeGradient(x + gridSize, y, z + gridSize, v);
        }

        v = cube.getVertex(6);
        v.setLocation(x + gridSize, y + gridSize, z + gridSize);
        v.setWeight(weight(x + gridSize, y + gridSize, z + gridSize));
        computeGradient(x + gridSize, y + gridSize, z + gridSize, v);

        if (x != 0) {
            cube.setVertex(7, currentSlice[cubeY][cubeX - 1].getVertex(6));
        } else {
            v = cube.getVertex(7);
            v.setLocation(x, y + gridSize, z + gridSize);
            v.setWeight(weight(x, y + gridSize, z + gridSize));
            computeGradient(x, y + gridSize, z + gridSize, v);
        }

        return cube;
    }

    /**
     * Computes the gradient (using central differences) of the <code>WeightedVertex</code> <code>v</code> located
     * at the given position.
     *
     * @param x the x coordinate of the vertex
     * @param y the y coordinate of the vertex
     * @param z the z coordinate of the vertex
     * @param v the vertex
     */
    private void computeGradient(int x, int y, int z, WeightedVertex v) {
        float gX = weight(x - gridSize, y, z) - weight(x + gridSize, y, z);
        float gY = weight(x, y - gridSize, z) - weight(x, y + gridSize, z);
        float gZ = weight(x, y, z - gridSize) - weight(x, y, z + gridSize);

        v.setNormal(gX, gY, gZ);
    }

    /**
     * Returns the weight (density) at the given position. If the position is out of bounds of the <code>data</code>
     * array this method will return 0.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the weight
     */
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

    /**
     * Computes the position and normal of all appropriate triangle vertices (that lie on the edges of the cube).
     *
     * @param x the x coordinate of the cubes vertex 0
     * @param y the y coordinate of the cubes vertex 0
     * @param z the z coordinate of the cubes vertex 0
     * @param cube the cube whose edges are to be computed
     * @param cubeIndex the index of the cube (see {@link controller.mc_alg.Cube#getIndex(float)})
     * @param currentSlice the slice the algorithm is currently working on
     */
    private void computeEdges(int x, int y, int z, Cube cube, int cubeIndex, Cube[][] currentSlice) {
        int edgeIndex = Tables.getEdgeIndex(cubeIndex);
        int cubeX = x / gridSize;
        int cubeY = y / gridSize;

        if ((edgeIndex & 1) == 1) { // Edge 0
            if (y != 0) {
                cube.setEdge(0, currentSlice[cubeY - 1][cubeX].getEdge(2));
            } else if (z != 0) {
                cube.setEdge(0, lowerSlice[cubeY][cubeX].getEdge(4));
            } else {
                interpolate(cube.getVertex(0), cube.getVertex(1), cube.getEdge(0));
            }
        }

        if ((edgeIndex & 2) == 2) { // Edge 1
            if (z != 0) {
                cube.setEdge(1, lowerSlice[cubeY][cubeX].getEdge(5));
            } else {
                interpolate(cube.getVertex(1), cube.getVertex(2), cube.getEdge(1));
            }
        }

        if ((edgeIndex & 4) == 4) { // Edge 2
            if (z != 0) {
                cube.setEdge(2, lowerSlice[cubeY][cubeX].getEdge(6));
            } else {
                interpolate(cube.getVertex(2), cube.getVertex(3), cube.getEdge(2));
            }
        }

        if ((edgeIndex & 8) == 8) { // Edge 3
            if (x != 0) {
                cube.setEdge(3, currentSlice[cubeY][cubeX - 1].getEdge(1));
            } else if (z != 0) {
                cube.setEdge(3, lowerSlice[cubeY][cubeX].getEdge(7));
            } else {
                interpolate(cube.getVertex(3), cube.getVertex(0), cube.getEdge(3));
            }
        }

        if ((edgeIndex & 16) == 16) { // Edge 4
            if (y != 0) {
                cube.setEdge(4, currentSlice[cubeY - 1][cubeX].getEdge(6));
            } else {
                interpolate(cube.getVertex(4), cube.getVertex(5), cube.getEdge(4));
            }
        }

        if ((edgeIndex & 32) == 32) { // Edge 5
            interpolate(cube.getVertex(5), cube.getVertex(6), cube.getEdge(5));
        }

        if ((edgeIndex & 64) == 64) { // Edge 6
            interpolate(cube.getVertex(6), cube.getVertex(7), cube.getEdge(6));
        }

        if ((edgeIndex & 128) == 128) { // Edge 7
            if (x != 0) {
                cube.setEdge(7, currentSlice[cubeY][cubeX - 1].getEdge(5));
            } else {
                interpolate(cube.getVertex(7), cube.getVertex(4), cube.getEdge(7));
            }
        }

        if ((edgeIndex & 256) == 256) { // Edge 8
            if (x != 0) {
                cube.setEdge(8, currentSlice[cubeY][cubeX - 1].getEdge(9));
            } else if (y != 0) {
                cube.setEdge(8, currentSlice[cubeY - 1][cubeX].getEdge(11));
            } else {
                interpolate(cube.getVertex(4), cube.getVertex(0), cube.getEdge(8));
            }
        }

        if ((edgeIndex & 512) == 512) { // Edge 9
            if (y != 0) {
                cube.setEdge(9, currentSlice[cubeY - 1][cubeX].getEdge(10));
            } else {
                interpolate(cube.getVertex(5), cube.getVertex(1), cube.getEdge(9));
            }
        }

        if ((edgeIndex & 1024) == 1024) { // Edge 10
            interpolate(cube.getVertex(6), cube.getVertex(2), cube.getEdge(10));
        }

        if ((edgeIndex & 2048) == 2048) { // Edge 11
            if (x != 0) {
                cube.setEdge(11, currentSlice[cubeY][cubeX - 1].getEdge(10));
            } else {
                interpolate(cube.getVertex(7), cube.getVertex(3), cube.getEdge(11));
            }
        }
    }

    /**
     * Linearly interpolates the position and normal of <code>edge</code> (that is assumed to lie on the edge between
     * <code>v1</code> and <code>v2</code>).
     *
     * @param v1 the first vertex of a cube
     * @param v2 the second vertex of a cube
     * @param edge the vertex of a triangle whose position and normal is to be interpolated
     */
    private void interpolate(WeightedVertex v1, WeightedVertex v2, Vertex edge) {
        float edgeX, edgeY, edgeZ;
        float normalX, normalY, normalZ;
        double min = Math.pow(10, -4);
        double length;
        float alpha;

        if (Math.abs(level - v1.getWeight()) < min) {
            edge.setLocation(v1.getLocation());
            edge.setNormal(v1.getNormal().normalized());
            return;
        }

        if (Math.abs(level - v2.getWeight()) < min) {
            edge.setLocation(v2.getLocation());
            edge.setNormal(v2.getNormal().normalized());
            return;
        }

        if (Math.abs(v1.getWeight() - v2.getWeight()) < min) {
            edge.setLocation(v1.getLocation());
            edge.setNormal(v1.getNormal().normalized());
            return;
        }

        alpha = (level - v2.getWeight()) / (v1.getWeight() - v2.getWeight());

        normalX = alpha * v1.getNormal().getX() + (1 - alpha) * v2.getNormal().getX();
        normalY = alpha * v1.getNormal().getY() + (1 - alpha) * v2.getNormal().getY();
        normalZ = alpha * v1.getNormal().getZ() + (1 - alpha) * v2.getNormal().getZ();

        length = Math.sqrt(Math.pow(normalX, 2) + Math.pow(normalY, 2) + Math.pow(normalZ, 2));
        normalX /= length;
        normalY /= length;
        normalZ /= length;

        edgeX = alpha * v1.getLocation().getX() + (1 - alpha) * v2.getLocation().getX();
        edgeY = alpha * v1.getLocation().getY() + (1 - alpha) * v2.getLocation().getY();
        edgeZ = alpha * v1.getLocation().getZ() + (1 - alpha) * v2.getLocation().getZ();

        edge.setLocation(edgeX, edgeY, edgeZ);
        edge.setNormal(normalX, normalY, normalZ);
    }

    /**
     * Updates the <code>points</code>, <code>normals</code>, and <code>indices</code> with triangles constructed
     * from the edges of the given <code>Cube</code> according to
     * {@link controller.mc_alg.Tables#getTriangleIndex(int)}.
     *
     * @param cube the cube with whose edges the mesh is to be updated
     * @param cubeIndex the index of the cube (see {@link controller.mc_alg.Cube#getIndex(float)})
     */
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

    /**
     * Stops the execution of the Marching Cubes algorithm. No mesh update will be produced until after
     * {@link #continueRun()} is called.
     */
    public void stopRun() {

        synchronized (this) {
            stopped = true;

            while (stopped) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                    break;
                }
            }
        }
    }

    /**
     * Restarts the execution of the Marching Cubes algorithm.
     */
    public void continueRun() {

        synchronized (this) {
            stopped = false;
            this.notify();
        }
    }
}
