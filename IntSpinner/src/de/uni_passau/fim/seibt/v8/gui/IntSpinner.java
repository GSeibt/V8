package de.uni_passau.fim.seibt.v8.gui;

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

/**
 * A configurable spinner over an integer value.
 *
 * @see javax.swing.JSpinner
 */
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

    /**
     * Constructs a new <code>IntSpinner</code> with start = 0, end = 1, step = 1 that is not cyclic.
     */
    public IntSpinner() {
        this(0,1,1,false);
    }

    /**
     * Constructs a new <code>IntSpinner</code> with the given start and end values. Its step will be 1 and it will
     * not be cyclic.
     *
     * @param start the start value for the <code>IntSpinner</code>
     * @param end the end value for the <code>IntSpinner</code>
     */
    public IntSpinner(int start, int end) {
        this(start, end, 1, false);
    }

    /**
     * Constructs a new <code>IntSpinner</code> with the given configuration.
     *
     * @param start the start value for the <code>IntSpinner</code>
     * @param end the end value for the <code>IntSpinner</code>
     * @param step the increment/decrement with one click of the +/- buttons
     * @param cyclic whether the <code>IntSpinner</code> should be cyclic
     */
    public IntSpinner(int start, int end, int step, boolean cyclic) {
        this.start = new SimpleIntegerProperty(start);
        this.end = new SimpleIntegerProperty(end);
        this.step = new SimpleIntegerProperty(step);
        this.cyclic = new SimpleBooleanProperty(cyclic);
        this.value = new SimpleIntegerProperty();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("IntSpinner.fxml"));

        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setValue(start);

        textField.textProperty().addListener((textV, oldV, newV) -> {
            if (!newV.isEmpty()) {
                try {
                    int newValue = Integer.parseInt(newV);
                    setValue(newValue);
                } catch (NumberFormatException e) {
                    textField.setText(oldV);
                }
            }
        });
    }

    @FXML
    public void decrease(ActionEvent event) {
        int newValue = value.get() - step.get();

        setValue(newValue);
    }

    @FXML
    public void increase(ActionEvent event) {
        int newValue = value.get() + step.get();

        setValue(newValue);
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

        if (end < value.get()) {
            setValue(end);
        }

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

    public void setValue(int newValue) {

        if (newValue < start.get()) {

            if (cyclic.get()) {
                newValue = end.get() - (start.get() - newValue);
            } else {
                newValue = start.get();
            }
        } else if (newValue > end.get()) {

            if (cyclic.get()) {
                newValue = start.get() + (newValue - end.get());
            } else {
                newValue = end.get();
            }
        }

        textField.setText(String.valueOf(newValue));
        value.set(newValue);
    }
}
