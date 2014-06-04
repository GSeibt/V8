package gui.opengl;

import java.nio.IntBuffer;
import java.util.concurrent.SynchronousQueue;

import controller.mc_alg.MCRunner;
import controller.mc_alg.Mesh;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;

public class GL_V8 {

    private final int DEFAULT_FOV = 70;

    private SynchronousQueue<Mesh> newBuffer = new SynchronousQueue<>();
    private MCRunner mcRunner;
    private Camera camera;

    private int vaoId;  // Vertex Array Object ID
    private int vboId;  // Vertex Buffer Object ID (Points)
    private int vboiId; // Vertex Buffer Object ID (Indices)
    private int vbonId; // Vertex Buffer Object ID (Normals)
    private int indicesCount;

    public GL_V8(float[][][] data, float level) {
        try {
            initDisplay();
            initGLLight();
            initGLObjects();
            initInput();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        float aspect = (float) Display.getWidth() / (float) Display.getHeight();
        this.camera = new Camera(DEFAULT_FOV, aspect, 0.1f, 10000);
        this.mcRunner = new MCRunner(data, level, MCRunner.Type.SLICE, this::receiveUpdate);
    }

    private void initGLLight() {
        glEnable(GL_LIGHTING);
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

        glEnable(GL_LIGHT0);

        IntBuffer position = BufferUtils.createIntBuffer(4);
        position.put(new int[] {1, 1, 0, 0}).flip();

        IntBuffer ambient = BufferUtils.createIntBuffer(4);
        ambient.put(new int[] {0,0,0,1}).flip();

        IntBuffer diffuse = BufferUtils.createIntBuffer(4);
        diffuse.put(new int[] {1,1,1,1}).flip();

        IntBuffer specular = BufferUtils.createIntBuffer(4);
        specular.put(new int[] {1,1,1,1}).flip();

        glLight(GL_LIGHT0, GL_POSITION, position);
        glLight(GL_LIGHT0, GL_AMBIENT, ambient);
        glLight(GL_LIGHT0, GL_DIFFUSE, diffuse);
        glLight(GL_LIGHT0, GL_SPECULAR, specular);

        IntBuffer emission = BufferUtils.createIntBuffer(4);
        emission.put(new int[] {0, 0, 0, 1}).flip();

        glMaterial(GL_FRONT_AND_BACK, GL_SPECULAR, specular);
        glMaterial(GL_FRONT_AND_BACK, GL_EMISSION, emission);
    }

    private void initGLObjects() {

        // create a new Vertex Array Object in memory and select it
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        // create a new Vertex Buffer Object for the vertexes
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

        // put the Vertex Buffer Object in the attributes list at index 0
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        // create a new Vertex Buffer Object for the normals
        vbonId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbonId);

        // put the Vertex Buffer Object in the attributes list at index 1
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        // create a new Vertex Buffer Object for the indices
        vboiId = GL15.glGenBuffers();
    }

    private void initDisplay() throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(800, 600));
        Display.setTitle("V8");
        Display.create();
    }

    private void initInput() throws LWJGLException {
        Keyboard.create();
        Mouse.create();
    }

    public void show() {
        Thread runner = new Thread(mcRunner);
        runner.setName("MCRunner");

        runner.start();
        while (!Display.isCloseRequested()) {
            useUpdate();
            input();
            draw();
            Display.update();
            Display.sync(30);
        }

        runner.interrupt();
        cleanup();
    }

    private void useUpdate() {
        Mesh change = newBuffer.poll();

        if (change != null) {
            indicesCount = change.getIndices().limit();

            GL30.glBindVertexArray(vaoId);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, change.getVertexes(), GL15.GL_STATIC_DRAW);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, change.getIndices(), GL15.GL_STATIC_DRAW);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbonId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, change.getNormals(), GL15.GL_STATIC_DRAW);

            // Deselect (bind to 0) the VBO
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL30.glBindVertexArray(0);
        }

        mcRunner.continueRun();
    }

    private void input() {
        camera.input();
    }

    private void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        camera.useView();

        // Bind to the VAO that has all the information about the vertices
        GL30.glBindVertexArray(vaoId);

        // Bind to the index VBO that has all the information about the order of the vertices
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

        // Draw the vertices
        GL11.glColor3f(1f, 0, 0);
        GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_INT, 0);

        // Put everything back to default (deselect)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    private void cleanup() {
        cleanupBuffers();

        Display.destroy();
        Keyboard.destroy();
        Mouse.destroy();
    }

    private void cleanupBuffers() {

        // Disable the VBO index from the VAO attributes list
        GL20.glDisableVertexAttribArray(0);

        // Delete the vertex VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);

        // Delete the index VBO
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboiId);

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }

    private void receiveUpdate(Mesh mesh) {

        try {
            newBuffer.put(mesh);
        } catch (InterruptedException ignored) {
        }
    }
}
