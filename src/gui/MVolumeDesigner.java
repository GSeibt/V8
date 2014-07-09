package gui;

import java.io.IOException;

import controller.mc_alg.metaball_volume.MetaBallVolume;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MVolumeDesigner extends GridPane {

    @FXML
    private Label currBallsLabel;
    @FXML
    private IntSpinner sizeX;
    @FXML
    private IntSpinner sizeY;
    @FXML
    private IntSpinner sizeZ;
    @FXML
    private IntSpinner posX;
    @FXML
    private IntSpinner posY;
    @FXML
    private IntSpinner posZ;
    @FXML
    private IntSpinner intensity;
    @FXML
    private Button goBtn;
    @FXML
    private Button rndBtn;
    @FXML
    private Button addBtn;

    private MetaBallVolume tempVolume;
    private MetaBallVolume volume;

    private MVolumeDesigner() {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MVolumeDesigner.fxml"));

        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tempVolume = new MetaBallVolume(sizeX.getValue(), sizeY.getValue(), sizeZ.getValue());

        sizeX.valueProperty().addListener((observable, oldV, newV) -> {
            tempVolume.setX_dim(newV.intValue());
        });

        sizeY.valueProperty().addListener((observable, oldV, newV) -> {
            tempVolume.setY_dim(newV.intValue());
        });

        sizeZ.valueProperty().addListener((observable, oldV, newV) -> {
            tempVolume.setZ_dim(newV.intValue());
        });

        posX.endProperty().bind(sizeX.valueProperty());
        posY.endProperty().bind(sizeY.valueProperty());
        posZ.endProperty().bind(sizeZ.valueProperty());
        intensity.setStart(-1000);
        intensity.setEnd(1000);
    }

    /**
     * Returns the designed volume.
     * The method will return <code>null</code> until the <code>Window</code> containing this
     * <code>MVolumeDesigner</code> was closed using its 'Go!' button.
     *
     * @return the volume
     */
    public MetaBallVolume getVolume() {
        return volume;
    }

    @FXML
    private void goClicked() {
        Window window = getScene().getWindow();

        if (window instanceof Stage) {
            volume = tempVolume;
            ((Stage) window).close();
        }
    }

    @FXML
    private void randomClicked() {
        tempVolume.addRandomBall();
        updateLabel();
    }

    @FXML
    private void addClicked() {
        tempVolume.addBall(posX.getValue(), posY.getValue(), posZ.getValue(), intensity.getValue());
        updateLabel();
    }

    /**
     * Updates the <code>currBallsLabel</code>.
     */
    private void updateLabel() {
        currBallsLabel.setText(String.format("Current balls: %d", tempVolume.getNumBalls()));
    }

    /**
     * Shows a <code>MVolumeDesigner</code> and returns the designed volume. If the window is dismissed
     * <code>null</code> will be returned.
     *
     * @return the designed volume or <code>null</code>
     */
    public static MetaBallVolume showVolumeDesigner() {
        Stage stage = new Stage();
        MVolumeDesigner designer = new MVolumeDesigner();
        Scene scene = new Scene(designer);

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        return designer.getVolume();
    }

    @Override
    public String toString() {
        return String.format("%s %dx%dx%d Balls: %d", MVolumeDesigner.class.getSimpleName(), sizeX.getValue(),
                sizeY.getValue(), sizeZ.getValue(), tempVolume.getNumBalls());
    }
}
