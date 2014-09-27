package gui;

import controller.DCMImage;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A JavaFX <code>Stage</code> showing a XY plot of the pixel values in the given image and their frequency.
 * As the pixel values are floats these will be collected in the nearest integer bin.
 * 0 will be excluded from the histogram.
 */
public class Histogram extends Stage {

    private DCMImage image;
    private Label minLabel;
    private Label maxLabel;

    /**
     * Constructs a new <code>Histogram</code> for the given <code>DCMImage</code>.
     *
     * @param image the image for which a <code>Histogram</code> is to be shown
     */
    public Histogram(DCMImage image) {
        this.image = image;
        this.minLabel = new Label();
        this.maxLabel = new Label();

        AreaChart<Number, Number> c = createChart();

        Region filler = new Region();
        HBox labelBox = new HBox(minLabel, filler, maxLabel);
        filler.setPrefWidth(15);
        labelBox.setSpacing(5);
        labelBox.setOpaqueInsets(new Insets(10, 10, 10, 10));
        labelBox.setAlignment(Pos.BASELINE_CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(c);
        root.setBottom(labelBox);
        root.setPrefSize(700, 600);

        setScene(new Scene(root));
        initModality(Modality.WINDOW_MODAL);
    }

    /**
     * Constructs the chart. Reading from the image is performed in a <code>Task</code>.
     *
     * @return the chart
     */
    private AreaChart<Number, Number> createChart() {
        NumberAxis xAxis = new NumberAxis(0, 255, 20);
        NumberAxis yAxis = new NumberAxis();
        AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);

        xAxis.setLabel("Pixel Value");
        yAxis.setLabel("Quantity");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setCreateSymbols(false);

        Task<int[]> dataWorker = new Task<int[]>() {

            @Override
            protected int[] call() throws Exception {
                int[] values = new int[256];
                float[][] pixels = image.getImageRaster();

                double floor;
                double ceil;
                for (float[] row : pixels) {
                    for (float pixelValue : row) {

                        floor = Math.floor(pixelValue);
                        ceil = Math.ceil(pixelValue);

                        if ((pixelValue - floor) < (ceil - pixelValue)) {
                            values[(int) floor]++;
                        } else {
                            values[(int) ceil]++;
                        }
                    }
                }

                return values;
            }
        };

        dataWorker.setOnSucceeded(value -> {
            int[] values = dataWorker.getValue();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            ObservableList<XYChart.Data<Number, Number>> data = series.getData();

            int min = 0;
            int max = values.length - 1;

            minLabel.setText(String.format("Number of occurrences of the minimum value %d : %d", min, values[min]));
            maxLabel.setText(String.format("Number of occurrences of the maximum value %d : %d", max, values[max]));

            for (int i = 1; i < values.length - 1; i++) {
                data.add(new XYChart.Data<>(i, values[i]));
            }

            chart.getData().add(series);
        });

        new Thread(dataWorker).start();

        return chart;
    }
}
