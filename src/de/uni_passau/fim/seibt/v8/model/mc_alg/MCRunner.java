package de.uni_passau.fim.seibt.v8.model.mc_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import de.uni_passau.fim.seibt.v8.model.mc_alg.mc_volume.MCVolume;
import de.uni_passau.fim.seibt.v8.util.Buffers;
import de.uni_passau.fim.seibt.v8.util.Vector3f;

import static de.uni_passau.fim.seibt.v8.model.mc_alg.MCRunner.Type.*;

/**
 * <code>Runnable</code> that performs the Marching Cubes algorithm over the values of a given <code>MCVolume</code>.
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

    private Map<Long, Vertex> edgeCache;

    private DoubleProperty progress; // 0 or negative => 0%, 1 or greater => 100%

    private MCVolume data;
    private float level;
    private int gridSize;
    private Type type;
    private Consumer<Mesh> meshConsumer; // will be called with the current mesh after every mesh update
    private Consumer<Long> onFinish;

    private volatile boolean pausing; // whether this MCRunner stops after every mesh update
    private volatile boolean paused; // whether this MCRunner was paused

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
     *         the level for the Marching Cubes algorithm
     *
     * @throws NullPointerException
     *         if <code>data</code> or <code>type</code> is <code>null</code>
     * @throws IllegalArgumentException
     *         if <code>level</code> is smaller than 0 or <code>gridSize</code> is smaller than 1
     */
    public MCRunner(MCVolume data, float level) {
        this(data, level, 1, COMPLETE);
    }

    /**
     * Constructs a new <code>MCRunner</code> that performs the Marching Cubes algorithm over the given data.
     *
     * @param data
     *         the data for the Marching Cubes algorithm
     * @param level
     *         the level for the Marching Cubes algorithm
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
    public MCRunner(MCVolume data, float level, int gridSize, Type type) {
        Objects.requireNonNull(data, "data must not be null!");

        if (!(level >= 0)) {
            throw new IllegalArgumentException("level must be greater or equal to 0!");
        }

        if (!(gridSize >= 1)) {
            throw new IllegalArgumentException("gridSize must be greater or equal to 1!");
        }

        Objects.requireNonNull(type, "type must not be null!");

        progress = new SimpleDoubleProperty(0);

        this.data = data;
        this.level = level;
        this.gridSize = gridSize;
        this.type = type;

        this.pausing = false;
        this.paused = false;
        this.numLastTriangles = 0;

        int capacity = 100000;
        this.points = new LinkedHashMap<>(capacity);
        this.indices = new ArrayList<>(capacity);
        this.normals = new ArrayList<>(capacity);
        this.edgeCache = new HashMap<>(capacity);
    }

    /**
     * Returns the x size of the underlying volume.
     *
     * @return the size in x
     */
    public int getXSize() {
        return data.xSize();
    }

    /**
     * Returns the y size of the underlying volume.
     *
     * @return the size in y
     */
    public int getYSize() {
        return data.ySize();
    }

    /**
     * Returns the z size of the underlying volume.
     *
     * @return the size in z
     */
    public int getZSize() {
        return data.zSize();
    }

    /**
     * Returns the progress property of this <code>MCRunner</code>. Will have a value between 0 - 1 indicating
     * 0% to 100% done.
     *
     * @return the progress property
     */
    public DoubleProperty progressProperty() {
        return progress;
    }

    /**
     * Returns whether this <code>MCRunner</code> is pausing after every mesh update.
     * The default is <code>false</code>.
     *
     * @return true iff the <code>MCRunner</code> is pausing after every mesh update
     */
    public boolean isPausing() {
        return pausing;
    }

    /**
     * Sets whether this <code>MCRunner</code> is pausing after every mesh update.
     * The default is <code>false</code>.
     *
     * @param pausing
     *         whether this <code>MCRunner</code> is pausing after every mesh update
     */
    public void setPausing(boolean pausing) {
        this.pausing = pausing;
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

    /**
     * Sets the method that will be called after the Marching Cubes algorithm is finished.
     * The <code>Consumer</code> will be supplied with a <code>Long</code> representing the time in milliseconds
     * the execution took.
     *
     * @param onFinish the method to be called after the MC algorithm is finished
     */
    public void setOnRunFinished(Consumer<Long> onFinish) {
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        int cubesInSlice = data.xSize() * data.ySize() / gridSize * gridSize;
        int numCubes = (data.zSize() * cubesInSlice) / gridSize;
        Cube cube = new Cube();
        int doneCubes = 0;
        int cubeIndex;

        progress.set(0);

        for (int z = 0; z < data.zSize() - gridSize; z += gridSize) {
            for (int y = 0; y < data.ySize() - gridSize; y += gridSize) {
                for (int x = 0; x < data.xSize() - gridSize; x += gridSize) {

                    computeVertices(x, y, z, cube);
                    cubeIndex = cube.getIndex(level);

                    if ((cubeIndex != 0) && (cubeIndex != 255)) {
                        computeEdges(x, y, z, cube, cubeIndex);
                        updateMesh(cube, cubeIndex);
                    }

                    if (type == CUBE) {
                        outputMesh();
                    }

                    if (Thread.interrupted()) {
                        return;
                    }

                    if (paused) {
                        synchronized (this) {

                            while (paused) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            if (type == SLICE) {
                outputMesh();
            }

            cleanCache(z);

            progress.set((doneCubes += cubesInSlice) / (float) numCubes);
        }

        if (type == COMPLETE) {
            outputMesh();
        }

        if (onFinish != null) {
            onFinish.accept(System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Removes mappings from the <code>edgeCache</code> that will no be used by the algorithm again.
     *
     * @param z
     *         the layer that the algorithm finished
     */
    private void cleanCache(int z) {
        List<Long> toRemove = new LinkedList<>();

        edgeCache.forEach((id, vertex) -> {
            int cubeZ = (int) (id & 0xffffff) >>> 4; // extract z from the id
            int cubeIndex = (int) (id & 0xf); // extract the index from the id

            if (cubeZ == z - 1 || !Cube.topIndices.contains(cubeIndex)) {
                toRemove.add(id);
            }
        });

        toRemove.forEach(edgeCache::remove);
    }

    /**
     * Packs the given integers into a <code>long</code> to be used as a key into <code>edgeCache</code>.
     * <code>x</code>, <code>y</code>, and <code>z</code> will receive 20 bit each, <code>index</code> will receive
     * 4 bit. The <code>long</code> will have the format [x,y,z,index].
     *
     * @param x
     *         the x coordinate of the cubes vertex 0
     * @param y
     *         the y coordinate of the cubes vertex 0
     * @param z
     *         the z coordinate of the cubes vertex 0
     * @param index
     *         the index of the edge
     *
     * @return a <code>long</code> of the specified format
     */
    private long packLong(int x, int y, int z, int index) {
        long edgeID = 0;

        edgeID |= x;
        edgeID = edgeID << 20;
        edgeID |= y;
        edgeID = edgeID << 20;
        edgeID |= z;
        edgeID = edgeID << 4;
        edgeID |= index;

        return edgeID;
    }

    /**
     * Converts the <code>points</code>, <code>normals</code> and <code>indices</code> into a <code>Mesh</code> and
     * feeds the <code>meshConsumer</code> with it. If no new triangles were created or the consumer is
     * <code>null</code> no update will be performed. If the type is not <code>COMPLETE</code> (in which case this
     * method is called only once) and this <code>MCRunner</code> is pausing this method pauses the run.
     */
    private void outputMesh() {

        if (this.indices.size() <= numLastTriangles) {
            return;
        } else {
            numLastTriangles = this.indices.size();
        }

        if (meshConsumer != null) {
            FloatBuffer points = Buffers.allocateFloatBuffer(this.points.size() * 3 + this.normals.size() * 3);
            FloatBuffer normals = Buffers.allocateFloatBuffer(this.normals.size() * 3);
            IntBuffer indices = Buffers.allocateIntBuffer(this.indices.size());

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

                points.put(normalLinePoint.getX());
                points.put(normalLinePoint.getY());
                points.put(normalLinePoint.getZ());
            }
            this.indices.forEach(indices::put);

            points.flip();
            normals.flip();
            indices.flip();

            meshConsumer.accept(new Mesh(points, normals, indices));
        }

        if (type == COMPLETE) {
            return;
        }

        if (pausing) {
            pauseRun();
        }
    }

    /**
     * Computes the locations, values and gradients of the corner vertices of the cube whose vertex 0 is at the given
     * position in the <code>data</code>
     *
     * @param x the x coordinate of the cubes vertex 0
     * @param y the y coordinate of the cubes vertex 0
     * @param z the z coordinate of the cubes vertex 0
     * @param cube the cube whose vertices are to be computed
     */
    private void computeVertices(int x, int y, int z, Cube cube) {
        CornerVertex v;

        v = cube.getVertex(0);
        v.setLocation(x, y, z);
        v.setValue(data.value(x, y, z));

        v = cube.getVertex(1);
        v.setLocation(x + gridSize, y, z);
        v.setValue(data.value(x + gridSize, y, z));

        v = cube.getVertex(2);
        v.setLocation(x + gridSize, y + gridSize, z);
        v.setValue(data.value(x + gridSize, y + gridSize, z));

        v = cube.getVertex(3);
        v.setLocation(x, y + gridSize, z);
        v.setValue(data.value(x, y + gridSize, z));

        v = cube.getVertex(4);
        v.setLocation(x, y, z + gridSize);
        v.setValue(data.value(x, y, z + gridSize));

        v = cube.getVertex(5);
        v.setLocation(x + gridSize, y, z + gridSize);
        v.setValue(data.value(x + gridSize, y, z + gridSize));

        v = cube.getVertex(6);
        v.setLocation(x + gridSize, y + gridSize, z + gridSize);
        v.setValue(data.value(x + gridSize, y + gridSize, z + gridSize));

        v = cube.getVertex(7);
        v.setLocation(x, y + gridSize, z + gridSize);
        v.setValue(data.value(x, y + gridSize, z + gridSize));

        for (int i = 0; i < 8; i++) {
            computeGradient(cube.getVertex(i));
        }
    }

    /**
     * Computes the gradient (using central differences) of the <code>WeightedVertex</code> <code>v</code> located
     * at the given position.
     *
     * @param v
     *         the vertex
     */
    private void computeGradient(CornerVertex v) {
        int x = (int) v.getLocation().getX();
        int y = (int) v.getLocation().getY();
        int z = (int) v.getLocation().getZ();

        float gX = data.value(x - gridSize, y, z) - data.value(x + gridSize, y, z);
        float gY = data.value(x, y - gridSize, z) - data.value(x, y + gridSize, z);
        float gZ = data.value(x, y, z - gridSize) - data.value(x, y, z + gridSize);

        gX /= gridSize;
        gY /= gridSize;
        gZ /= gridSize;

        v.setNormal(gX, gY, gZ);
    }

    /**
     * Computes the position and normal of all appropriate triangle vertices (that lie on the edges of the cube).
     *
     * @param x
     *         the x coordinate of the cubes vertex 0
     * @param y
     *         the y coordinate of the cubes vertex 0
     * @param z
     *         the z coordinate of the cubes vertex 0
     * @param cube
     *         the cube whose edges are to be computed
     * @param cubeIndex
     *         the index of the cube (see {@link Cube#getIndex(float)})
     */
    private void computeEdges(int x, int y, int z, Cube cube, int cubeIndex) {
        int edgeIndex = Tables.getEdgeIndex(cubeIndex);

        if ((edgeIndex & 1) == 1) { // Edge 0
            Vertex edge = null;

            if (y != 0) {
                edge = edgeCache.get(packLong(x, y - gridSize, z, 2));
            } else if (z != 0) {
                edge = edgeCache.get(packLong(x, y, z - gridSize, 4));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(0), cube.getVertex(1));
                edgeCache.put(packLong(x, y, z, 0), edge);
            }
            cube.setEdge(0, edge);
        }

        if ((edgeIndex & 2) == 2) { // Edge 1
            Vertex edge = null;

            if (z != 0) {
                edge = edgeCache.get(packLong(x, y, z - gridSize, 5));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(1), cube.getVertex(2));
                edgeCache.put(packLong(x, y, z, 1), edge);
            }
            cube.setEdge(1, edge);
        }

        if ((edgeIndex & 4) == 4) { // Edge 2
            Vertex edge = null;

            if (z != 0) {
                edge = edgeCache.get(packLong(x, y, z - gridSize, 6));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(2), cube.getVertex(3));
                edgeCache.put(packLong(x, y, z, 2), edge);
            }
            cube.setEdge(2, edge);
        }

        if ((edgeIndex & 8) == 8) { // Edge 3
            Vertex edge = null;

            if (x != 0) {
                edge = edgeCache.get(packLong(x - gridSize, y, z, 1));
            } else if (z != 0) {
                edge = edgeCache.get(packLong(x, y, z - gridSize, 7));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(3), cube.getVertex(0));
                edgeCache.put(packLong(x, y, z, 3), edge);
            }
            cube.setEdge(3, edge);
        }

        if ((edgeIndex & 16) == 16) { // Edge 4
            Vertex edge = null;

            if (y != 0) {
                edge = edgeCache.get(packLong(x, y - gridSize, z, 6));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(4), cube.getVertex(5));
                edgeCache.put(packLong(x, y, z, 4), edge);
            }
            cube.setEdge(4, edge);
        }

        if ((edgeIndex & 32) == 32) { // Edge 5
            Vertex edge;

            edge = interpolate(cube.getVertex(5), cube.getVertex(6));
            edgeCache.put(packLong(x, y, z, 5), edge);
            cube.setEdge(5, edge);
        }

        if ((edgeIndex & 64) == 64) { // Edge 6
            Vertex edge;

            edge = interpolate(cube.getVertex(6), cube.getVertex(7));
            edgeCache.put(packLong(x, y, z, 6), edge);
            cube.setEdge(6, edge);
        }

        if ((edgeIndex & 128) == 128) { // Edge 7
            Vertex edge = null;

            if (x != 0) {
                edge = edgeCache.get(packLong(x - gridSize, y, z, 5));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(7), cube.getVertex(4));
                edgeCache.put(packLong(x, y, z, 7), edge);
            }
            cube.setEdge(7, edge);
        }

        if ((edgeIndex & 256) == 256) { // Edge 8
            Vertex edge = null;

            if (x != 0) {
                edge = edgeCache.get(packLong(x - gridSize, y, z, 9));
            } else if (y != 0) {
                edge = edgeCache.get(packLong(x, y - gridSize, z, 11));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(4), cube.getVertex(0));
                edgeCache.put(packLong(x, y, z, 8), edge);
            }
            cube.setEdge(8, edge);
        }

        if ((edgeIndex & 512) == 512) { // Edge 9
            Vertex edge = null;

            if (y != 0) {
                edge = edgeCache.get(packLong(x, y - gridSize, z, 10));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(5), cube.getVertex(1));
                edgeCache.put(packLong(x, y, z, 9), edge);
            }
            cube.setEdge(9, edge);
        }

        if ((edgeIndex & 1024) == 1024) { // Edge 10
            Vertex edge;

            edge = interpolate(cube.getVertex(6), cube.getVertex(2));
            edgeCache.put(packLong(x, y, z, 10), edge);
            cube.setEdge(10, edge);
        }

        if ((edgeIndex & 2048) == 2048) { // Edge 11
            Vertex edge = null;

            if (x != 0) {
                edge = edgeCache.get(packLong(x - gridSize, y, z, 10));
            }

            if (edge == null) {
                edge = interpolate(cube.getVertex(7), cube.getVertex(3));
                edgeCache.put(packLong(x, y, z, 11), edge);
            }
            cube.setEdge(11, edge);
        }
    }

    /**
     * Linearly interpolates the position and normal of a <code>Vertex</code> that is assumed to lie on the edge between
     * <code>v1</code> and <code>v2</code>.
     *
     * @param v1
     *         the first vertex of a cube
     * @param v2
     *         the second vertex of a cube
     */
    private Vertex interpolate(CornerVertex v1, CornerVertex v2) {
        float edgeX, edgeY, edgeZ;
        float normalX, normalY, normalZ;
        double min = Math.pow(10, -4);
        double length;
        float alpha;
        Vertex edge = new Vertex(0, 0, 0);

        if (Math.abs(level - v1.getValue()) < min) {
            edge.setLocation(v1.getLocation());
            edge.setNormal(v1.getNormal().normalized());
            return edge;
        }

        if (Math.abs(level - v2.getValue()) < min) {
            edge.setLocation(v2.getLocation());
            edge.setNormal(v2.getNormal().normalized());
            return edge;
        }

        if (Math.abs(v1.getValue() - v2.getValue()) < min) {
            edge.setLocation(v1.getLocation());
            edge.setNormal(v1.getNormal().normalized());
            return edge;
        }

        alpha = (level - v2.getValue()) / (v1.getValue() - v2.getValue());

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

        return edge;
    }

    /**
     * Updates the <code>points</code>, <code>normals</code>, and <code>indices</code> with triangles constructed
     * from the edges of the given <code>Cube</code> according to
     * {@link de.uni_passau.fim.seibt.v8.model.mc_alg.Tables#getTriangleIndex(int)}.
     *
     * @param cube
     *         the cube with whose edges the mesh is to be updated
     * @param cubeIndex
     *         the index of the cube (see {@link de.uni_passau.fim.seibt.v8.model.mc_alg.Cube#getIndex(float)})
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
     * Pauses the execution of the Marching Cubes algorithm. No mesh update will be produced until after
     * {@link #continueRun()} is called.
     */
    public void pauseRun() {
        paused = true;
    }

    /**
     * Restarts the execution of the Marching Cubes algorithm.
     */
    public void continueRun() {
        synchronized (this) {
            paused = false;
            notify();
        }
    }
}
