package com.programmer74.jrawtool.converters;

import com.programmer74.jrawtool.byteimage.ByteImage;
import static com.programmer74.jrawtool.converters.RawToPgmConverter.openDCRawAsConverterToPGM;
import com.programmer74.jrawtool.doubleimage.DoubleImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class GenericConverter {

  public static String getFileExtension(String filename) {
    int i = filename.lastIndexOf('.');
    if (i > 0) {
      return filename.substring(i + 1).toLowerCase();
    }
    return filename;
  }

  public static String getFileNameWithoutExtension(String filename) {
    int i = filename.lastIndexOf('.');
    if (i > 0) {
      return filename.substring(0, i).toLowerCase();
    }
    return filename;
  }

  public static boolean isRaw(String filename) {
    return (filename.toLowerCase().endsWith(".nef")) || (filename.toLowerCase().endsWith(".cr2"));
  }

  private static boolean isJavaSupportedImage(String filename) {
    return getFileExtension(filename).equals("jpg") || getFileExtension(filename).equals("png")
        || getFileExtension(filename).equals("bmp");
  }

  private static boolean isPGM(String filename) {
    return getFileExtension(filename).equals("pgm");
  }

  public static DoubleImage loadPicture(String filename) {
    if (isJavaSupportedImage(filename)) {
      return JpegImage.loadPicture(filename);
    } else if (isPGM(filename)) {
      return PGMImageColoured.loadPicture(filename);
    } else if (isRaw(filename)) {
      InputStream dcrawOutput = openDCRawAsConverterToPGM(filename);
      return PGMImageColoured.loadPictureFromInputStream(dcrawOutput);
    }
    return null;
  }

  public static ByteImage loadPreview(String filename) {
    if (isRaw(filename)) {
      return PGMImageColoured.loadPreview(filename);
    } else if (isJavaSupportedImage(filename)) {
      return JpegImage.loadPreview(filename);
    }
    return null;
  }

  public static Image loadSmallPreview(String filename, int maxWidth, int maxHeight) {
    BufferedImage img = null;
    if (isRaw(filename)) {
      img = PGMImageColoured.loadPreview(filename).getBufferedImage();
    } else if (isJavaSupportedImage(filename)) {
      img = JpegImage.loadPreview(filename).getBufferedImage();
    }
    if (img == null) {
      return null;
    }

    int imageWidth = img.getWidth(null);
    int imageHeight = img.getHeight(null);

    double wK = imageWidth * 1.0 / maxWidth;
    double hK = imageHeight * 1.0 / maxHeight;

    double scale = 1 / Math.max(wK, hK);

    int paintW = (int)(imageWidth * 1.0 * scale);
    int paintH = (int)(imageHeight * 1.0 * scale);

    int offsetX = (maxWidth - paintW) / 2;
    int offsetY = (maxHeight - paintH) / 2;

    BufferedImage scaledImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
    Graphics g = scaledImage.getGraphics();
    g.drawImage(img, offsetX, offsetY, paintW, paintH, null);
    g.dispose();
    img.flush();
    return scaledImage;
  }
}
