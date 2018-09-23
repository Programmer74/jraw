package com.programmer74.jrawtool.converters;

import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class PGMImage {

  // image buffer for plain gray-scale pixel values
  private int[][] bayerPixels;

  private DoubleImage doubleImage;

  private int min = 65535;
  private int max = 0;

  private double getValAt(int row, int col) {
    return bayerPixels[row][col] * 1.0;
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

    for(int row=0; row<bayerPixels.length; ++row)
      for(int col=0; col<bayerPixels[row].length; ++col){
        pixel = bayerPixels[row][col];
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

  // translating raw gray scale pixel values to buffered image for display
  private void colorizeBayerPixelsToDoublePixels(){

    // copy the pixels values
    for(int row=1; row<bayerPixels.length - 1; ++row)
      for(int col=1; col<bayerPixels[row].length - 1; ++col){

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

  private PGMImage() {

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
      System.out.println("Dimensions: " + cols + "x" + rows);
      int maxValue = infile.nextInt();
      System.out.println("Max value: " + maxValue);
      bayerPixels = new int[rows][cols];
      doubleImage = new DoubleImage(cols, rows, getDefaultValues());
      System.out.println("Reading in image from " + filename + " of size " + rows + " by " + cols);
      // process the rest lines that hold the actual pixel values

      String header = filetype + "\n" + rows + " " + cols + "\n" + maxValue + "\n";
      int offset = header.length();

      int pixelH, pixelL;

      for (int r=0; r<rows; r++)
        for (int c=0; c<cols; c++) {
          pixelH = data[offset] & 0xFF;
          pixelL = data[offset + 1] & 0xFF;
          bayerPixels[r][c] = pixelH << 8 | pixelL;
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

    PGMImage pgmImage = new PGMImage();
    //load from file to memory
    pgmImage.readPGMArray(filename);
    System.out.println("PGM read");

    //calculate minmaxes for to-double conversion
    pgmImage.calculateMinMax();
    System.out.println("MinMax OK");

    //colorize bayer array
    pgmImage.colorizeBayerPixelsToDoublePixels();
    System.out.println("Colorizing OK");

    DoubleImage image = pgmImage.doubleImage;
//    image.getBufferedImage();
    System.out.println("Converting OK");

    return image;
  }

  public static DoubleImageDefaultValues getDefaultValues() {
    DoubleImageDefaultValues values = new DoubleImageDefaultValues();
    values.setrK(2.17);
    values.setgK(1.0);
    values.setbK(1.163);
    values.setGamma(2.2222);
    values.setExposure(0);
    values.setBrigthness(0);
    values.setContrast(1);
    return values;
  }
}
