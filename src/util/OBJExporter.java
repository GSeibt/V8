package util;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Calendar;

import controller.mc_alg.Mesh;
import gui.V8;

/**
 * An exporter for the .obj file format.
 */
public class OBJExporter {

    /**
     * Saves the given <code>Mesh</code> as an .obj file.
     * If <code>saveFile</code> exists and is not a directory it will be overwritten.
     * Neither <code>mesh</code> nor <code>saveFile</code> may be <code>null</code>.
     *
     * @param mesh the <code>Mesh</code> to be exported
     * @param saveFile the <code>File</code> to save the .obj data to
     */
    public static void export(Mesh mesh, File saveFile) {

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
     * Returns the name (without the extension) of the given file.
     * If the filename is only an extension (e.g. '.txt') the original name will be returned.
     *
     * @param file the file whose name is to be extracted
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
