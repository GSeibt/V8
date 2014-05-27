package gui.opengl;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

public class GL_V8 {

    private final int DEFAULT_FOV = 70;

    public static void main(String[] args) {
        new GL_V8().show();
    }

    private Camera camera;

    public GL_V8() {
        try {
            initDisplay();
            initInput();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        float aspect = Display.getWidth() / (float) Display.getHeight();
        camera = new Camera(DEFAULT_FOV, aspect, 0.3f, 1000);
    }

    private void initDisplay() throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(800, 600));
        Display.create();
    }

    private void initInput() throws LWJGLException {
        Keyboard.create();
        Mouse.create();
    }

    private void show() {

        while (!Display.isCloseRequested()) {
            input();
            draw();
            Display.update();
            Display.sync(30);
            x += 1f;
        }

        cleanup();
    }

    private void input() {
        camera.input();
    }

    float x;

    private void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        camera.useView();

        glPushMatrix();
        {
            glTranslatef(0, 0, -10);
            glRotatef(x, 1, 1, 0);
            glBegin(GL_QUAD_STRIP);
                glColor3f(0f, 0.5f, 0.5f);
                glVertex3f(-1,-1,-1);
                glVertex3f(1,-1,-1);
                glColor3f(0.4f, 0.3f, 0.9f);
                glVertex3f(-1,-1,1);
                glVertex3f(1,-1,1);
                glColor3f(0.3f, 0.5f, 0f);
                glVertex3f(-1,1,1);
                glVertex3f(1,1,1);
                glColor3f(0f, 0.1f, 0.8f);
                glVertex3f(-1,1,-1);
                glVertex3f(1,1,-1);
                glColor3f(0.6f, 0f, 0.8f);
                glVertex3f(-1,-1,-1);
                glVertex3f(1,-1,-1);
            glEnd();
            glBegin(GL_QUADS);
                glColor3f(0.3f, 0.5f, 0.7f);
                glVertex3f(-1,-1,-1);
                glVertex3f(-1, -1, 1);
                glColor3f(0.2f, 0.7f, 0.9f);
                glVertex3f(-1,1,1);
                glVertex3f(-1, 1, -1);
                glColor3f(1f, 1f, 1f);
                glVertex3f(1,-1,1);
                glVertex3f(1,-1,-1);
                glColor3f(0.4f, 0.3f, 0.3f);
                glVertex3f(1,1,-1);
                glVertex3f(1,1,1);
            glEnd();
        }
        glPopMatrix();
    }

    private void cleanup() {
        Display.destroy();
        Keyboard.destroy();
        Mouse.destroy();
    }
}
