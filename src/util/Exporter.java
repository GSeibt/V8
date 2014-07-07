package util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Calendar;

import controller.mc_alg.Mesh;

/**
 * An exporter for the .obj and .stl file format.
 */
public class Exporter {

    /**
     * Saves the given <code>Mesh</code> as an .obj file.
     *
     * @param mesh the <code>Mesh</code> to be exported
     * @param saveFile the <code>File</code> to save the .obj data to
     */
    public static void exportOBJ(Mesh mesh, File saveFile) {
        String vertex = "v";
        String vertexNormal = "vn";
        String vectorFormatString = "%s %s %s %s %n";
        String face = "f";
        String faceFormatString = "%s %2$d//%2$d %3$d//%3$d %4$d//%4$d %n";

        try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {

            FloatBuffer verts = mesh.getVertices();
            while (verts.hasRemaining()) {
                writer.write(String.format(vectorFormatString, vertex, verts.get(), verts.get(), verts.get()));
                verts.position(verts.position() + 3); // skip the normal point
            }

            writer.write(String.format("%n"));

            FloatBuffer norms = mesh.getNormals();
            while (norms.hasRemaining()) {
                writer.write(String.format(vectorFormatString, vertexNormal, norms.get(), norms.get(), norms.get()));
            }

            writer.write(String.format("%n"));

            IntBuffer ind = mesh.getIndices();
            while (ind.hasRemaining()) {

                // vertices and normals are 1 indexed in .obj so we have to add 1 to every index...
                writer.write(String.format(faceFormatString, face, ind.get() + 1, ind.get() + 1, ind.get() + 1));
            }
        } catch (IOException e) {
            System.err.println("Could not write the " + saveFile.getName() + " .obj file. " + e.getMessage());
        }
    }

    /**
     * Saves the given <code>Mesh</code> as a binary .stl file.
     *
     * @param mesh the <code>Mesh</code> to be exported
     * @param saveFile the <code>File</code> to save the .stl data to
     */
    public static void exportSTL(Mesh mesh, File saveFile) {
        int numFaces = mesh.getIndices().limit() / 3;
        IntBuffer indices = mesh.getIndices();
        FloatBuffer vertices = mesh.getVertices();
        ByteBuffer outputBuffer = ByteBuffer.allocate(4 + numFaces * (12 * 4 + 2)).order(ByteOrder.LITTLE_ENDIAN);
        Vector3f v1 = new Vector3f(0, 0, 0);
        Vector3f v2 = new Vector3f(0, 0, 0);
        Vector3f v3 = new Vector3f(0, 0, 0);
        Vector3f[] vectors = {v1, v2, v3};
        Vector3f normal;
        int index;

        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(saveFile)))) {
            byte[] header = new byte[80];
            byte[] headerString = String.format("Exported by V8 on %td", Calendar.getInstance()).getBytes();
            System.arraycopy(headerString, 0, header, 0, headerString.length);

            output.write(header);
            outputBuffer.putInt(numFaces);

            while (indices.hasRemaining()) {

                for (int i = 0; i < 3; i++) {
                    index = indices.get() * 6;
                    vectors[i].setXYZ(vertices.get(index), vertices.get(index + 1), vertices.get(index + 2));
                }

                normal = (v2.sub(v1)).cross(v3.sub(v1)).normalized();

                // the normal vector
                outputBuffer.putFloat(normal.getX());
                outputBuffer.putFloat(normal.getY());
                outputBuffer.putFloat(normal.getZ());

                // the vertices
                outputBuffer.putFloat(vectors[0].getX());
                outputBuffer.putFloat(vectors[0].getY());
                outputBuffer.putFloat(vectors[0].getZ());

                outputBuffer.putFloat(vectors[1].getX());
                outputBuffer.putFloat(vectors[1].getY());
                outputBuffer.putFloat(vectors[1].getZ());

                outputBuffer.putFloat(vectors[2].getX());
                outputBuffer.putFloat(vectors[2].getY());
                outputBuffer.putFloat(vectors[2].getZ());

                // the attribute byte count
                outputBuffer.putShort((short) 0);
            }

            output.write(outputBuffer.array());
        } catch (IOException e) {
            System.err.println("Could not write the " + saveFile.getName() + " .stl file. " + e.getMessage());
        }
    }
}
