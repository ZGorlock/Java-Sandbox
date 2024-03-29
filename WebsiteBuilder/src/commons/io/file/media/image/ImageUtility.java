/*
 * File:    ImageUtility.java
 * Package: commons.io.file.media.image
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.io.file.media.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import commons.access.Filesystem;
import commons.io.graphics.DrawUtility;
import commons.math.component.vector.IntVector;
import commons.math.component.vector.Vector;
import commons.math.number.BoundUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles image operations.
 */
public class ImageUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ImageUtility.class);
    
    
    //Static Methods
    
    /**
     * Loads an image.
     *
     * @param file The image file.
     * @return The BufferedImage loaded from the file, or null if there was an error.
     */
    public static BufferedImage loadImage(File file) {
        if (file == null) {
            return null;
        }
        
        if (Filesystem.logFilesystem()) {
            logger.trace("Filesystem: Loading image file: {}", file.getAbsolutePath());
        }
        
        try {
            BufferedImage tmpImage = ImageIO.read(file);
            BufferedImage image = new BufferedImage(tmpImage.getWidth(), tmpImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D imageGraphics = image.createGraphics();
            DrawUtility.drawImage(imageGraphics, tmpImage);
            DrawUtility.dispose(imageGraphics);
            return image;
            
        } catch (Exception e) {
            if (Filesystem.logFilesystem()) {
                logger.trace("Filesystem: Unable to load image file: {}", file.getAbsolutePath());
            }
            return null;
        }
    }
    
    /**
     * Saves an image to a file.
     *
     * @param image The image.
     * @param file  The output file.
     * @return Whether the image was successfully saved or not.
     */
    public static boolean saveImage(BufferedImage image, File file) {
        if ((image == null) || (file == null)) {
            return false;
        }
        
        if (Filesystem.logFilesystem()) {
            logger.trace("Filesystem: Saving image file: {}", file.getAbsolutePath());
        }
        
        try {
            return file.mkdirs() && ImageIO.write(image, Filesystem.getFileType(file), file);
            
        } catch (Exception e) {
            if (Filesystem.logFilesystem()) {
                logger.trace("Filesystem: Unable to save image file: {}", file.getAbsolutePath());
            }
            return false;
        }
    }
    
    /**
     * Returns the pixel dimension of an image file.
     *
     * @param image The image file.
     * @return The pixel dimensions of the image file, or null if there is an error.
     */
    public static Dimension getDimensions(File image) {
        if (image == null) {
            return null;
        }
        
        Dimension result = null;
        Iterator<ImageReader> imageReaders = ImageIO.getImageReadersBySuffix(Filesystem.getFileType(image));
        if (imageReaders.hasNext()) {
            ImageReader reader = imageReaders.next();
            try (ImageInputStream stream = new FileImageInputStream(image)) {
                reader.setInput(stream);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                result = new Dimension(width, height);
            } catch (IOException ignored) {
            } finally {
                reader.dispose();
            }
        }
        return result;
    }
    
    /**
     * Returns the pixel width of an image file.
     *
     * @param image The image file.
     * @return The pixel width of the image file, or -1 if there is an error.
     */
    public static int getWidth(File image) {
        Dimension dimensions = getDimensions(image);
        if (dimensions == null) {
            return -1;
        }
        return dimensions.width;
    }
    
    /**
     * Returns the pixel height of an image file.
     *
     * @param image The image file.
     * @return The pixel height of the image file, or -1 if there is an error.
     */
    public static int getHeight(File image) {
        Dimension dimensions = getDimensions(image);
        if (dimensions == null) {
            return -1;
        }
        return dimensions.height;
    }
    
    /**
     * Determines if the pixel data of two images is equal.
     *
     * @param image1 The first image.
     * @param image2 The second image.
     * @return Whether the pixel data of the two images is equal or not.
     */
    public static boolean pixelsEqual(BufferedImage image1, BufferedImage image2) {
        if ((image1 == null) || (image2 == null) ||
                (image1.getWidth() != image2.getWidth()) ||
                (image1.getHeight() != image2.getHeight())) {
            return false;
        }
        
        for (int x = 0; x < image1.getWidth(); x++) {
            for (int y = 0; y < image2.getHeight(); y++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Deletes the extra data in an image file, reducing the file size.
     *
     * @param image            The image file.
     * @param target           The output file.
     * @param preserveMetadata Whether or not to preserve the metadata of the image file.
     * @return Whether the image file was successfully cleaned or not.
     */
    public static boolean cleanImageFile(File image, File target, boolean preserveMetadata) {
        if ((image == null) || !image.exists()) {
            return false;
        }
        
        if (Filesystem.logFilesystem()) {
            logger.trace("Filesystem: Cleaning image file: {}", image.getAbsolutePath());
        }
        
        try {
            final File output = Filesystem.createTemporaryFile(Filesystem.getFileType(image));
            
            final Map<String, FileTime> dates = Filesystem.readDates(image);
            
            IIOImage imageData;
            IIOMetadata streamMetadata;
            
            try (final ImageInputStream imageInputStream = ImageIO.createImageInputStream(image)) {
                final ImageReader reader = ImageIO.getImageReaders(imageInputStream).next();
                try {
                    reader.setInput(imageInputStream);
                    
                    final ImageReadParam readParams = reader.getDefaultReadParam();
                    
                    imageData = reader.readAll(0, readParams);
                    streamMetadata = reader.getStreamMetadata();
                    
                } finally {
                    imageInputStream.flush();
                    reader.dispose();
                }
            }
            
            if (!preserveMetadata) {
                imageData.setMetadata(null);
                imageData.setThumbnails(null);
                streamMetadata = null;
            }
            
            try (final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(output)) {
                final ImageWriter writer = ImageIO.getImageWritersBySuffix(Filesystem.getFileType(output)).next();
                try {
                    writer.setOutput(imageOutputStream);
                    
                    final ImageWriteParam writeParams = writer.getDefaultWriteParam();
                    writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParams.unsetCompression();
                    
                    writer.write(streamMetadata, imageData, writeParams);
                    
                } finally {
                    imageOutputStream.flush();
                    writer.dispose();
                }
            }
            
            Filesystem.writeDates(output, dates);
            
            if (!Filesystem.safeReplace(output, target)) {
                throw new Exception("Failed to safely replace image file");
            }
            return true;
            
        } catch (Exception e) {
            if (Filesystem.logFilesystem()) {
                logger.trace("Filesystem: Unable to clean image file: {}", image.getAbsolutePath());
            }
            return false;
        }
    }
    
    /**
     * Deletes the extra data in an image file, reducing the file size.
     *
     * @param image            The image file.
     * @param preserveMetadata Whether or not to preserve the metadata of the image file.
     * @return Whether the image file was successfully cleaned or not.
     * @see #cleanImageFile(File, File, boolean)
     */
    public static boolean cleanImageFile(File image, boolean preserveMetadata) {
        return cleanImageFile(image, image, preserveMetadata);
    }
    
    /**
     * Deletes the extra data in an image file, reducing the file size.
     *
     * @param image  The image file.
     * @param target The output file.
     * @return Whether the image file was successfully cleaned or not.
     * @see #cleanImageFile(File, File, boolean)
     */
    public static boolean cleanImageFile(File image, File target) {
        return cleanImageFile(image, target, true);
    }
    
    /**
     * Deletes the extra data in an image file, reducing the file size.
     *
     * @param image The image file.
     * @return Whether the image file was successfully cleaned or not.
     * @see #cleanImageFile(File, File)
     */
    public static boolean cleanImageFile(File image) {
        return cleanImageFile(image, image);
    }
    
    /**
     * Crops an image.
     *
     * @param image The image.
     * @param rect  The bounds within the image to crop.
     * @return The cropped image.
     */
    public static BufferedImage cropImage(BufferedImage image, Rectangle rect) {
        if ((image == null) || (rect == null)) {
            return image;
        }
        
        rect.x = BoundUtility.truncate((int) rect.getX(), 0, image.getWidth() - 1);
        rect.y = BoundUtility.truncate((int) rect.getY(), 0, image.getHeight() - 1);
        rect.width = BoundUtility.truncate((int) rect.getWidth(), 1, image.getWidth() - rect.x);
        rect.height = BoundUtility.truncate((int) rect.getHeight(), 1, image.getHeight() - rect.y);
        
        BufferedImage cropped = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), image.getType());
        Graphics2D g2 = (Graphics2D) cropped.getGraphics();
        DrawUtility.drawImage(g2, image, new Vector(rect.getX(), rect.getY()), (int) rect.getWidth(), (int) rect.getHeight(), new IntVector(0, 0), (int) rect.getWidth(), (int) rect.getHeight());
        DrawUtility.dispose(g2);
        return cropped;
    }
    
    /**
     * Crops an image.
     *
     * @param image        The image.
     * @param offTheLeft   The number of pixels to crop off the left.
     * @param offTheTop    The number of pixels to crop off the top.
     * @param offTheRight  The number of pixels to crop off the right.
     * @param offTheBottom The number of pixels to crop off the bottom.
     * @return The cropped image.
     * @see #cropImage(BufferedImage, Rectangle)
     */
    public static BufferedImage cropImage(BufferedImage image, int offTheLeft, int offTheTop, int offTheRight, int offTheBottom) {
        if (image == null) {
            return null;
        }
        
        offTheLeft = BoundUtility.truncate(offTheLeft, 0, image.getWidth() - 1);
        offTheTop = BoundUtility.truncate(offTheTop, 0, image.getHeight() - 1);
        offTheRight = BoundUtility.truncate(offTheRight, 0, image.getWidth() - 1);
        offTheBottom = BoundUtility.truncate(offTheBottom, 0, image.getHeight() - 1);
        
        Rectangle rect = new Rectangle(offTheLeft, offTheTop,
                BoundUtility.truncate((image.getWidth() - offTheLeft - offTheRight), 1, image.getWidth() - offTheLeft),
                BoundUtility.truncate((image.getHeight() - offTheTop - offTheBottom), 1, image.getHeight() - offTheTop));
        return cropImage(image, rect);
    }
    
    /**
     * Scales an image.
     *
     * @param image The image.
     * @param scale The factor to scale the image by.
     * @return The scaled image.
     */
    public static BufferedImage scaleImage(BufferedImage image, double scale) {
        if ((image == null) || (scale == 1.0) || !BoundUtility.inBounds(scale, 0.0, Double.MAX_VALUE, false, false)) {
            return image;
        }
        
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage scaled = new BufferedImage((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), image.getType());
        return transformOp.filter(image, scaled);
    }
    
    /**
     * Scales an image to a maximum width or height, while preserving the aspect ratio.
     *
     * @param image     The image.
     * @param maxWidth  The maximum width of the final image.
     * @param maxHeight The maximum height of the final image.
     * @return The scaled image with the specified maximum width or height, or the original image if it is within the specified maximum dimensions.
     * @see #scaleImage(BufferedImage, double)
     */
    public static BufferedImage scaleImage(BufferedImage image, int maxWidth, int maxHeight) {
        if ((image == null) || (image.getWidth() <= maxWidth) && (image.getHeight() <= maxHeight)) {
            return image;
        }
        
        double scale = Math.min(((double) maxWidth / image.getWidth()), ((double) maxHeight / image.getHeight()));
        return scaleImage(image, scale);
    }
    
    /**
     * Copies an image from the clipboard.
     *
     * @return The image, or null if there is no image on the clipboard.
     */
    public static BufferedImage copyImageFromClipboard() {
        BufferedImage image = null;
        
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable transferable = clipboard.getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            
            try {
                image = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
                
            } catch (Exception e) {
                if (commons.access.Clipboard.logClipboard()) {
                    logger.trace("Clipboard: Unable to retrieve image contents from the image");
                }
                return null;
            }
        }
        
        if (commons.access.Clipboard.logClipboard()) {
            logger.trace("Clipboard: Retrieved image contents of the image");
        }
        return image;
    }
    
    /**
     * Copies an image to the clipboard.
     *
     * @param image The image.
     * @return Whether the image was copied to the clipboard or not.
     */
    public static boolean copyImageToClipboard(final BufferedImage image) {
        if (image == null) {
            return false;
        }
        
        final Transferable transferableImage = new Transferable() {
            
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                DataFlavor[] flavors = new DataFlavor[1];
                flavors[0] = DataFlavor.imageFlavor;
                return flavors;
            }
            
            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                DataFlavor[] flavors = getTransferDataFlavors();
                return Arrays.stream(flavors).anyMatch(flavor::equals);
            }
            
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (flavor.equals(DataFlavor.imageFlavor)) {
                    return image;
                } else {
                    throw new UnsupportedFlavorException(flavor);
                }
            }
            
        };
        
        try {
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(transferableImage, (clipboard1, contents) -> {
            });
            if (commons.access.Clipboard.logClipboard()) {
                logger.trace("Clipboard: Published image contents to the clipboard");
            }
            return true;
            
        } catch (Exception e) {
            if (commons.access.Clipboard.logClipboard()) {
                logger.trace("Clipboard: Failed to publish image contents to the clipboard");
            }
            return false;
        }
    }
    
    /**
     * Returns a list of available image formats.
     *
     * @return A list of available image formats.
     */
    public static List<String> getAvailableImageFormats() {
        List<String> imageFormats = new ArrayList<>();
        Iterator<ImageWriterSpi> serviceProviders = IIORegistry.getDefaultInstance()
                .getServiceProviders(ImageWriterSpi.class, false);
        while (serviceProviders.hasNext()) {
            imageFormats.addAll(Arrays.asList(serviceProviders.next().getFormatNames()));
        }
        imageFormats = imageFormats.stream().map(String::toUpperCase).distinct().sorted().collect(Collectors.toList());
        return imageFormats;
    }
    
}
