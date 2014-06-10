package gui.opengl;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * A <code>Camera</code> for an OpenGL scene.
 */
public class Camera {

    private final int fov;
    private final float aspectRatio;
    private final float nearClip;
    private final float farClip;

    // position
    private Vector3f position;

    // rotation
    private Vector3f forward;
    private Vector3f upward;

    /**
     * Constructs a new <code>Camera</code> using the given parameters.
     * The <code>Camera</code> will be placed at (0,0,0) looking along the negative z-axis.
     * OpenGL calls to initialise the projection matrix will be made.
     *
     * @param fov
     *         the field of view in degrees
     * @param aspectRatio
     *         the aspect ratio
     * @param nearClip
     *         the near clipping plane
     * @param farClip
     *         the far clipping plane
     */
    public Camera(int fov, float aspectRatio, float nearClip, float farClip) {
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearClip = nearClip;
        this.farClip = farClip;
        this.position = new Vector3f(0, 0, 0);
        this.forward = new Vector3f(0, 0, -1);
        this.upward = new Vector3f(0, 1, 0);

        initGL();
    }

    /**
     * Initialises the OpenGL projection matrix.
     */
    private void initGL() {
        int prevMode = glGetInteger(GL_MATRIX_MODE);

        glMatrixMode(GL_PROJECTION);
        gluPerspective(fov, aspectRatio, nearClip, farClip);
        glMatrixMode(prevMode);
    }

    /**
     * Polls input events and moves the camera appropriately.
     */
    public void input() {
        keyboard();
        mouse();
    }

    /**
     * Polls mouse input.
     */
    private void mouse() {

        while (Mouse.next()) {

            if (!Mouse.isButtonDown(0)) {
                continue;
            }

            int dx = Mouse.getEventDX();
            int dy = Mouse.getEventDY();

            yaw(dx * -0.01f);
            pitch(dy * -0.01f);
        }
    }

    /**
     * Polls keyboard input.
     */
    private void keyboard() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            move(forward, 1f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            move(forward, -1f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            move(getLeft(), 1f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            move(getRight(), 1f);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            pitch(-0.1f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            pitch(0.1f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            yaw(0.1f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            yaw(-0.1f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            roll(-0.1f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
            roll(0.1f);
        }
    }

    /**
     * Makes OpenGL calls to adjust the eye position according to the camera position.
     */
    public void useView() {
        Vector3f center = position.add(forward);

        gluLookAt(position.getX(), position.getY(), position.getZ(), center.getX(), center.getY(), center.getZ(),
                upward.getX(), upward.getY(), upward.getZ());
    }

    /**
     * Moves the <code>Camera</code> in the given direction by the given amount.
     * The <code>dir</code> vector will be normalized internally.
     *
     * @param dir
     *         the direction
     * @param amount
     *         the amount
     */
    public void move(Vector3f dir, float amount) {
        position = position.add(dir.normalized().mul(amount));
    }

    /**
     * Rotates the camera around its forward vector.
     *
     * @param angle
     *         the angle in degrees by which to rotate
     */
    public void roll(float angle) {
        upward = upward.rotate(forward, angle);
    }

    /**
     * Rotates the camera around a vector pointing to its left.
     *
     * @param angle
     *         the angle in degrees by which to rotate
     */
    public void pitch(float angle) {
        Vector3f cross = getLeft();

        upward = upward.rotate(cross, angle);
        forward = forward.rotate(cross, angle);
    }

    /**
     * Rotates the camera around its upward vector.
     *
     * @param angle
     *         the angle in degrees by which to rotate
     */
    public void yaw(float angle) {
        forward = forward.rotate(upward, angle);
    }

    /**
     * Gets a normalized vector pointing to the cameras left.
     *
     * @return the vector pointing to the left
     */
    public Vector3f getLeft() {
        return upward.cross(forward).normalized();
    }

    /**
     * Gets a normalized vector pointing to the cameras right.
     *
     * @return the vector pointing to the right
     */
    public Vector3f getRight() {
        return forward.cross(upward).normalized();
    }
}
