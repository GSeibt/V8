package gui;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class IntSpinner extends GridPane {

    private final IntegerProperty start;
    private final IntegerProperty end;
    private final IntegerProperty step;
    private final BooleanProperty cyclic;
    private final IntegerProperty value;

    @FXML
    private Button decButton;
    @FXML
    private Button incButton;
    @FXML
    private TextField textField;

    public IntSpinner() {
        this(0,1,1,false);
    }

    public IntSpinner(int start, int end) {
        this(start, end, 1, false);
    }

    public IntSpinner(int start, int end, int step, boolean cyclic) {
        this.start = new SimpleIntegerProperty(start);
        this.end = new SimpleIntegerProperty(end);
        this.step = new SimpleIntegerProperty(step);
        this.cyclic = new SimpleBooleanProperty(cyclic);
        this.value = new SimpleIntegerProperty();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/IntSpinner.fxml"));

        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setValue(start);
    }

    @FXML
    public void decrease(ActionEvent event) {
        int newValue = value.get() - step.get();

        if (newValue < start.get()) {

            if (cyclic.get()) {
                setValue(end.get() - (start.get() - newValue));
            }
        } else {
            setValue(newValue);
        }
    }

    @FXML
    public void increase(ActionEvent event) {
        int newValue = value.get() + step.get();

        if (newValue > end.get()) {

            if (cyclic.get()) {
                setValue(start.get() + (newValue - end.get()));
            }
        } else {
            setValue(newValue);
        }
    }

    private void setValue(int newValue) {
        textField.setText(String.valueOf(newValue));
        value.set(newValue);
    }

    public int getStart() {
        return start.get();
    }

    public IntegerProperty startProperty() {
        return start;
    }

    public void setStart(int start) {

        if (start > value.get()) {
            setValue(start);
        }

        this.start.set(start);
    }

    public int getEnd() {
        return end.get();
    }

    public IntegerProperty endProperty() {
        return end;
    }

    public void setEnd(int end) {
        this.end.set(end);
    }

    public int getStep() {
        return step.get();
    }

    public IntegerProperty stepProperty() {
        return step;
    }

    public void setStep(int step) {
        this.step.set(step);
    }

    public boolean getCyclic() {
        return cyclic.get();
    }

    public BooleanProperty cyclicProperty() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic.set(cyclic);
    }

    public int getValue() {
        return value.get();
    }

    public IntegerProperty valueProperty() {
        return value;
    }
}
