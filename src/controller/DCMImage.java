package controller;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;

public class DCMImage {

    private static ImageReaderSpi spi = new DicomImageReaderSpi();
    private static DicomImageReader imageReader;

    static {
        try {
            imageReader = (DicomImageReader) spi.createReaderInstance();
        } catch (IOException e) {
            System.err.println("Could not create the DicomImageReader. " + e);
        }
    }

    private File file;
    private BufferedImage awtImage;
    private WritableImage fxImage;

    public DCMImage(File file) {
        this.file = file;
    }

    public WritableImage getImage() {

        BufferedImage image = getAWTImage();

        if (fxImage == null) {
            fxImage = new WritableImage(image.getWidth(), image.getHeight());
        }

        return SwingFXUtils.toFXImage(image, fxImage);
    }

    private BufferedImage getAWTImage() {

        if (awtImage == null) {
            try {
                awtImage = readAWTImage();
            } catch (IOException e) {
                System.err.println("Could not read an image. " + e);
            }
        }

        return awtImage;
    }

    private BufferedImage readAWTImage() throws IOException {
        BufferedImage bufferedImage;
        ImageInputStream inputStream = new FileImageInputStream(file);

        imageReader.setInput(inputStream);
        bufferedImage = imageReader.read(0);

        return bufferedImage;
    }

    public int[][] getImageRaster() {
        Raster raster = getAWTImage().getRaster();

        int w = raster.getWidth();
        int h = raster.getHeight();

        int[][] pixels = new int[h][];
        for (int i = 0; i < h; i++) {
            pixels[i] = raster.getPixels(0, i, w, 1, new int[w]);
        }

        return pixels;
    }

    public Attributes getAttributes() {
        Attributes attributes;

        try {
            imageReader.setInput(new FileImageInputStream(file));
            attributes = ((DicomMetaData) imageReader.getStreamMetadata()).getAttributes();
        } catch (IOException e) {
            System.err.println("Could not read the attributes. " + e);
            return null;
        }

        return attributes;
    }

    public void reset() {
        awtImage = null;
        fxImage = null;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
