package controller.ms_alg;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import controller.ms_alg.ms_volume.MSGrid;
import org.lwjgl.BufferUtils;

public class MSRunner implements Runnable {

    private static short[] edges = {
            0b0000, 0b1001, 0b0011, 0b1010, 0b0110, 0b1111, 0b0101, 0b1100, 0b1100, 0b0101, 0b1111, 0b0110, 0b1010,
            0b0011, 0b1001, 0b0000
    };

    private static short[][] lines = {
            {}, {0, 3}, {0, 1}, {1, 3}, {1, 2}, {0, 1, 2, 3}, {0, 2}, {2, 3}, {2, 3}, {0, 2}, {1, 2, 3, 0}, {1, 2},
            {1, 3}, {0, 1}, {0, 3}, {}
    };

    private final MSGrid data;
    private final float level;

    private Map<Vertex2D, Integer> vertices;
    private List<Integer> indices;

    private Consumer<Mesh2D> meshConsumer;

    public MSRunner(MSGrid data, float level) {
        this.data = data;
        this.level = level;
        this.vertices = new LinkedHashMap<>();
        this.indices = new ArrayList<>();
    }

    @Override
    public void run() {
        short squareIndex;
        Square square = new Square();

        for (int y = 0; y < data.ySize(); y++) {

            for (int x = 0; x < data.xSize(); x++) {

                computeVertices(x, y, square);
                squareIndex = square.getIndex(level);

                if ((squareIndex != 0) && (squareIndex != 15)) {
                    computeEdges(x, y, square, squareIndex);
                    updateMesh(square, squareIndex);
                }
            }
        }

        if (meshConsumer != null) {
            meshConsumer.accept(createMesh());
        }
    }

    private Mesh2D createMesh() {
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

    private void computeVertices(int x, int y, Square square) {
        DensityVertex2D v;

        v = square.getVertex(0);
        v.setXY(x, y);
        v.setDensity(data.density(x, y));

        v = square.getVertex(1);
        v.setXY(x + 1, y);
        v.setDensity(data.density(x + 1, y));

        v = square.getVertex(2);
        v.setXY(x + 1, y + 1);
        v.setDensity(data.density(x + 1, y + 1));

        v = square.getVertex(3);
        v.setXY(x, y + 1);
        v.setDensity(data.density(x, y + 1));
    }

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

    public void setMeshConsumer(Consumer<Mesh2D> meshConsumer) {
        this.meshConsumer = meshConsumer;
    }

    public MSGrid getData() {
        return data;
    }
}
