package controller;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.shape.MeshView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller {

    public ImageView imageView;
    public MeshView meshView;
    public ListView<File> directoriesList;
    public ListView<DCMImage> filesList;
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
    }

    @FXML
    private void addDirectoryClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
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
}
