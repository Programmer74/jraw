package com.programmer74.jrawtool.converters;

import com.programmer74.jrawtool.byteimage.ByteImage;
import static com.programmer74.jrawtool.converters.RawToPgmConverter.openDCRawAsJpegPreviewExtractor;
import static java.awt.Image.SCALE_FAST;

import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;
import com.sun.xml.internal.ws.server.UnsupportedMediaException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class PGMImageColoured {

  private int[][] pgmPixels;

  private DoubleImage doubleImage;

  private int min = 65535;
  private int max = 0;

  private static boolean lastProcessedImageIsColouredImage = false;

  private double getValAt(int row, int col) {
    return pgmPixels[row][col] * 1.0;
  }

  private double interpolate(Double ... values) {
    Double sum = 0D;
    int count = 0;
    for (Double value : values) {
      sum += value;
      count++;
    }
    sum = sum / count / max;
    return sum;
  }

  //calculate max brightness value over raw bayer array
  private void calculateMinMax() {
    int pixel;

    for(int row=0; row< pgmPixels.length; ++row)
      for(int col=0; col< pgmPixels[row].length; ++col){
        pixel = pgmPixels[row][col];
        if (pixel < min) min = pixel;
        if (pixel > max) max = pixel;
      }

//    System.out.println(min + ":" + max);

    Double x = (Math.log(max) / Math.log(2));
    int bitness = x.intValue() + 1;

//    System.out.println("Raw bitness: " + bitness);

    max = (int)Math.pow(2, bitness);
//    System.out.println(max);
  }

  private void copyPixelsToDoublePixels(){
    // copy the pixels values
    for(int row=0; row< pgmPixels.length; row++)
      for(int col=0, x=0; col < pgmPixels[row].length && x < doubleImage.getWidth(); col += 3, x++){

        double r = 0, g = 0, b = 0;

        r = interpolate(getValAt(row, col));
        g = interpolate(getValAt(row, col + 1));
        b = interpolate(getValAt(row, col + 2));

        doubleImage.setPixel(x, row, r, g, b);
      }
  }

  private void colorizeBayerPixelsToDoublePixels(){

    // copy the pixels values
    for(int row=1; row<pgmPixels.length - 1; ++row)
      for(int col=1; col<pgmPixels[row].length - 1; ++col){

        double r = 0, g = 0, b = 0;

        //http://www.siliconimaging.com/RGB%20Bayer.htm
        //GB
        //RG
        //i know this is dummy, will rewrite soon
        if ((row % 2 == 0) && (col % 2 == 0)) {
          //first G pixel
          g = interpolate(getValAt(row, col));
          r = interpolate(getValAt(row + 1, col), getValAt(row - 1, col));
          b = interpolate(getValAt(row, col + 1), getValAt(row, col - 1));
        } else if ((row % 2 == 0) && (col % 2 != 0)) {
          //B pixel
          b = interpolate(getValAt(row, col));
          r = interpolate(getValAt(row + 1, col - 1));
          g = interpolate(getValAt(row, col - 1),
              getValAt(row, col + 1),
              getValAt(row - 1, col),
              getValAt(row + 1, col));
        } else if ((row % 2 != 0) && (col % 2 == 0)) {
          //R pixel
          r = interpolate(getValAt(row, col));
          b = interpolate(getValAt(row - 1, col + 1));
          g = interpolate(getValAt(row, col - 1),
              getValAt(row, col + 1),
              getValAt(row - 1, col),
              getValAt(row + 1, col));
        } else if ((row % 2 != 0) && (col % 2 != 0)) {
          //second G pixel
          g = interpolate(getValAt(row, col));
          r = interpolate(getValAt(row, col - 1), getValAt(row, col + 1));
          b = interpolate(getValAt(row - 1, col), getValAt(row + 1, col));
        }

        doubleImage.setPixel(col, row, r, g, b);
      }
  }

  private PGMImageColoured() {

  }

  public static byte[] inputStreamToByteArray(final InputStream is) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = is.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    return buffer.toByteArray();
  }

  private static int atoi(final byte[] data, int offset) {
    int res = 0;
    int i = offset;
    while (i < offset + 10) {
      char c = (char)data[i];
      if ((c < '0') || (c > '9')) {
        break;
      }
      res = res * 10 + (c - '0');
      i++;
    }
    return res;
  }

  private void readPGMArray(final InputStream inputStream, final Consumer<String> statusUpdated) {
    try {
      byte[] data = inputStreamToByteArray(inputStream);
      int offset = 0;

      if (data[0] != (byte)('P')) {
        throw new UnsupportedMediaException();
      }

      if ((data[1] != (byte)('5')) && (data[1] != (byte)('6'))) {
        throw new UnsupportedMediaException();
      }
      offset += 3;

      Integer cols = atoi(data, offset);
      offset += cols.toString().length() + 1;
      final Integer rows = atoi(data, offset);
      offset += rows.toString().length() + 1;

      int w = cols;
      int h = rows;

      statusUpdated.accept("Dimensions: " + w + "x" + h);
      Integer maxValue = atoi(data, offset);
      statusUpdated.accept("Max value: " + maxValue);
      offset += maxValue.toString().length() + 1;

      if (data[1] == (byte)('6')) {
        statusUpdated.accept("Processing coloured PGM");
        lastProcessedImageIsColouredImage = true;
        cols *= 3;
      } else {
        statusUpdated.accept("Processing monochrome PGM (not debayered)");
        lastProcessedImageIsColouredImage = false;
      }

      pgmPixels = new int[rows][cols];

      statusUpdated.accept("Array: " + cols + "x" + rows);
      doubleImage = new DoubleImage(w, h, getDefaultValues());
      statusUpdated.accept("Reading inputStream image of size " + w + " by " + h);

      int pixelH, pixelL;

      for (int r=0; r<rows; r++) {
        for (int c = 0; c < cols; c++) {
          pixelH = data[offset] & 0xFF;
          pixelL = data[offset + 1] & 0xFF;
          pgmPixels[r][c] = pixelH << 8 | pixelL;
          offset += 2;
        }
      }
      inputStream.close();
    } catch(FileNotFoundException fe) {
      statusUpdated.accept("Had a problem opening a inputStream.");
    } catch (Exception e) {
      statusUpdated.accept(e.toString() + " caught inputStream readPPM.");
      e.printStackTrace();
    }
  }

  public static DoubleImage loadPicture(final String filename, final Consumer<String> statusUpdated) {
    try {
      InputStream stream = new FileInputStream(filename);
      statusUpdated.accept("Trying to open file " + filename);
      return loadPictureFromInputStream(stream, statusUpdated);
    } catch (Exception ex) {
      System.out.println("Had a problem opening a inputStream.");
      return null;
    }
  }

  public static DoubleImage loadPictureFromInputStream
      (final InputStream stream, final Consumer<String> statusUpdated) {

    PGMImageColoured pgmImageWithDebayering = new PGMImageColoured();
    //load from file to memory
    pgmImageWithDebayering.readPGMArray(stream, statusUpdated);
    statusUpdated.accept("PGM read");

    //calculate minmaxes for to-double conversion
    pgmImageWithDebayering.calculateMinMax();
    statusUpdated.accept("MinMax OK");

    if (lastProcessedImageIsColouredImage) {
      //no need to colorize, just convert pixels
      pgmImageWithDebayering.copyPixelsToDoublePixels();
    } else {
      //colorize bayer array
      pgmImageWithDebayering.colorizeBayerPixelsToDoublePixels();
      statusUpdated.accept("Colorizing OK");
    }

    DoubleImage image = pgmImageWithDebayering.doubleImage;
    statusUpdated.accept("Converting OK");

    return image;
  }

  public static ByteImage loadPreview(String filename) {
    try {
      final InputStream is = openDCRawAsJpegPreviewExtractor(filename);
      return JpegImage.loadPreview(is);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public static DoubleImageDefaultValues getDefaultValues() {
    DoubleImageDefaultValues values = new DoubleImageDefaultValues();
    values.setrK(1.0);
    values.setgK(1.0);
    values.setbK(1.0);
    values.setGamma(2.2222);
    values.setExposure(0);
    values.setBrigthness(0);
    values.setContrast(1);
    //values.setShouldAutoAdjust(!lastProcessedImageIsColouredImage);
    values.setShouldAutoAdjust(true);
    return values;
  }
}
