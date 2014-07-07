package gui.opengl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;
import javax.imageio.ImageIO;

import controller.mc_alg.MCRunner;
import controller.mc_alg.Mesh;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import util.Vector3f;

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

/**
 * A window showing the mesh resulting from an <code>MCRunner</code> instance.
 * Capabilities include displaying a coordinate system, the unit cubes of the Marching Cubes algorithm, displaying
 * the mesh as lines or filled polygons and enabling/disabling lighting. Screenshots will be placed in a directory
 * called 'screenshots' in the current working directory. Keybindings are as follows:<br><br>
 *
 * <center>
 * <table border="1" summary="Keybindings for the camera.">
 * <caption>Keybindings for the camera.</caption>
 * <tr><td>Key</td><td>Effect</td></tr>
 * <tr><td>W</td><td>Move forward</td></tr>
 * <tr><td>A</td><td>Strafe left</td></tr>
 * <tr><td>S</td><td>Move backward</td></tr>
 * <tr><td>D</td><td>Strafe right</td></tr>
 * <tr><td>Q</td><td>Roll counterclockwise</td></tr>
 * <tr><td>E</td><td>Roll clockwise</td></tr>
 * <tr><td>UP</td><td>Pitch up</td></tr>
 * <tr><td>DOWN</td><td>Pitch down</td></tr>
 * <tr><td>LEFT</td><td>Yaw left</td></tr>
 * <tr><td>RIGHT</td><td>Yaw right</td></tr>
 * </table><br>
 *
 * <table border="1" summary="Keybindings for the rendering.">
 * <caption>Keybindings for the rendering.</caption>
 * <tr><td>Key</td><td>Effect</td></tr>
 * <tr><td>G</td><td>Switch between wireframe/fill mode for primitives</td></tr>
 * <tr><td>L</td><td>Switch lighting on/off</td></tr>
 * <tr><td>C</td><td>Switch cull face on/off</td></tr>
 * <tr><td>K</td><td>Show/hide coordinate system</td></tr>
 * <tr><td>B</td><td>Show/hide unit cubes</td></tr>
 * <tr><td>N</td><td>Show/hide normal vectors</td></tr>
 * <tr><td>INSERT</td><td>Take a screenshot</td></tr>
 * </table><br>
 *
 * <table border="1" summary="Keybindings for the Marching Cubes algorithm.">
 * <caption>Keybindings for the Marching Cubes algorithm.</caption>
 * <tr><td>Key</td><td>Effect</td></tr>
 * <tr><td>P</td><td>Pause/unpause computation</td></tr>
 * <tr><td>PERIOD</td><td>Continue computation for one cube/slice</td></tr>
 * </table>
 * </center>
 */
public class OpenGL_V8 {

    static {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        File libFile;
        String name;

        if (os.contains("windows")) {
            if (arch.contains("64")) {
                name = "lwjgl64.dll";
            } else {
                name = "lwjgl.dll";
            }
        } else if (os.contains("linux")){
            if (arch.contains("64")) {
                name = "liblwjgl64.so";
            } else {
                name = "liblwjgl.so";
            }
        } else if (os.contains("mac")) {
            name = "liblwjgl.jnilib";
        } else {
            throw new UnsatisfiedLinkError(
                    "Could not find an appropriate native LWJGL library for " + os + " " + arch + ".");
        }
        libFile = new File(name);

        if (!libFile.exists()) {
            try (InputStream libStream = OpenGL_V8.class.getResourceAsStream("/" + name)) {
                Files.copy(libStream, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UnsatisfiedLinkError(
                        "Could not copy the required library to where it can be loaded. " + e.getMessage());
            }
        }

        System.load(libFile.getAbsolutePath());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                String cPath = new File(OpenGL_V8.class.getResource("/").toURI()).getAbsolutePath();
                String cName = FileDeleter.class.getCanonicalName();
                String path = libFile.getAbsolutePath();
                Runtime.getRuntime().exec(new String[] {"java", "-cp", cPath, cName, "5000", path});
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }));
    }

    private SynchronousQueue<Mesh> newBuffer;
    private FloatBuffer lightPosition;
    private MCRunner mcRunner;
    private Camera camera;
    private final File scDir; // the screenshot directory

    private int vertexVBOID;  // Vertex Buffer Object ID (Points, Normal Points)
    private int indexVBOID;   // Vertex Buffer Object ID (Indices)
    private int normalVBOID;  // Vertex Buffer Object ID (Normals)
    private int indicesCount; // how many indices should be drawn (the triangles of the mesh)

    private boolean showNormalLines;
    private boolean showCubes;
    private boolean showCoordinateSystem;

    /**
     * Constructs a new <code>OpenGL_V8</code> window that will show the results of the given <code>mcRunner</code>.
     * Note that this constructor must be called in the same thread as the {@link #show()} method.
     *
     * @param mcRunner the <code>MCRunner</code> for this <code>OpenGL_V8</code>
     */
    public OpenGL_V8(MCRunner mcRunner) {

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

        int fov = 70;
        float aspectRatio = Display.getWidth() / (float) Display.getHeight();
        float nearClip = 0.1f;
        float farClip = 10000;

        this.newBuffer = new SynchronousQueue<>();
        this.camera = new Camera(fov, aspectRatio, nearClip, farClip);
        this.mcRunner = mcRunner;
        this.mcRunner.setOnMeshFinished(this::receiveUpdate);
        this.showNormalLines = false;
        this.showCubes = false;
        this.showCoordinateSystem = false;
        this.scDir = new File("./screenshots");
    }

    /**
     * Initializes general OpenGL settings.
     */
    private void initGL() {
        glMatrixMode(GL_MODELVIEW);
        glClearColor(0.5f, 0.5f, 0.5f, 1);
        glClearDepth(1);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }

    /**
     * Initializes OpenGL settings pertaining to lighting.
     */
    private void initGLLight() {
        glMatrixMode(GL_MODELVIEW);
        glShadeModel(GL_SMOOTH);

        lightPosition = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[] {1, 1, 0, 1}).flip();

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

    /**
     * Initializes vertex buffer objects for storing the data the <code>MCRunner</code> produces.
     */
    private void initGLObjects() {

        // create a new Vertex Buffer Object for the vertexes
        vertexVBOID = glGenBuffersARB();
        glBindBufferARB(GL_ARRAY_BUFFER, vertexVBOID);

        // create a new Vertex Buffer Object for the normals
        normalVBOID = glGenBuffersARB();
        glBindBufferARB(GL_ARRAY_BUFFER, normalVBOID);

        // create a new Vertex Buffer Object for the indices
        indexVBOID = glGenBuffersARB();
        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, vertexVBOID);

        glBindBufferARB(GL_ARRAY_BUFFER, 0);
        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Initializes the LWJGL <code>Display</code> class.
     *
     * @throws LWJGLException if there is any exception initializing the <code>Display</code>
     */
    private void initDisplay() throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(1024, 786));
        Display.setTitle("V8");
        Display.create();
    }

    /**
     * Initializes the LWJGL input classes for <code>Mouse</code> and <code>Keyboard</code>.
     *
     * @throws LWJGLException if there is any exception initializing the <code>Display</code>
     */
    private void initInput() throws LWJGLException {
        Keyboard.create();
        Mouse.create();
    }

    /**
     * Shows the window and starts the <code>MCRunner</code> in a new thread. This method blocks until the window is
     * closed. Note that this method must be called in the same thread as the constructor of this instance of
     * <code>GL_V8</code>.
     */
    public void show() {
        Thread runner = new Thread(mcRunner);
        runner.setName(mcRunner.getClass().getSimpleName());

        runner.start();
        while (!Display.isCloseRequested()) {
            update();
            input();
            draw();
            Display.update();
            Display.sync(30);
        }

        runner.interrupt();
        cleanup();
    }

    /**
     * Draws the scene.
     */
    private void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        camera.useView();

        glLight(GL_LIGHT0, GL_POSITION, lightPosition);

        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBufferARB(GL_ARRAY_BUFFER, vertexVBOID);
        glVertexPointer(3, GL_FLOAT, 24, 0);

        glEnableClientState(GL_NORMAL_ARRAY);
        glBindBufferARB(GL_ARRAY_BUFFER, normalVBOID);
        glNormalPointer(GL_FLOAT, 0, 0);

        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, indexVBOID);

        glColor3f(1f, 0, 0);
        glDrawElements(GL_TRIANGLES, indicesCount, GL_UNSIGNED_INT, 0);

        if (showNormalLines) {
            drawNormalLines();
        }

        if (showCubes) {
            drawCubes();
        }

        if (showCoordinateSystem) {
            drawCoordinateSystem();
        }

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);

        glBindBufferARB(GL_ARRAY_BUFFER, 0);
        glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Draws a coordinate system. X, Y, and Z axis are red, green, and blue respectively.
     */
    private void drawCoordinateSystem() {
        boolean lighting = glIsEnabled(GL_LIGHTING);
        int oldLineWidth = glGetInteger(GL_LINE_WIDTH);

        glDisable(GL_LIGHTING);

        glLineWidth(3);
        glBegin(GL_LINES);

        glColor3f(1f,  0, 0 );
        glVertex3i(0, 0, 0);

        int length = 800;
        int arrowLength = 200;
        int arrowHeight = 30;

        glVertex3i(length, 0, 0);
        glVertex3i(length, 0, 0);
        glVertex3i(length - arrowLength, arrowHeight, 0);
        glVertex3i(length, 0, 0);
        glVertex3i(length - arrowLength, -arrowHeight, 0);

        glColor3f(0,  1f, 0 );
        glVertex3i(0, 0, 0);
        glVertex3i(0, length, 0);
        glVertex3i(0, length, 0);
        glVertex3i(arrowHeight, length - arrowLength, 0);
        glVertex3i(0, length, 0);
        glVertex3i(-arrowHeight, length - arrowLength, 0);

        glColor3f(0,  0, 1f );
        glVertex3i(0, 0, 0);
        glVertex3i(0, 0, length);
        glVertex3i(0, 0, length);
        glVertex3i(arrowHeight, 0, length - arrowLength);
        glVertex3i(0, 0, length);
        glVertex3i(-arrowHeight, 0, length - arrowLength);

        glEnd();

        if (lighting) {
            glEnable(GL_LIGHTING);
        }
        glLineWidth(oldLineWidth);
    }

    /**
     * Draws 1x1x1 cubes around the camera corresponding to cubes used by the Marching Cubes algorithm.
     */
    private void drawCubes() {
        boolean lighting = glIsEnabled(GL_LIGHTING);
        Vector3f camPosition = camera.getPosition();
        int camX = (int) camPosition.getX();
        int camY = (int) camPosition.getY();
        int camZ = (int) camPosition.getZ();
        int numCubes = 3;

        glDisable(GL_LIGHTING);

        glColor3f(0, 1f, 0);
        glBegin(GL_LINES);

        for (int z = camZ - numCubes; z <= camZ + numCubes; z++) {

            for (int y = camY - numCubes; y <= camY + numCubes; y++) {
                glVertex3i(camX - numCubes, y, z);
                glVertex3i(camX + numCubes, y, z);
            }

            for (int x = camX - numCubes; x <= camX + numCubes; x++) {
                glVertex3i(x, camY - numCubes, z);
                glVertex3i(x, camY + numCubes, z);
            }
        }

        for (int x = camX - numCubes; x <= camX + numCubes; x++) {

            for (int y = camY - numCubes; y <= camY + numCubes; y++) {
                glVertex3i(x, y, camZ - numCubes);
                glVertex3i(x, y, camZ + numCubes);
            }
        }

        glEnd();

        if (lighting) {
            glEnable(GL_LIGHTING);
        }
    }

    /**
     * Draws the normal lines that were given in the last received <code>Mesh</code>.
     */
    private void drawNormalLines() {
        boolean lighting = glIsEnabled(GL_LIGHTING);

        glDisable(GL_LIGHTING);

        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBufferARB(GL_ARRAY_BUFFER, vertexVBOID);
        glVertexPointer(3, GL_FLOAT, 0, 0);

        glEnableClientState(GL_NORMAL_ARRAY);
        glBindBufferARB(GL_ARRAY_BUFFER, 0);

        glColor3f(0, 0, 1f);
        glDrawArrays(GL_LINES, 0, indicesCount);

        if (lighting) {
            glEnable(GL_LIGHTING);
        }
    }

    /**
     * This method will be called by the <code>MCRunner</code> thread when a new mesh was produced.
     *
     * @param mesh the new mesh
     */
    private void receiveUpdate(Mesh mesh) {
        try { newBuffer.put(mesh); } catch (InterruptedException ignored) {}
    }

    /**
     * Checks whether a new <code>Mesh</code> was produced by the <code>MCRunner</code> and if so buffers the
     * received data.
     */
    private void update() {
        Mesh change = newBuffer.poll();

        if (change != null) {
            indicesCount = change.getNumIndices();

            glBindBufferARB(GL_ARRAY_BUFFER, vertexVBOID);
            glBufferDataARB(GL_ARRAY_BUFFER, change.getVertices(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ARRAY_BUFFER, normalVBOID);
            glBufferDataARB(GL_ARRAY_BUFFER, change.getNormals(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, indexVBOID);
            glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER, change.getIndices(), GL_STATIC_DRAW_ARB);

            glBindBufferARB(GL_ARRAY_BUFFER, 0);
            glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    /**
     * Polls for input and makes the resulting changes to the state of the window.
     */
    private void input() {
        camera.input(); // move the camera

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

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_K) {
                showCoordinateSystem = !showCoordinateSystem;
            }

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_B) {
                showCubes = !showCubes;
            }

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_N) {
                showNormalLines = !showNormalLines;
            }

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_PERIOD) {
                mcRunner.continueRun();
            }

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_P) {
                boolean stopping = mcRunner.isStopping();

                if (stopping) {
                    mcRunner.setStopping(false);
                    mcRunner.continueRun();
                } else {
                    mcRunner.setStopping(true);
                }
            }

            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_INSERT) {
                screenshot();
            }
        }
    }

    /**
     * Takes a screenshot of the window and stores it in the directory 'screenshots' in the current working directory.
     * The filename will be 'SC_X.bmp' where X is a continually increasing index.
     */
    private void screenshot() {
        int width = Display.getWidth();
        int height= Display.getHeight();
        int bpp = 4; // this assumes a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);

        glReadBuffer(GL_FRONT);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        new Thread(() -> {
            String format = "BMP"; // JPG works too
            File screenshot;

            synchronized (scDir) {
                if (scDir.exists() && !scDir.isDirectory()) {
                    System.err.println("Screenshot directory could not be created.");
                    return;
                } else if (!scDir.exists()) {
                    if (!scDir.mkdir()) {
                        System.err.println("Screenshot directory could not be created.");
                        return;
                    }
                }

                File[] scFiles = scDir.listFiles((ignored, name) -> name.matches("SC_[0-9]+\\.(bmp|jpg)"));
                int nextIndex = Arrays.stream(scFiles)
                        .map(f -> Integer.parseInt(new Scanner(f.getName()).findInLine("[0-9]+")))
                        .max(Integer::compare)
                        .orElse(0);
                screenshot = new File(scDir, "SC_" + (nextIndex + 1) + "." + format.toLowerCase());

                try {
                    if (!screenshot.createNewFile()) {
                        throw new IOException("Could not create the screenshot file.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int i = (x + (width * y)) * bpp;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }

            try {
                ImageIO.write(image, format, screenshot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Handles all cleanup required after {@link #show()} has returned.
     */
    private void cleanup() {
        cleanupBuffers();
        Display.destroy();
        Keyboard.destroy();
        Mouse.destroy();
    }

    /**
     * Deletes the buffers created in {@link #initGLObjects()}.
     */
    private void cleanupBuffers() {
        glDeleteBuffersARB(vertexVBOID);
        glDeleteBuffersARB(indexVBOID);
        glDeleteBuffersARB(normalVBOID);
    }
}
