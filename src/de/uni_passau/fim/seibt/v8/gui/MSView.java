package de.uni_passau.fim.seibt.v8.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import de.uni_passau.fim.seibt.v8.controller.DCMImage;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import de.uni_passau.fim.seibt.v8.model.ms_alg.MSRunner;
import de.uni_passau.fim.seibt.v8.model.ms_alg.Mesh2D;
import de.uni_passau.fim.seibt.v8.model.ms_alg.ms_volume.GridVolume;
import de.uni_passau.fim.seibt.v8.model.ms_alg.ms_volume.MSGrid;

public class MSView extends Stage {

    private final Slider levelSlider;
    private final IntSpinner gridSizeSpinner;
    private final MeshService meshService;
    private MSGrid data;

    private BorderPane root;

    private class MeshService extends Service<Mesh2D> {

        @Override
        protected Task<Mesh2D> createTask() {
            return new Task<Mesh2D>() {

                @Override
                protected Mesh2D call() throws Exception {
                    float level = (float) levelSlider.getValue();
                    int gridSize = gridSizeSpinner.getValue();

                    return new MSRunner(data, level, gridSize).call();
                }
            };
        }
    }

    public MSView(ListView<DCMImage> images, Slider levelSlider, IntSpinner gridSizeSpinner) {
        this.levelSlider = levelSlider;
        this.gridSizeSpinner = gridSizeSpinner;
        this.root = new BorderPane(new ProgressIndicator(-1));
        this.meshService = new MeshService();

        meshService.setOnSucceeded(event -> {
            drawMesh((Mesh2D) event.getSource().getValue());
        });

        images.getFocusModel().focusedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                data = new GridVolume(newV.getImageRaster());
                meshService.restart();
            }
        });

        gridSizeSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            meshService.restart();
        });

        levelSlider.valueChangingProperty().addListener((obs, oldV, newV) -> {
            if (oldV && !newV) {
                meshService.restart();
            }
        });

        levelSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (!meshService.isRunning()) {
                meshService.restart();
            }
        });

        DCMImage image = images.getFocusModel().getFocusedItem();
        if (image != null) {
            this.data = new GridVolume(image.getImageRaster());
            this.root.setPrefSize(data.xSize(), data.ySize());
            this.meshService.start();
        }

        setScene(new Scene(root));
        setTitle("Marching Squares");
    }

    private void drawMesh(Mesh2D mesh) {
        Canvas canvas = new Canvas(data.xSize(), data.ySize());
        GraphicsContext g2D = canvas.getGraphicsContext2D();
        FloatBuffer vertices = mesh.getVertices();
        IntBuffer indices = mesh.getIndices();

        g2D.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2D.setStroke(Color.RED);

        while (indices.hasRemaining()) {
            int startIndex = indices.get() * 2;
            int endIndex = indices.get() * 2;

            g2D.strokeLine(vertices.get(startIndex), vertices.get(startIndex + 1),
                    vertices.get(endIndex), vertices.get(endIndex + 1));
        }

        root.setCenter(canvas);
    }
}
