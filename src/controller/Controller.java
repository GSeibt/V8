package controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import gui.Histogram;
import gui.IntSpinner;
import gui.MSView;
import gui.MVolumeDesigner;
import gui.PreviewImageService;
import gui.opengl.MeshView3D;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.mc_alg.MCRunner;
import model.mc_alg.mc_volume.ArrayVolume;
import model.mc_alg.mc_volume.CachedVolume;
import model.mc_alg.mc_volume.MCVolume;
import model.mc_alg.metaball_volume.MetaBallVolume;
import util.Exporter;

import static model.mc_alg.MCRunner.Type.COMPLETE;

/**
 * JavaFX controller class for fxml/V8.fxml.
 */
public class Controller {

    @FXML
    private Label levelLabel;
    @FXML
    private CheckBox cacheCheckBox;
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
    boolean cacheMode;
    private PreviewImageService previewImageService;

    /**
     * Called by the FXMLLoader, initializes the <code>Controller</code>.
     */
    @FXML
    private void initialize() {
        dirCache = new HashMap<>();
        directories = directoriesList.getItems();
        previewImageService = new PreviewImageService();
        cacheMode = cacheCheckBox.isSelected();

        previewImageService.setOnSucceeded(event -> {
            imageView.setImage((Image) event.getSource().getValue());
        });

        dataSource.selectedToggleProperty().addListener((o, oldV, newV) -> {

            if (randRButton.equals(newV)) {
                levelSlider.setMin(0);
                levelSlider.setMax(5);
                directoriesList.setDisable(true);
                cacheMode = cacheCheckBox.isSelected();
                cacheCheckBox.setSelected(false);
                cacheCheckBox.setDisable(true);
                filesList.setDisable(true);
                imageView.setImage(null);
                addBtn.setDisable(true);
            } else if (imageRButton.equals(newV)) {
                int focusedIndex;

                levelSlider.setMin(0);
                levelSlider.setMax(255);
                directoriesList.setDisable(false);
                cacheCheckBox.setSelected(cacheMode);
                cacheCheckBox.setDisable(false);
                filesList.setDisable(false);
                focusedIndex = filesList.getFocusModel().getFocusedIndex();
                filesList.getSelectionModel().clearSelection();
                filesList.getSelectionModel().select(focusedIndex);
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

        filesList.getFocusModel().focusedItemProperty().addListener((value, oldValue, newValue) -> {
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
            if (!previewImageService.isRunning()) {
                previewImageService.setLevel(newV.doubleValue());
                previewImageService.restart();
            }
        });

        imageView.setPreserveRatio(false);
        imageView.fitHeightProperty().bind(imagePane.heightProperty());
        imageView.fitWidthProperty().bind(imagePane.widthProperty());

        MenuItem exportItem = new MenuItem("Export PNG");
        ContextMenu contextMenu = new ContextMenu(exportItem);

        exportItem.setOnAction(event -> {
            Image image = imageView.getImage();

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Portable Network Graphics", "*.png"));
            File saveFile = fileChooser.showSaveDialog(stage);

            if (saveFile != null) {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    ImageIO.write(bufferedImage, "PNG", saveFile);
                } catch (IOException e) {
                    System.err.println("Could not write the image to " + saveFile.getName());
                }
            }
        });

        imageView.setOnContextMenuRequested(event -> {
            if (imageView.getImage() != null) {
                contextMenu.show(imageView, event.getScreenX(), event.getScreenY());
            }
        });

        loadingBarBox.visibleProperty().addListener((o, oldV, newV) -> {
            if (oldV && !newV) {
                mcProgress.progressProperty().unbind();
                mcProgress.setProgress(0);
                dataLoadingProgress.progressProperty().unbind();
                dataLoadingProgress.setProgress(0);
            }
        });

        levelLabel.textProperty().bind(levelSlider.valueProperty().asString("%.1f"));
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

        final Task<MCVolume> rasterLoader;

        if (cacheCheckBox.isSelected() && !dataSource.getSelectedToggle().equals(randRButton)) {

            rasterLoader = new Task<MCVolume>() {

                @Override
                protected MCVolume call() throws Exception {
                    return new CachedVolume(images, 4);
                }
            };

            dataLoadingProgress.progressProperty().setValue(-1);
        } else if (dataSource.getSelectedToggle().equals(imageRButton)) {

            rasterLoader = new Task<MCVolume>() {

                @Override
                protected MCVolume call() throws Exception {
                    float[][][] data = new float[images.size()][][];

                    Iterator<DCMImage> it = images.iterator();
                    for (int i = 0; i < images.size() && it.hasNext(); i++) {
                        data[i] = it.next().getImageRaster();
                        updateProgress(i, images.size());
                    }

                    return new ArrayVolume(data);
                }
            };

            dataLoadingProgress.progressProperty().bind(rasterLoader.progressProperty());
        } else if (dataSource.getSelectedToggle().equals(randRButton)) {
            MetaBallVolume volume = MVolumeDesigner.showVolumeDesigner();

            if (volume == null) {
                return;
            }

            rasterLoader = new Task<MCVolume>() {

                @Override
                protected MCVolume call() throws Exception {
                    dataLoadingProgress.progressProperty().bind(volume.progressProperty());

                    return new ArrayVolume(volume.getVolume());
                }
            };
        } else {
            return;
        }

        loadingBarBox.setVisible(true);

        Toggle selToggle = mcType.getSelectedToggle();
        if (selToggle.equals(exportRBtn)) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wavefront OBJ", "*.obj"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Surface Tesselation Language", "*.stl"));

            File saveFile = fileChooser.showSaveDialog(stage);

            if (saveFile == null) {
                return;
            }

            rasterLoader.setOnSucceeded(event -> {
                MCRunner mcRunner = new MCRunner(rasterLoader.getValue(), level, gridSize, COMPLETE);

                mcProgress.progressProperty().bind(mcRunner.progressProperty());
                mcRunner.setOnRunFinished(l -> Platform.runLater(() -> loadingBarBox.setVisible(false)));
                mcRunner.setOnMeshFinished(m -> new Thread(() -> {
                    if (saveFile.getName().endsWith("obj")) {
                        Exporter.exportOBJ(m, saveFile);
                    } else if (saveFile.getName().endsWith("stl")) {
                        Exporter.exportSTL(m, saveFile);
                    }
                }).start());

                Thread runnerThread = new Thread(mcRunner);
                runnerThread.setName(MCRunner.class.getSimpleName());
                runnerThread.start();
            });
        } else if (selToggle.equals(cubeRBtn) || selToggle.equals(sliceRBtn) || selToggle.equals(completeRBtn)) {
            MCRunner.Type type = MCRunner.Type.valueOf(((RadioButton) selToggle).getText().toUpperCase());

            rasterLoader.setOnSucceeded(event -> {
                MCRunner mcRunner = new MCRunner(rasterLoader.getValue(), level, gridSize, type);

                mcProgress.progressProperty().bind(mcRunner.progressProperty());
                mcRunner.setOnRunFinished(l -> Platform.runLater(() -> loadingBarBox.setVisible(false)));

                Thread glThread = new Thread(() -> new MeshView3D(mcRunner).show());
                glThread.setName(MeshView3D.class.getSimpleName());
                glThread.start();
            });
        } else {
            return;
        }

        Thread rasterLoaderThread = new Thread(rasterLoader);
        rasterLoaderThread.setName("RasterLoader");
        rasterLoaderThread.start();
    }

    /**
     * ActionListener for the 'Reset' button.
     */
    @FXML
    private void resetClicked() {
        previewMode = false;
        imageView.setImage(previewImageService.getOriginalImage());
    }

    /**
     * ActionListener for the 'Marching Squares' button.
     */
    @FXML
    public void marchingSquaresClicked() {
        DCMImage image = filesList.getFocusModel().getFocusedItem();

        if (image != null) {
            new MSView(filesList, levelSlider, gridSizeSpinner).show();
        }
    }
}
