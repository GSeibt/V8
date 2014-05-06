package controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;

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
    private Image image;

    public DCMImage(File file) {
        this.file = file;
    }

    public Image getImage() {

        if (image == null) {
            try {
                image = readImage();
            } catch (IOException e) {
                System.err.println("Could not read an image. " + e);
            }
        }

        return image;
    }

    private Image readImage() throws IOException {
        BufferedImage bufferedImage;
        ImageInputStream inputStream = new FileImageInputStream(file);

        imageReader.setInput(inputStream);
        bufferedImage = imageReader.read(0);

        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
