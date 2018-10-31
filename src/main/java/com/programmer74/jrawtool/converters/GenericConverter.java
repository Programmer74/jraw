package com.programmer74.jrawtool.converters;

import com.programmer74.jrawtool.byteimage.ByteImage;
import static com.programmer74.jrawtool.converters.RawToPgmConverter.openDCRawAsConverterToPGM;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import java.io.InputStream;

public class GenericConverter {

  public static String getFileExtension(String filename) {
    int i = filename.lastIndexOf('.');
    if (i > 0) {
      return filename.substring(i + 1).toLowerCase();
    }
    return filename;
  }

  private static boolean isRaw(String filename) {
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
}
