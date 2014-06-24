package gui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * A <code>Service</code> that produces a B/W image from the given original where black pixels have a grey value
 * <= the given level and white pixels have a grey value above the given level.
 */
public class PreviewImageService extends Service<Image> {

    private Image originalImage;
    private Task<Image> defaultTask;
    private double level = 0;

    /**
     * Constructs a new <code>PreviewImageService</code> with <code>originalImage</code> <code>null</code>
     * and <code>level</code> 0.
     */
    public PreviewImageService() {
        defaultTask = new Task<Image>() {

            @Override
            protected Image call() throws Exception {
                return null;
            }
        };
    }

    /**
     * Gets the original image this <code>PreviewImageService</code> is working from.
     *
     * @return the original
     */
    public Image getOriginalImage() {
        return originalImage;
    }

    /**
     * Sets the original image this <code>PreviewImageService</code> is working from to the given value.
     *
     * @param originalImage the new original image
     */
    public void setOriginalImage(Image originalImage) {
        this.originalImage = originalImage;
    }

    /**
     * Sets the level this <code>PreviewImageService</code> is using to the new value.
     *
     * @param level the new level
     */
    public void setLevel(double level) {
        this.level = level;
    }

    @Override
    protected Task<Image> createTask() {
        if (originalImage != null) {

            Task<Image> task = new Task<Image>() {

                @Override
                protected Image call() throws Exception {
                    int width = (int) originalImage.getWidth();
                    int height = (int) originalImage.getHeight();

                    WritableImage previewImage = new WritableImage(width, height);
                    PixelReader pixelReader = originalImage.getPixelReader();
                    PixelWriter pixelWriter = previewImage.getPixelWriter();

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            if ((pixelReader.getArgb(x, y) & 255) <= level) { // & 255 extracts grey value from ARGB int
                                pixelWriter.setColor(x, y, Color.BLACK);
                            } else {
                                pixelWriter.setColor(x, y, Color.WHITE);
                            }
                        }
                    }

                    return previewImage;
                }
            };

            return task;
        } else {
            return defaultTask;
        }
    }
}
