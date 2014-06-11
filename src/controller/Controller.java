package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import controller.mc_alg.MCRunner;
import gui.Histogram;
import gui.opengl.GL_V8;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * JavaFX controller class for fxml/V8.fxml.
 */
public class Controller {

    @FXML
    private TextField gridSizeTextField;
    @FXML
    private TextField levelTextField;
    @FXML
    private ProgressBar mcProgress;
    @FXML
    private ProgressBar imageProgress;
    @FXML
    private ListView<File> directoriesList;
    @FXML
    private ListView<DCMImage> filesList;
    @FXML
    private ImageView imageView;
    @FXML
    private Pane imagePane;

    private Map<File, ObservableList<DCMImage>> dirCache;
    private ObservableList<File> directories;
    private Stage stage;

    /**
     * Called by the FXMLLoader, initializes the <code>Controller</code>.
     */
    @FXML
    private void initialize() {
        dirCache = new HashMap<>();
        directories = directoriesList.getItems();

        directoriesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filesList.setItems(dirCache.get(newValue));
        });

        directoriesList.setCellFactory(param -> new ListCell<File>() {

            @Override
            protected void updateItem(File item, boolean empty) {
                if (empty) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        filesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setImage(newValue.getImage());
        });

        imageView.setPreserveRatio(false);
        imageView.fitHeightProperty().bind(imagePane.heightProperty());
        imageView.fitWidthProperty().bind(imagePane.widthProperty());
    }

    /**
     * ActionListener for the 'Add Directory' menu item.
     */
    @FXML
    private void addDirectoryClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("D:\\Dropbox\\Studium\\3D Bildverarbeitung und -druck\\Flöte"));
        File dir = directoryChooser.showDialog(stage.getScene().getWindow());
        List<DCMImage> images;

        if (dir == null || directories.contains(dir)) {
            return;
        }

        try (Stream<Path> fileStream = Files.list(dir.toPath()).filter(name -> name.toString().endsWith(".dcm"))) {
            Stream<List<DCMImage>> imageStream = fileStream.map(path -> DCMImage.getDCMImages(path.toFile()));
            images = imageStream.reduce(new LinkedList<DCMImage>(), (l1, l2) -> {l1.addAll(l2); return l1; });
        } catch (IOException e) {
            System.err.println("Could not list the files in directory " + dir.getName());
            return;
        }

        dirCache.put(dir, FXCollections.observableList(images));
        directories.add(dir);
    }

    /**
     * ActionListener for the button that starts the marching cubes run.
     */
    @FXML
    private void mcStartClicked() {
        List<DCMImage> images = filesList.getItems();

        if (images.isEmpty()) {
            return;
        }

        final int level;
        try {
            level = Integer.parseInt(levelTextField.getText().trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid level. " + levelTextField.getText());
            return;
        }

        final int gridSize;
        try {
            gridSize = Integer.parseInt(gridSizeTextField.getText().trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid gridSize: " + gridSizeTextField.getText());
            return;
        }

        Task<float[][][]> rasterLoader = new Task<float[][][]>() {

            @Override
            protected float[][][] call() throws Exception {
                float[][][] data = new float[images.size()][][];

                for (int i = 0; i < images.size(); i++) {
                    data[i] = images.get(i).getImageRaster();
                    updateProgress(i, images.size());
                }

                return data;
            }
        };

        rasterLoader.setOnSucceeded(event -> {
            MCRunner mcRunner = new MCRunner(rasterLoader.getValue(), level, gridSize, MCRunner.Type.SLICE);

            mcProgress.progressProperty().bind(mcRunner.progressProperty());

            Thread glThread = new Thread(() -> new GL_V8(mcRunner).show());
            glThread.setName("OpenGL View");
            glThread.start();
        });

        imageProgress.progressProperty().bind(rasterLoader.progressProperty());

        Thread rasterLoaderThread = new Thread(rasterLoader);
        rasterLoaderThread.setName("Raster loader Thread");
        rasterLoaderThread.start();
    }

    /**
     * Sets the <code>Stage</code> this <code>Controller</code> will use to display modal dialogs and such.
     *
     * @param stage the <code>Stage</code> for the <code>Controller</code>
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * ActionListener for the 'Histogram' button.
     */
    @FXML
    private void histogramClicked() {
        new Histogram(filesList.getFocusModel().getFocusedItem()).show();
    }
}
