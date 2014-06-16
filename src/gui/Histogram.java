package gui;

import controller.DCMImage;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

/**
 * A JavaFX <code>Stage</code> showing a XY plot of the pixel values in the given image and their frequency.
 * As the pixel values are floats these will be collected in the nearest integer bin.
 * 0 will be excluded from the histogram.
 */
public class Histogram extends Stage {

    private DCMImage image;

    /**
     * Constructs a new <code>Histogram</code> for the given <code>DCMImage</code>.
     *
     * @param image the image for which a <code>Histogram</code> is to be shown
     */
    public Histogram(DCMImage image) {
        this.image = image;

        BarChart<String, Number> c = createChart();

        BorderPane root = new BorderPane();
        root.setCenter(c);
        root.setPrefSize(500, 500);

        setScene(new Scene(root));
        initModality(Modality.APPLICATION_MODAL);
    }

    /**
     * Constructs the chart. Reading from the image is performed in a <code>Task</code>.
     *
     * @return the chart
     */
    private BarChart<String, Number> createChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        xAxis.setLabel("Pixel Value");
        yAxis.setLabel("Quantity");
        chart.setLegendVisible(false);

        Task<XYChart.Series<String, Number>> dataWorker = new Task<XYChart.Series<String, Number>>() {

            @Override
            protected XYChart.Series<String, Number> call() throws Exception {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                Attributes attributes = image.getAttributes();
                int smallest = attributes.getInt(Tag.SmallestImagePixelValue, 0);
                int largest = attributes.getInt(Tag.LargestImagePixelValue, 1000);
                int[] values = new int[largest + 1];
                float[][] pixels = image.getImageRaster();

                double floor;
                double ceil;
                for (float[] row : pixels) {
                    for (float pixelValue : row) {

                        if (Float.compare(0, pixelValue) == 0) {
                            continue;
                        }

                        floor = Math.floor(pixelValue);
                        ceil = Math.ceil(pixelValue);

                        if ((pixelValue - floor) < (ceil - pixelValue)) {
                            values[(int) floor]++;
                        } else {
                            values[(int) ceil]++;
                        }
                    }
                }

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != 0) {
                        series.getData().add(new XYChart.Data<>(String.valueOf(i + smallest), values[i]));
                    }
                }

                return series;
            }
        };

        dataWorker.setOnSucceeded(value -> chart.getData().add(dataWorker.getValue()));

        new Thread(dataWorker).start();

        return chart;
    }
}
