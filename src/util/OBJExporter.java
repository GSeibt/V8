package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import controller.mc_alg.Mesh;

/**
 * An exporter for the .obj file format.
 */
public class OBJExporter {

    /**
     * Saves the given <code>Mesh</code> as an .obj file.
     *
     * @param mesh the <code>Mesh</code> to be exported
     * @param saveFile the <code>File</code> to save the .obj data to
     */
    public static void export(Mesh mesh, File saveFile) {
        String vertex = "v";
        String vertexNormal = "vn";
        String vertexFormatString = "%s %s %s %s %n";
        String face = "f";
        String faceFormatString = "%s %2$d//%2$d %3$d//%3$d %4$d//%4$d %n";

        try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {

            FloatBuffer verts = mesh.getVertices();
            while (verts.hasRemaining()) {
                writer.write(String.format(vertexFormatString, vertex, verts.get(), verts.get(), verts.get()));
            }

            writer.write(String.format("%n"));

            FloatBuffer norms = mesh.getNormals();
            while (norms.hasRemaining()) {
                writer.write(String.format(vertexFormatString, vertexNormal, norms.get(), norms.get(), norms.get()));
            }

            writer.write(String.format("%n"));

            IntBuffer ind = mesh.getIndices();
            while (ind.hasRemaining()) {

                // vertexes and normals are 1 indexed in .obj so we have to add 1 to every index...
                writer.write(String.format(faceFormatString, face, ind.get() + 1, ind.get() + 1, ind.get() + 1));
            }
        } catch (IOException e) {
            System.err.println("Could not write the " + saveFile.getName() + " .obj file. "+ e.getMessage());
        }
    }
}
