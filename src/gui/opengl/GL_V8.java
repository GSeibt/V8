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

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

public class GL_V8 {

    private final int DEFAULT_FOV = 70;

    private SynchronousQueue<Mesh> newBuffer = new SynchronousQueue<>();
    private MCRunner mcRunner;
    private Camera camera;

    private int vboId;  // Vertex Buffer Object ID (Points)
    private int vboiId; // Vertex Buffer Object ID (Indices)
    private int vbonId; // Vertex Buffer Object ID (Normals)
    private int indicesCount;

    public GL_V8(float[][][] data, float level) {
        try {
            initDisplay();
            initGL();
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

    private void initGL() {
        glClearColor(0.5f,0.5f,0.5f,1f);
        glClearDepth(1.0f);
    }

    private void initGLLight() {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

        IntBuffer position = BufferUtils.createIntBuffer(4);
        position.put(new int[] {1, 1, 1, 0}).flip();

        IntBuffer ambient = BufferUtils.createIntBuffer(4);
        ambient.put(new int[] {0, 0, 0, 1}).flip();

        IntBuffer diffuse = BufferUtils.createIntBuffer(4);
        diffuse.put(new int[] {1, 1, 1, 1}).flip();

        IntBuffer specular = BufferUtils.createIntBuffer(4);
        specular.put(new int[] {1, 1, 1, 1}).flip();

        glLight(GL_LIGHT0, GL_POSITION, position);
        glLight(GL_LIGHT0, GL_AMBIENT, ambient);
        glLight(GL_LIGHT0, GL_DIFFUSE, diffuse);
        glLight(GL_LIGHT0, GL_SPECULAR, specular);

//        FloatBuffer lModelAmbient = BufferUtils.createFloatBuffer(4);
//        lModelAmbient.put(new float[] {0.5f, 0.5f, 0.5f, 1}).flip();

//        glLightModel(GL_LIGHT_MODEL_AMBIENT, lModelAmbient);

        IntBuffer emission = BufferUtils.createIntBuffer(4);
        emission.put(new int[] {0, 0, 0, 1}).flip();

       glMaterial(GL_FRONT, GL_SPECULAR, specular);
//        glMaterialf(GL_FRONT, GL_SHININESS, 50f);
        glMaterial(GL_FRONT_AND_BACK, GL_EMISSION, emission);
    }

    private void initGLObjects() {

        // create a new Vertex Buffer Object for the vertexes
        vboId = glGenBuffersARB();
        glBindBufferARB(GL_ARRAY_BUFFER, vboId);

        // create a new Vertex Buffer Object for the normals
        vbonId = glGenBuffersARB();
        glBindBufferARB(GL_ARRAY_BUFFER, vbonId);

        // create a new Vertex Buffer Object for the indices
        vboiId = glGenBuffersARB();
        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, vboId);

        glBindBufferARB(GL_ARRAY_BUFFER, 0);
        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
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

            glBindBufferARB(GL_ARRAY_BUFFER, vboId);
            glBufferDataARB(GL_ARRAY_BUFFER, change.getVertexes(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ARRAY_BUFFER, vbonId);
            glBufferDataARB(GL_ARRAY_BUFFER, change.getNormals(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, vboiId);
            glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER, change.getIndices(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ARRAY_BUFFER, 0);
            glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
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

//        glColor3f(0.1f, 0.4f, 0.9f);
//        Sphere s = new Sphere();
//        s.draw(1f, 10, 10);

        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBufferARB(GL_ARRAY_BUFFER, vboId);
        glVertexPointer(3, GL_FLOAT, 0, 0);

        glEnableClientState(GL_NORMAL_ARRAY);
        glBindBufferARB(GL_ARRAY_BUFFER, vbonId);
        glNormalPointer(GL_FLOAT, 0, 0);

        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, vboiId);

        glColor3f(0, 1f, 0);
        glDrawElements(GL_TRIANGLES, indicesCount, GL_UNSIGNED_INT, 0);

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);

        glBindBufferARB(GL_ARRAY_BUFFER, 0);
        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void cleanup() {
        cleanupBuffers();
        Display.destroy();
        Keyboard.destroy();
        Mouse.destroy();
    }

    private void cleanupBuffers() {
        glDeleteBuffersARB(vboId);
        glDeleteBuffersARB(vboiId);
        glDeleteBuffersARB(vbonId);
    }

    private void receiveUpdate(Mesh mesh) {
        try { newBuffer.put(mesh); } catch (InterruptedException ignored) {}
    }
}
