package gui.opengl;

import java.nio.FloatBuffer;
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
import static org.lwjgl.util.glu.GLU.gluPerspective;

public class GL_V8 {

    private final int DEFAULT_FOV = 70;
    private FloatBuffer lightPosition;
    private SynchronousQueue<Mesh> newBuffer;
    private MCRunner mcRunner;
    private Camera camera;
    private int vboId;   // Vertex Buffer Object ID (Points)
    private int vboiId;  // Vertex Buffer Object ID (Indices)
    private int vbonId;  // Vertex Buffer Object ID (Normals)
    private int vbonlId; // Vertex Buffer Object ID (Normal Lines)
    private int indicesCount;
    private boolean showNormalLines;
    private int normalLinesCount;

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

        this.camera = new Camera();
        this.mcRunner = new MCRunner(data, level, MCRunner.Type.SLICE, this::receiveUpdate);
        this.lightPosition = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[] {1, 1, 0, 1}).flip();
        this.newBuffer = new SynchronousQueue<>();
        this.showNormalLines = false;
    }

    private void initGL() {
        glMatrixMode(GL_MODELVIEW);

        glClearColor(0.5f, 0.5f, 0.5f, 1);
        glClearDepth(1);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glMatrixMode(GL_PROJECTION);

        float aspectRatio = Display.getWidth() / (float) Display.getHeight();
        float nearClip = 0.1f;
        float farClip = 10000;
        gluPerspective(DEFAULT_FOV, aspectRatio, nearClip, farClip);

        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }

    private void initGLLight() {
        glMatrixMode(GL_MODELVIEW);
        glShadeModel(GL_SMOOTH);

        FloatBuffer matSpecular = BufferUtils.createFloatBuffer(4);
        matSpecular.put(new float[] {1, 1, 1, 1}).flip();

        glMaterial(GL_FRONT_AND_BACK, GL_SPECULAR, matSpecular);

        FloatBuffer matEmission = BufferUtils.createFloatBuffer(4);
        matEmission.put(new float[] {0, 0, 0, 1}).flip();

        glMaterial(GL_FRONT_AND_BACK, GL_EMISSION, matEmission);

        glMaterialf(GL_FRONT, GL_SHININESS, 20f);

        FloatBuffer ambient = BufferUtils.createFloatBuffer(4);
        ambient.put(new float[] {0, 0, 0, 1}).flip();

        glLight(GL_LIGHT0, GL_AMBIENT, ambient);

        FloatBuffer diffuse = BufferUtils.createFloatBuffer(4);
        diffuse.put(new float[] {1, 1, 1, 1}).flip();

        glLight(GL_LIGHT0, GL_DIFFUSE, diffuse);

        FloatBuffer specular = BufferUtils.createFloatBuffer(4);
        specular.put(new float[] {1, 1, 1, 1}).flip();

        glLight(GL_LIGHT0, GL_SPECULAR, specular);

        FloatBuffer modelAmbient = BufferUtils.createFloatBuffer(4);
        modelAmbient.put(new float[] {0.2f, 0.2f, 0.2f, 1.0f}).flip();

        glLightModel(GL_LIGHT_MODEL_AMBIENT, modelAmbient);

        glEnable(GL_LIGHT0);
        glEnable(GL_LIGHTING);

        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
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

        // create a new Vertex Buffer Object for the normal lines
        vbonlId = glGenBuffersARB();
        glBindBufferARB(GL_ARRAY_BUFFER, vbonlId);

        glBindBufferARB(GL_ARRAY_BUFFER, 0);
        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void initDisplay() throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(1024, 786));
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

    private void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        camera.useView();

        glLight(GL_LIGHT0, GL_POSITION, lightPosition);

        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBufferARB(GL_ARRAY_BUFFER, vboId);
        glVertexPointer(3, GL_FLOAT, 0, 0);

        glEnableClientState(GL_NORMAL_ARRAY);
        glBindBufferARB(GL_ARRAY_BUFFER, vbonId);
        glNormalPointer(GL_FLOAT, 0, 0);

        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, vboiId);

        glColor3f(1f, 0, 0);
        glDrawElements(GL_TRIANGLES, indicesCount, GL_UNSIGNED_INT, 0);

        if (showNormalLines) {
            boolean lighting = glIsEnabled(GL_LIGHTING);

            glDisable(GL_LIGHTING);

            glEnableClientState(GL_VERTEX_ARRAY);
            glBindBufferARB(GL_ARRAY_BUFFER, vbonlId);
            glVertexPointer(3, GL_FLOAT, 0, 0);

            glEnableClientState(GL_NORMAL_ARRAY);
            glBindBufferARB(GL_ARRAY_BUFFER, 0);

            glColor3f(0, 0, 1f);
            glDrawArrays(GL_LINES, 0, normalLinesCount);

            if (lighting) {
                glEnable(GL_LIGHTING);
            }
        }

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);

        glBindBufferARB(GL_ARRAY_BUFFER, 0);
        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void useUpdate() {
        Mesh change = newBuffer.poll();

        if (change != null) {
            indicesCount = change.getIndices().limit();
            normalLinesCount = change.getNormalLines().limit() / 2;

            glBindBufferARB(GL_ARRAY_BUFFER, vboId);
            glBufferDataARB(GL_ARRAY_BUFFER, change.getVertexes(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ARRAY_BUFFER, vbonId);
            glBufferDataARB(GL_ARRAY_BUFFER, change.getNormals(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, vboiId);
            glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER, change.getIndices(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ARRAY_BUFFER, vbonlId);
            glBufferDataARB(GL_ARRAY_BUFFER, change.getNormalLines(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ARRAY_BUFFER, 0);
            glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        mcRunner.continueRun();
    }

    private void input() {
        camera.input();

        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_G) {
                if (glGetInteger(GL_POLYGON_MODE) == GL_LINE) {
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                } else {
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                }
            }

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_L) {
                if (glIsEnabled(GL_LIGHTING)) {
                    glDisable(GL_LIGHTING);
                } else {
                    glEnable(GL_LIGHTING);
                }
            }

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_C) {
                if (glIsEnabled(GL_CULL_FACE)) {
                    glDisable(GL_CULL_FACE);
                } else {
                    glEnable(GL_CULL_FACE);
                }
            }

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_N) {
                showNormalLines = !showNormalLines;
            }
        }
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
