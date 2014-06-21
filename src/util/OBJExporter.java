package util;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import controller.mc_alg.Mesh;

public class OBJExporter {

    public static void export(Mesh mesh, File saveFile) {
        String vertex = "v";
        String vertexNormal = "vn";
        String face = "f";

        try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {

            FloatBuffer vert = mesh.getVertices();
            while (vert.hasRemaining()) {
                writer.write(String.format("%s %s %s %s %n", vertex, vert.get(), vert.get(), vert.get()));
            }

            writer.write(String.format("%n"));

            FloatBuffer norm = mesh.getNormals();
            while (norm.hasRemaining()) {
                writer.write(String.format("%s %s %s %s %n", vertexNormal, norm.get(), norm.get(), norm.get()));
            }

            writer.write(String.format("%n"));

            IntBuffer ind = mesh.getIndices();
            while (ind.hasRemaining()) {

                // vertexes and normals are 1 indexed in .obj so we have to add 1 to every index...
                writer.write(String.format("%s %2$d//%2$d %3$d//%3$d %4$d//%4$d %n", face, ind.get() + 1, ind.get() + 1, ind.get() + 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
