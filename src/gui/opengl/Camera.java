package gui.opengl;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;

public class Camera {

    public static final Vector3f xAxis = new Vector3f(1, 0, 0);
    public static final Vector3f yAxis = new Vector3f(0, 1, 0);
    public static final Vector3f zAxis = new Vector3f(0, 0, 1);

    // position
    private Vector3f position;

    // rotation
    private Vector3f forward;
    private Vector3f upward;

    // field of view
    private float fov;
    private float aspect;

    // clipping planes
    private float nearClip;
    private float farClip;

    public Camera(float fov, float aspect, float nearClip, float farClip) {
        this.position = new Vector3f(0, 0, 0);
        this.forward = new Vector3f(0, 0, -1);
        this.upward = new Vector3f(0, 1, 0);

        this.fov = fov;
        this.aspect = aspect;
        this.nearClip = nearClip;
        this.farClip = farClip;

        initGLProjection();
    }

    public void initGLProjection() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(fov, aspect, nearClip, farClip);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
    }

    public void input() {
        keyboard();
        mouse();
    }

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

    private void keyboard() {
        if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
            move(forward, 0.3f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
            move(forward, -0.3f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
            move(getLeft(), 0.3f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
            move(getRight(), 0.3f);
        }

        if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            pitch(0.1f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            pitch(-0.1f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            yaw(-0.1f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            yaw(0.1f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            roll(-0.1f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_E)) {
            roll(0.1f);
        }
    }

    public void useView() {
        gluLookAt(position.getX(), position.getY(), position.getZ(), position.getX() + forward.getX(),
                position.getY() + forward.getY(), position.getZ() + forward.getZ(), upward.getX(), upward.getY(),
                upward.getZ());
    }

    public void move(Vector3f dir, float amount) {
        position = position.add(dir.mul(amount));
    }

    public void roll(float angle) {
        upward = upward.rotate(forward, angle);
    }

    public void pitch(float angle) {
        Vector3f cross = getLeft();

        upward = upward.rotate(cross, angle);
        forward = forward.rotate(cross, angle);
    }

    public void yaw(float angle) {
        forward = forward.rotate(upward, angle);
    }

    public Vector3f getLeft() {
        return upward.cross(forward).normalized();
    }

    public Vector3f getRight() {
        return forward.cross(upward).normalized();
    }
}
