package controller.ms_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.Callable;

import controller.ms_alg.ms_volume.MSGrid;
import org.lwjgl.BufferUtils;

/**
 * <code>Callable</code> that when executed performs the Marching Squares algorithm over the given data. Produces
 * a <code>Mesh2D</code> describing the resulting lines.
 */
public class MSRunner implements Callable<Mesh2D> {

    /**
     * Analogous to {@link controller.mc_alg.Tables#EDGES}.
     */
    private static short[] edges = {
            0b0000, 0b1001, 0b0011, 0b1010, 0b0110, 0b1111, 0b0101, 0b1100, 0b1100, 0b0101, 0b1111, 0b0110, 0b1010,
            0b0011, 0b1001, 0b0000
    };

    /**
     * Analogous to {@link controller.mc_alg.Tables#TRIANGLES}.
     */
    private static short[][] lines = {
            {}, {0, 3}, {0, 1}, {1, 3}, {1, 2}, {0, 1, 2, 3}, {0, 2}, {2, 3}, {2, 3}, {0, 2}, {1, 2, 3, 0}, {1, 2},
            {1, 3}, {0, 1}, {0, 3}, {}
    };

    private final MSGrid data;
    private final float level;
    private final int gridSize;

    private Map<Vertex2D, Integer> vertices;
    private List<Integer> indices;

    /**
     * Constructs a new <code>MSRunner</code> performing the Marching Squares algorithm over the given data.
     *
     * @param data
     *         the data for the Marching Squares algorithm
     * @param level
     *         the density level for the Marching Squares algorithm
     * @param gridSize
     *         the grid size (the x/y dimension of the squares)
     */
    public MSRunner(MSGrid data, float level, int gridSize) {
        this.data = data;
        this.level = level;
        this.gridSize = gridSize;
        this.vertices = new LinkedHashMap<>();
        this.indices = new ArrayList<>();
    }

    @Override
    public Mesh2D call() {
        short squareIndex;
        Square square = new Square();

        for (int y = 0; y < data.ySize(); y += gridSize) {

            for (int x = 0; x < data.xSize(); x += gridSize) {

                computeVertices(x, y, square);
                squareIndex = square.getIndex(level);

                if ((squareIndex != 0) && (squareIndex != 15)) {
                    computeEdges(x, y, square, squareIndex);
                    updateMesh(square, squareIndex);
                }
            }
        }

        FloatBuffer vertices = BufferUtils.createFloatBuffer(this.vertices.size() * 2);
        IntBuffer indices = BufferUtils.createIntBuffer(this.indices.size());
        Iterator<Map.Entry<Vertex2D, Integer>> vertexIt = this.vertices.entrySet().iterator();

        Vertex2D vertex2D;
        while (vertexIt.hasNext()) {
            vertex2D = vertexIt.next().getKey();

            vertices.put(vertex2D.getX());
            vertices.put(vertex2D.getY());
        }
        this.indices.forEach(indices::put);

        vertices.flip();
        indices.flip();

        return new Mesh2D(vertices, indices);
    }

    /**
     * Computes the vertices of the given <code>Square</code>.
     *
     * @param x
     *         the x position of the squares vertex 0
     * @param y
     *         the y position of the squares vertex 0
     * @param square
     *         the square
     */
    private void computeVertices(int x, int y, Square square) {
        DensityVertex2D v;

        v = square.getVertex(0);
        v.setXY(x, y);
        v.setDensity(data.density(x, y));

        v = square.getVertex(1);
        v.setXY(x + gridSize, y);
        v.setDensity(data.density(x + gridSize, y));

        v = square.getVertex(2);
        v.setXY(x + gridSize, y + gridSize);
        v.setDensity(data.density(x + gridSize, y + gridSize));

        v = square.getVertex(3);
        v.setXY(x, y + gridSize);
        v.setDensity(data.density(x, y + gridSize));
    }

    /**
     * Computes the positions of all appropriate line endpoints on the edges of the square.
     *
     * @param x
     *         the x position of the squares vertex 0
     * @param y
     *         the y position of the squares vertex 0
     * @param square
     *         the square
     * @param squareIndex
     *         the index of the square {@link Square#getIndex(float)}
     */
    private void computeEdges(int x, int y, Square square, short squareIndex) {
        short edgeIndex = edges[squareIndex];

        if ((edgeIndex & 1) == 1) {
            square.setEdge(0, interpolate(square.getVertex(0), square.getVertex(1)));
        }

        if ((edgeIndex & 2) == 2) {
            square.setEdge(1, interpolate(square.getVertex(1), square.getVertex(2)));
        }

        if ((edgeIndex & 4) == 4) {
            square.setEdge(2, interpolate(square.getVertex(2), square.getVertex(3)));
        }

        if ((edgeIndex & 8) == 8) {
            square.setEdge(3, interpolate(square.getVertex(3), square.getVertex(0)));
        }
    }

    /**
     * Updates the <code>points</code>, and <code>indices</code> with lines constructed
     * from the edges of the given <code>Square</code> according to the array from the <code>EDGES</code> table for
     * the <code>squareIndex</code>.
     *
     * @param square
     *         the square with whose lines the mesh should be updated
     * @param squareIndex
     *         the index of the square {@link Square#getIndex(float)}
     */
    private void updateMesh(Square square, short squareIndex) {
        Vertex2D edge;
        Integer index;
        int newIndex = vertices.size();
        short[] lineIndices = lines[squareIndex];

        for (int i = 0; i < lineIndices.length; i += 2) {

            for (int j = 0; j < 2; j++) {

                try {
                    edge = square.getEdge(lineIndices[i + j]).clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    return;
                }

                index = vertices.get(edge);

                if (index == null) {
                    vertices.put(edge, newIndex);
                    indices.add(newIndex);
                    newIndex++;
                } else {
                    indices.add(index);
                }
            }
        }
    }

    /**
     * Linearly interpolates the position of a <code>Vertex2D</code> that is assumed to lie on the edge between
     * <code>v1</code> and <code>v2</code>.
     *
     * @param v1
     *         the first vertex of a cube
     * @param v2
     *         the second vertex of a cube
     */
    private Vertex2D interpolate(DensityVertex2D v1, DensityVertex2D v2) {
        Vertex2D edge = new Vertex2D();
        double min = Math.pow(10, -4);

        if (Math.abs(level - v1.getDensity()) < min) {
            edge.setXY(v1.getX(), v1.getY());
            return edge;
        }

        if (Math.abs(level - v2.getDensity()) < min) {
            edge.setXY(v2.getX(), v2.getY());
            return edge;
        }

        if (Math.abs(v1.getDensity() - v2.getDensity()) < min) {
            edge.setXY(v1.getX(), v1.getY());
            return edge;
        }

        float alpha = (level - v2.getDensity()) / (v1.getDensity() - v2.getDensity());
        float edgeX = alpha * v1.getX() + (1 - alpha) * v2.getX();
        float edgeY = alpha * v1.getY() + (1 - alpha) * v2.getY();

        edge.setXY(edgeX, edgeY);

        return edge;
    }

    /**
     * Returns the <code>MSGrid</code> data the <code>MSRunner</code> is using.
     *
     * @return the <code>MSGrid</code> data
     */
    public MSGrid getData() {
        return data;
    }
}
