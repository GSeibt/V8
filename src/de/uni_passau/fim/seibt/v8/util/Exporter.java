package de.uni_passau.fim.seibt.v8.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Calendar;

import de.uni_passau.fim.seibt.v8.V8;
import de.uni_passau.fim.seibt.v8.model.mc_alg.Mesh;

/**
 * An exporter for the .obj and .stl file format.
 */
public class Exporter {

    /**
     * Saves the given <code>Mesh</code> as an .obj file.
     * If <code>saveFile</code> exists and is not a directory it will be overwritten.
     * Neither <code>mesh</code> nor <code>saveFile</code> may be <code>null</code>.
     *
     * @param mesh
     *         the <code>Mesh</code> to be exported
     * @param saveFile
     *         the <code>File</code> to save the .obj data to
     */
    public static void exportOBJ(Mesh mesh, File saveFile) {

        if (mesh == null || saveFile == null) {
            System.err.println("Neither mesh nor saveFile may be null, aborting mesh export.");
            return;
        }

        if (saveFile.isDirectory()) {
            System.err.println("saveFile must not be a directory, aborting mesh export.");
            return;
        }

        String vertex = "v";
        String vertexNormal = "vn";
        String vectorFormatString = "%s %s %s %s %n";
        String face = "f";
        String faceFormatString = "%s %2$d//%2$d %3$d//%3$d %4$d//%4$d %n";

        try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
            writer.write(String.format("# Created by %s on %tc %n%n", V8.class.getSimpleName(), Calendar.getInstance()));
            writer.write(String.format("o %s%n", getFileName(saveFile)));

            FloatBuffer verts = mesh.getVertices();
            while (verts.hasRemaining()) {
                writer.write(String.format(vectorFormatString, vertex, verts.get(), verts.get(), verts.get()));
                verts.position(verts.position() + 3); // skip the normal points
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

        if (mesh == null || saveFile == null) {
            System.err.println("Neither mesh nor saveFile may be null, aborting mesh export.");
            return;
        }

        if (saveFile.isDirectory()) {
            System.err.println("saveFile must not be a directory, aborting mesh export.");
            return;
        }

        int numFaces = mesh.getIndices().limit() / 3;

        // an int for the number of faces and for every face 12 floats and one short for the attribute byte count
        ByteBuffer outputBuffer = ByteBuffer.allocate(4 + numFaces * (12 * 4 + 2)).order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer indices = mesh.getIndices();
        FloatBuffer vertices = mesh.getVertices();
        Vector3f v1 = new Vector3f(0, 0, 0);
        Vector3f v2 = new Vector3f(0, 0, 0);
        Vector3f v3 = new Vector3f(0, 0, 0);
        Vector3f[] vectors = {v1, v2, v3};
        Vector3f normal;
        int index;

        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(saveFile)))) {
            byte[] header = new byte[80];
            byte[] headerString = String.format("Created by %s on %tc", V8.class.getSimpleName(), Calendar.getInstance()).getBytes();
            System.arraycopy(headerString, 0, header, 0, headerString.length);

            output.write(header);
            outputBuffer.putInt(numFaces);

            while (indices.hasRemaining()) {

                for (int i = 0; i < 3; i++) {
                    index = indices.get() * 6; // *6 instead of *3 because we have to skip the normal points
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

    /**
     * Returns the name (without the extension) of the given file.
     * If the filename is only an extension (e.g. '.txt') the original name will be returned.
     *
     * @param file
     *         the file whose name is to be extracted
     *
     * @return the filename
     */
    private static String getFileName(File file) {
        String fileName = file.getName();
        int dotPos = fileName.lastIndexOf('.');

        if (dotPos > 0) {
            fileName = fileName.substring(0, dotPos);
        }

        return fileName;
    }
}
