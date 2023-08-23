/*
 * File:    QRCodeTest.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeTest {
    
    //Constants
    
    private static final File DATA_DIR = new File("data");
    
    private static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class) {{
        put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
    }};
    
    private static final Map<DecodeHintType, Object> HINTS_PURE = new EnumMap<>(HINTS) {{
        put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
    }};
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        test();

//        String data = decodeQrCodeImage(ImageIO.read(new File("E:/Downloads/Untitled.png")));
        
        int g = 5;
    }
    
    private static void test() throws Exception {
//        BufferedImage qrCode = generateQRCodeImage("www.specimens.wiki");
        
        BufferedImage qrCodeOut = generateQRCodeImage("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        ImageIO.write(qrCodeOut, "png", new File(DATA_DIR, "qr.png"));
        
        BufferedImage qrCodeIn = ImageIO.read(new File(DATA_DIR, "qr.png"));
        String qrCodeData = decodeQrCodeImage(qrCodeIn);
        
        BufferedImage barcode = generateEAN13BarcodeImage("5012345678900");
        ImageIO.write(barcode, "png", new File(DATA_DIR, "bar.png"));
    }
    
    
    //Static Methods
    
    public static BufferedImage generateQRCodeImage(String data) throws Exception {
        final QRCodeWriter barcodeWriter = new QRCodeWriter();
        
        BitMatrix bitMatrix = barcodeWriter.encode(data, BarcodeFormat.QR_CODE, 500, 500);
        
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    
    public static String decodeQrCodeImage(BufferedImage image) throws Exception {
        final Reader reader = new MultiFormatReader();
        final MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
        
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
        BinaryBitmap hybridBitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        List<Result> results = new ArrayList<>();
        
        try {
            Result[] result = multiReader.decodeMultiple(bitmap, HINTS);
            if (result != null) {
                return result[0].getText();
            }
        } catch (Exception ignored) {
        }
        
        try {
            Result result = reader.decode(bitmap, HINTS_PURE);
            if (result != null) {
                return result.getText();
            }
        } catch (Exception ignored) {
        }
        
        try {
            Result result = reader.decode(bitmap, HINTS);
            if (result != null) {
                return result.getText();
            }
        } catch (Exception ignored) {
        }
        
        try {
            Result result = reader.decode(hybridBitmap, HINTS);
            if (result != null) {
                return result.getText();
            }
        } catch (Exception ignored) {
        }
        
        return null;
    }
    
    public static BufferedImage generateEAN13BarcodeImage(String barcodeText) throws Exception {
        EAN13Writer barcodeWriter = new EAN13Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.EAN_13, 100, 50);
        
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    
}
