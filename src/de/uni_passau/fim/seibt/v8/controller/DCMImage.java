package de.uni_passau.fim.seibt.v8.controller;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;

/**
 * A lazy loading DICOM image constructed from a file.
 * Instances should be constructed using the {@link #getDCMImages(java.io.File)} method.
 */
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
    private int frameIndex;

    /**
     * Constructs a new <code>DCMImage</code> from the given <code>File</code>.
     *
     * @param file
     *      the .dcm <code>File</code>
     * @param  frameIndex
     *      the index of the image in the file
     */
    private DCMImage(File file, int frameIndex) {
        this.file = file;
        this.frameIndex = frameIndex;
    }

    /**
     * Reads all <code>DCMImage</code> instances from the given .dcm <code>File</code>.
     *
     * @param file
     *         the .dcm <code>File</code>
     *
     * @return a list of <code>DCMImage</code> instances contained in the file
     */
    public static List<DCMImage> getDCMImages(File file) {
        List<DCMImage> images = new LinkedList<>();
        int numImages;

        try {
            imageReader.setInput(new FileImageInputStream(file));
            numImages = imageReader.getNumImages(true);

            for (int i = 0; i < numImages; i++) {
                images.add(new DCMImage(file, i));
            }
        } catch (Exception e) {
            System.err.println("Could not read the images in " + file.getName());
            System.err.println(e);
        }

        return images;
    }

    /**
     * Gets a <code>WritableImage</code> representation of this DICOM image.
     *
     * @return
     *      the resulting <code>WritableImage</code>
     */
    public WritableImage getImage() {

        BufferedImage image = getAWTImage();

        if (fxImage == null) {
            fxImage = new WritableImage(image.getWidth(), image.getHeight());
        }

        return SwingFXUtils.toFXImage(image, fxImage);
    }

    /**
     * Gets the AWT <code>BufferedImage</code> representing this DICOM image.
     *
     * @return
     *      the resulting <code>BufferedImage</code>
     */
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

    /**
     * Reads the AWT image from the <code>file</code>.
     *
     * @return
     *      the resulting <code>BufferedImage</code>
     * @throws IOException
     *      if there is an exception reading from disk
     */
    private BufferedImage readAWTImage() throws IOException {
        BufferedImage bufferedImage;

        imageReader.setInput(new FileImageInputStream(file));
        bufferedImage = imageReader.read(frameIndex, imageReader.getDefaultReadParam());

        if (bufferedImage == null) {
            System.err.println("Could not read a BufferedImage from an image." + file);
        }

        return bufferedImage;
    }

    /**
     * Gets the raw image raster as a 2D Array of floats. Sub-arrays are rows of pixels.
     *
     * @return
     *      the raster
     */
    public float[][] getImageRaster() {
        Raster raster = getAWTImage().getRaster();

        int w = raster.getWidth();
        int h = raster.getHeight();

        float[][] pixels = new float[h][];
        for (int i = 0; i < h; i++) {
            pixels[i] = raster.getPixels(0, i, w, 1, new float[w]);
        }

        return pixels;
    }

    /**
     * Resets the cached data this <code>DCMImage</code> holds.
     */
    public void reset() {
        awtImage = null;
        fxImage = null;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
