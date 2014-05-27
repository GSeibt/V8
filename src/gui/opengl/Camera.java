package gui.opengl;

import javafx.geometry.Point3D;
import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

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
        this.forward = new Vector3f(0, 0, 1);
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
            rotateX(0.1f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            rotateX(-0.1f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            rotateY(0.1f);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            rotateY(-0.1f);
        }
    }

    public void useView() {
        double xAngle = Math.toDegrees(Math.acos(forward.dot(xAxis))) - 90;
        double yAngle = Math.toDegrees(Math.acos(forward.dot(yAxis))) - 90;

        glRotated(yAngle, 1, 0, 0);
        glRotated(xAngle, 0, 1, 0);
        glTranslatef(position.getX(), position.getY(), position.getZ());
    }

    public void move(Vector3f dir, float amount) {
        position = position.add(dir.mul(amount));
    }

    public void rotateY(float angle) {
        Vector3f horizontalAxis = yAxis.cross(forward).normalized();

        forward = forward.rotate(yAxis, angle).normalized();
        upward = forward.cross(horizontalAxis).normalized();
    }

    public void rotateX(float angle) {
        Vector3f horizontalAxis = yAxis.cross(forward).normalized();

        forward = forward.rotate(horizontalAxis, angle).normalized();
        upward = forward.cross(horizontalAxis).normalized();
    }

    public Vector3f getLeft() {
        return upward.cross(forward).normalized();
    }

    public Vector3f getRight() {
        return forward.cross(upward).normalized();
    }
}
