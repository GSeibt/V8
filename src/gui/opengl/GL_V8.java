package gui.opengl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.SynchronousQueue;

import controller.mc_alg.MCRunner;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11.*;

public class GL_V8 {

    private class MeshChange {

        private FloatBuffer vertexes;

        private IntBuffer indices;
        private MeshChange(FloatBuffer vertexes, IntBuffer indices) {
            this.vertexes = vertexes;
            this.indices = indices;
        }

    }
    private final int DEFAULT_FOV = 70;
    private SynchronousQueue<MeshChange> newBuffer = new SynchronousQueue<>();
    private MCRunner mcRunner;
    private Camera camera;
    private int vaoId;
    private int vboId;
    private int vboiId;
    private int indicesCount;

    public GL_V8(float[][][] data, float level) {
        try {
            initDisplay();
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

    private void initGLObjects() {

        // create a new Vertex Array Object in memory and select it (bind)
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // create a new Vertex Buffer Object in memory and select it (bind)
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

        // put the VBO in the attributes list at index 0
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        // Create a new VBO for the indices
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
        MeshChange change = newBuffer.poll();

        if (change != null) {
            indicesCount = change.indices.limit();

            GL30.glBindVertexArray(vaoId);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, change.vertexes, GL15.GL_STATIC_DRAW);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, change.indices, GL15.GL_STATIC_DRAW);

            // Deselect (bind to 0) the VBO
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
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
        GL20.glEnableVertexAttribArray(0);

        // Bind to the index VBO that has all the information about the order of the vertices
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

        // Draw the vertices
        GL11.glColor3f(1f, 0, 0);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_INT, 0);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        // Put everything back to default (deselect)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glDisableVertexAttribArray(0);
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

    private void receiveUpdate(FloatBuffer vertexes, IntBuffer indices) {

        try {
            newBuffer.put(new MeshChange(vertexes, indices));
        } catch (InterruptedException ignored) {
        }
    }
}
