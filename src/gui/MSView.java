package gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import controller.ms_alg.MSRunner;
import controller.ms_alg.Mesh2D;
import controller.ms_alg.ms_volume.MSGrid;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MSView extends Stage {

    private Canvas canvas;
    private BorderPane root;

    public MSView(MSRunner msRunner) {
        MSGrid data = msRunner.getData();

        this.root = new BorderPane(new ProgressIndicator(-1));
        this.root.setPrefSize(data.xSize(), data.ySize());
        this.canvas = new Canvas(data.xSize(), data.ySize());

        msRunner.setMeshConsumer(mesh -> Platform.runLater(() -> drawMesh(mesh)));
        new Thread(msRunner).start();

        setScene(new Scene(root));
    }

    private void drawMesh(Mesh2D mesh) {
        GraphicsContext g2D = canvas.getGraphicsContext2D();
        FloatBuffer vertices = mesh.getVertices();
        IntBuffer indices = mesh.getIndices();

        while (indices.hasRemaining()) {
            int startIndex = indices.get() * 2;
            int endIndex = indices.get() * 2;

            g2D.strokeLine(vertices.get(startIndex), vertices.get(startIndex + 1),
                    vertices.get(endIndex), vertices.get(endIndex + 1));
        }

        root.setCenter(canvas);
    }
}
