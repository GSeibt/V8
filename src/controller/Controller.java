package controller;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gui.opengl.GL_V8;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import static javafx.scene.SceneAntialiasing.DISABLED;

public class Controller {

    @FXML
    private TextField levelTextField;
    @FXML
    private ProgressBar meshProgress;
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

        setupImageView();
    }

    private void setupImageView() {
        imageView.setPreserveRatio(false);
        imageView.fitHeightProperty().bind(imagePane.heightProperty());
        imageView.fitWidthProperty().bind(imagePane.widthProperty());
    }

    @FXML
    private void addDirectoryClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
       // directoryChooser.setInitialDirectory(new File("D:\\Code\\IntelliJ Projects\\V8")); //TODO remove
        File dir = directoryChooser.showDialog(stage.getScene().getWindow());

        if (dir == null || directories.contains(dir)) {
            return;
        }

        FileFilter filter = pathname -> pathname.getName().endsWith(".dcm");
        Stream<DCMImage> s = Arrays.stream(dir.listFiles(filter)).map(DCMImage::new);
        List<DCMImage> images = s.collect(Collectors.toList());

        dirCache.put(dir, FXCollections.observableList(images));
        directories.add(dir);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void cubesClicked() {
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
            float[][][] data = rasterLoader.getValue();

            new Thread(() -> new GL_V8(data, level).show()).start();
        });

        meshProgress.progressProperty().bind(rasterLoader.progressProperty());
        new Thread(rasterLoader).start();
    }
}
