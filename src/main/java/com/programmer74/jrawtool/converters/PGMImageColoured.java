package com.programmer74.jrawtool.converters;

import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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

    System.out.println(min + ":" + max);

    Double x = (Math.log(max) / Math.log(2));
    int bitness = x.intValue() + 1;

    System.out.println("Raw bitness: " + bitness);

    max = (int)Math.pow(2, bitness);
    System.out.println(max);
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

  private void readPGMArray(String filename){
    try {
      FileReader reader = new FileReader(filename);
      Scanner infile = new Scanner(reader);

      Path path = Paths.get(filename);
      byte[] data = Files.readAllBytes(path);

      // process the top 4 header lines
      String filetype=infile.nextLine();

      System.out.println("File type: " + filetype);

      //infile.nextLine();
      int cols = infile.nextInt();
      int rows = infile.nextInt();

      int w = cols;
      int h = rows;

      System.out.println("Dimensions: " + w + "x" + h);
      int maxValue = infile.nextInt();
      System.out.println("Max value: " + maxValue);

      if (filetype.equals("P6")) {
        System.out.println("Processing coloured PGM");
        lastProcessedImageIsColouredImage = true;
        cols *= 3;
      } else if (filetype.equals("P5")) {
        System.out.println("Processing monochrome PGM (not debayered)");
        lastProcessedImageIsColouredImage = false;
      }

      pgmPixels = new int[rows][cols];

      System.out.println("Array: " + cols + "x" + rows);
      doubleImage = new DoubleImage(w, h, getDefaultValues());
      System.out.println("Reading in image from " + filename + " of size " + w + " by " + h);
      // process the rest lines that hold the actual pixel values

      String header = filetype + "\n" + h + " " + w + "\n" + maxValue + "\n";
      int offset = header.length();

      int pixelH, pixelL;

      for (int r=0; r<rows; r++)
        for (int c=0; c<cols; c++) {
          pixelH = data[offset] & 0xFF;
          pixelL = data[offset + 1] & 0xFF;
          pgmPixels[r][c] = pixelH << 8 | pixelL;
          offset += 2;
        }
      infile.close();
    } catch(FileNotFoundException fe) {
      System.out.println("Had a problem opening a file.");
    } catch (Exception e) {
      System.out.println(e.toString() + " caught in readPPM.");
      e.printStackTrace();
    }
  }

  public static DoubleImage loadPicture(String filename) {

    PGMImageColoured pgmImageWithDebayering = new PGMImageColoured();
    //load from file to memory
    pgmImageWithDebayering.readPGMArray(filename);
    System.out.println("PGM read");

    //calculate minmaxes for to-double conversion
    pgmImageWithDebayering.calculateMinMax();
    System.out.println("MinMax OK");

    if (lastProcessedImageIsColouredImage) {
      //no need to colorize, just convert pixels
      pgmImageWithDebayering.copyPixelsToDoublePixels();
    } else {
      //colorize bayer array
      pgmImageWithDebayering.colorizeBayerPixelsToDoublePixels();
      System.out.println("Colorizing OK");
    }

    DoubleImage image = pgmImageWithDebayering.doubleImage;
    System.out.println("Converting OK");

    return image;
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
