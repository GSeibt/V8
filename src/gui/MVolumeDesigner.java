package gui;

import java.io.IOException;

import controller.mc_alg.metaball_volume.MetaBallVolume;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class MVolumeDesigner extends GridPane {

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

    private MetaBallVolume volume;

    public MVolumeDesigner() {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MVolumeDesigner.fxml"));

        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        volume = new MetaBallVolume(sizeX.getValue(), sizeY.getValue(), sizeZ.getValue());

        sizeX.valueProperty().addListener((observable, oldV, newV) -> {
            volume.setX_dim(newV.intValue());
        });

        sizeY.valueProperty().addListener((observable, oldV, newV) -> {
            volume.setY_dim(newV.intValue());
        });

        sizeZ.valueProperty().addListener((observable, oldV, newV) -> {
            volume.setZ_dim(newV.intValue());
        });


    }

    public MetaBallVolume getVolume() {
        return volume;
    }
}
