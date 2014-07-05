package controller;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import controller.mc_alg.ArrayVolume;
import controller.mc_alg.MCRunner;
import controller.mc_alg.metaball_volume.MetaBallVolume;
import gui.Histogram;
import gui.IntSpinner;
import gui.PreviewImageService;
import gui.opengl.OpenGL_V8;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.OBJExporter;

import static controller.mc_alg.MCRunner.Type.COMPLETE;

/**
 * JavaFX controller class for fxml/V8.fxml.
 */
public class Controller {

    @FXML
    private Slider levelSlider;
    @FXML
    private IntSpinner gridSizeSpinner;
    @FXML
    private RadioButton cubeRBtn;
    @FXML
    private RadioButton sliceRBtn;
    @FXML
    private RadioButton completeRBtn;
    @FXML
    private RadioButton exportRBtn;
    @FXML
    private VBox loadingBarBox;
    @FXML
    private Button addBtn;
    @FXML
    private RadioButton randRButton;
    @FXML
    private RadioButton imageRButton;
    @FXML
    private ToggleGroup dataSource;
    @FXML
    private ToggleGroup mcType;
    @FXML
    private ProgressBar mcProgress;
    @FXML
    private ProgressBar dataLoadingProgress;
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
    private File lastDir; // the parent of the last directory that was added
    private Stage stage;

    boolean previewMode = false;
    private PreviewImageService previewImageService;

    /**
     * Called by the FXMLLoader, initializes the <code>Controller</code>.
     */
    @FXML
    private void initialize() {
        dirCache = new HashMap<>();
        directories = directoriesList.getItems();
        previewImageService = new PreviewImageService();

        previewImageService.setOnSucceeded(event -> {
            imageView.setImage((Image) event.getSource().getValue());
        });

        dataSource.selectedToggleProperty().addListener((o, oldV, newV) -> {

            if (randRButton.equals(newV)) {
                directoriesList.setDisable(true);
                filesList.setDisable(true);
                imageView.setDisable(true);
                addBtn.setDisable(true);
            } else if (imageRButton.equals(newV)) {
                directoriesList.setDisable(false);
                filesList.setDisable(false);
                imageView.setDisable(false);
                addBtn.setDisable(false);
            }
        });

        directoriesList.getFocusModel().focusedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filesList.setItems(dirCache.get(newValue));
            }
        });

        directoriesList.setCellFactory(param -> new ListCell<File>() {

            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        filesList.getFocusModel().focusedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                WritableImage image = newValue.getImage();

                previewImageService.setOriginalImage(image);

                if (previewMode) {
                    previewImageService.restart();
                } else {
                    imageView.setImage(image);
                }
            }
        });

        levelSlider.valueProperty().addListener((slider, oldV, newV) -> {
            previewMode = true;
            previewImageService.setLevel(newV.doubleValue());
            previewImageService.restart();
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
        directoryChooser.setInitialDirectory(lastDir);
        File dir = directoryChooser.showDialog(stage.getScene().getWindow());

        if (dir == null || directories.contains(dir)) {
            return;
        }

        lastDir = dir.getParentFile();

        File[] dcmFiles = dir.listFiles((ignored, name) -> name.endsWith(".dcm"));
        List<DCMImage> images = Arrays.stream(dcmFiles)
                        .map(DCMImage::getDCMImages)
                        .reduce(new LinkedList<>(), (l1, l2) -> {l1.addAll(l2); return l1; });

        if (images.isEmpty()) {
            return;
        }

        dirCache.put(dir, FXCollections.observableList(images));
        directories.add(dir);
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
        DCMImage focusedItem = filesList.getFocusModel().getFocusedItem();

        if (focusedItem != null) {
            new Histogram(focusedItem).show();
        }
    }

    /**
     * ActionListener for the 'Go!' button.
     */
    @FXML
    private void goClicked() {
        List<DCMImage> images = filesList.getItems();

        if (images.isEmpty() && !dataSource.getSelectedToggle().equals(randRButton)) {
            return;
        }

        float level = (float) levelSlider.getValue();
        int gridSize = gridSizeSpinner.getValue();

        final Task<float[][][]> rasterLoader;
        if (dataSource.getSelectedToggle().equals(imageRButton)) {

            rasterLoader = new Task<float[][][]>() {

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

            dataLoadingProgress.progressProperty().bind(rasterLoader.progressProperty());
        } else if (dataSource.getSelectedToggle().equals(randRButton)) {

            rasterLoader = new Task<float[][][]>() {

                @Override
                protected float[][][] call() throws Exception {
                    MetaBallVolume volume = new MetaBallVolume(200, 200, 200);

                    volume.setBalls(10);
                    dataLoadingProgress.progressProperty().bind(volume.progressProperty());

                    return volume.getVolume();
                }
            };
        } else {
            return;
        }

        Toggle selToggle = mcType.getSelectedToggle();
        if (selToggle.equals(exportRBtn)) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wavefront OBJ", "*.obj"));

            File saveFile = fileChooser.showSaveDialog(stage);

            if (saveFile == null) {
                return;
            }

            rasterLoader.setOnSucceeded(event -> {
                MCRunner mcRunner = new MCRunner(new ArrayVolume(rasterLoader.getValue()), level, gridSize, COMPLETE);

                mcProgress.progressProperty().bind(mcRunner.progressProperty());
                mcRunner.setOnRunFinished(l -> Platform.runLater(() -> loadingBarBox.setVisible(false)));
                mcRunner.setOnMeshFinished(m -> new Thread(() -> OBJExporter.export(m, saveFile)).start());

                Thread runnerThread = new Thread(mcRunner);
                runnerThread.setName(MCRunner.class.getSimpleName());
                runnerThread.start();
            });
        } else if (selToggle.equals(cubeRBtn) || selToggle.equals(sliceRBtn) || selToggle.equals(completeRBtn)) {
            MCRunner.Type type = MCRunner.Type.valueOf(((RadioButton) selToggle).getText().toUpperCase());

            rasterLoader.setOnSucceeded(event -> {
                MCRunner mcRunner = new MCRunner(new ArrayVolume(rasterLoader.getValue()), level, gridSize, type);

                mcProgress.progressProperty().bind(mcRunner.progressProperty());
                mcRunner.setOnRunFinished(l -> Platform.runLater(() -> loadingBarBox.setVisible(false)));

                Thread glThread = new Thread(() -> new OpenGL_V8(mcRunner).show());
                glThread.setName(OpenGL_V8.class.getSimpleName());
                glThread.start();
            });
        } else {
            return;
        }

        Thread rasterLoaderThread = new Thread(rasterLoader);
        rasterLoaderThread.setName("RasterLoader");
        rasterLoaderThread.start();

        loadingBarBox.setVisible(true);
    }

    /**
     * ActionListener for the 'Reset' button.
     */
    @FXML
    private void resetClicked() {
        previewMode = false;
        imageView.setImage(previewImageService.getOriginalImage());
    }
}
