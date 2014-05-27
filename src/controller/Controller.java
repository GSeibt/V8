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

import controller.mc_alg.service.MCComplete;
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
import javafx.scene.shape.TriangleMesh;
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
    @FXML
    private Pane subScenePane;

    private Group world;

    private Map<File, ObservableList<DCMImage>> dirCache;
    private ObservableList<File> directories;
    private List<MeshView> loadedMeshes = new LinkedList<>();
    private Stage stage;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;

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
        setupSubScene();
    }

    private void setupImageView() {
        imageView.setPreserveRatio(false);
        imageView.fitHeightProperty().bind(imagePane.heightProperty());
        imageView.fitWidthProperty().bind(imagePane.widthProperty());
    }

    private void setupSubScene() {
        world = new Group();

        SubScene subScene = new SubScene(world, subScenePane.getWidth(), subScenePane.getHeight(), true, DISABLED);
        subScene.widthProperty().bind(subScenePane.widthProperty());
        subScene.heightProperty().bind(subScenePane.heightProperty());

        Camera camera = setupCamera(subScene);

        subScene.setFill(Color.GRAY);
        subScene.setCamera(camera);

        subScenePane.getChildren().addAll(subScene);

        addAxis(); //TODO remove
    }

    private void addAxis() { //TODO remove
        Box xAxis = new Box(1000, 1, 1);
        xAxis.setTranslateX(500);
        Box yAxis = new Box(1, 1000, 1);
        yAxis.setTranslateY(500);
        Box zAxis = new Box(1, 1, 1000);
        zAxis.setTranslateZ(500);

        PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        xAxis.setMaterial(redMaterial);

        PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        yAxis.setMaterial(greenMaterial);

        PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
        zAxis.setMaterial(blueMaterial);

        world.getChildren().addAll(xAxis, yAxis, zAxis);
    }

    private Camera setupCamera(SubScene subScene) {
        PerspectiveCamera camera = new PerspectiveCamera(true);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        Translate translate = new Translate();
        Translate pivot = new Translate();
        Rotate rotX = new Rotate();
        Rotate rotY = new Rotate();
        Rotate rotZ = new Rotate();
        rotX.setAxis(Rotate.X_AXIS);
        rotY.setAxis(Rotate.Y_AXIS);
        rotZ.setAxis(Rotate.Z_AXIS);

        camera.getTransforms().addAll(translate, pivot, rotX, rotY, rotZ);

        subScene.setOnKeyPressed(event -> {

            switch (event.getCode()) {
                case C:
                    loadedMeshes.forEach(mesh -> world.getChildren().remove(mesh));
                    break;
                case W:
                    translate.setY(translate.getY() - 5);
                    break;
                case S:
                    translate.setY(translate.getY() + 5);
                    break;
                case A:
                    translate.setX(translate.getX() - 5);
                    break;
                case D:
                    translate.setX(translate.getX() + 5);
                    break;
                case Q:
                    translate.setZ(translate.getZ() - 5);
                    break;
                case E:
                    translate.setZ(translate.getZ() + 5);
                    break;
            }
        });

        subScene.setOnMousePressed(event -> {
            subScene.requestFocus();
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            double mouseDeltaX = (mousePosX - mouseOldX);
            double mouseDeltaY = (mousePosY - mouseOldY);

            rotY.setAngle(rotY.getAngle() + mouseDeltaX * 0.1);
            rotX.setAngle(rotX.getAngle() - mouseDeltaY * 0.1);
        });

        translate.setZ(-1000);

        return camera;
    }

    @FXML
    private void addDirectoryClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("D:\\Code\\IntelliJ Projects\\V8")); //TODO remove
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

                long time = System.nanoTime(); //TODO remove
                for (int i = 0; i < images.size(); i++) {
                    data[i] = images.get(i).getImageRaster();
                    updateProgress(i, images.size());
                }
                System.out.println("Images: " + (System.nanoTime() - time) / Math.pow(10, 6) + " ms"); //TODO remove

                return data;
            }
        };

        rasterLoader.setOnSucceeded(event -> {
            MCComplete mc = new MCComplete(level, rasterLoader.getValue());
            mc.setOnSucceeded(e -> {
                MeshView meshView = new MeshView(mc.getValue());
                meshView.setMaterial(new PhongMaterial(Color.CRIMSON));

                world.getChildren().addAll(meshView);
                System.out.println("Added " + meshView.toString());
                System.out.println(((TriangleMesh) meshView.getMesh()).getFaces().size() / 2);
                loadedMeshes.add(meshView);
            });

            mc.setOnFailed(e -> {
                mc.getException().printStackTrace();
            });

            meshProgress.progressProperty().bind(mc.progressProperty());
            mc.start();
        });

        meshProgress.progressProperty().bind(rasterLoader.progressProperty());
        new Thread(rasterLoader).start();
    }
}
