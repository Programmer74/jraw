package com.programmer74.jrawtool;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class PGMImage {

  // image buffer for graphical display
  private BufferedImage img;
  // image buffer for plain gray-scale pixel values
  private int[][] bayerPixels;

  // image buffer for doubles
  private double[][][] colouredPixelsRGB;

  int min = 65535;
  int max = 0;

  double gamma = 1.0;

  private double getValAt(int row, int col, char color) {
    Double val = bayerPixels[row][col] * gamma;
    //System.out.println(val);
    return val;
  }

  private double interpolate(Double ... values) {
    Double sum = 0D;
    int count = 0;
    for (Double value : values) {
      sum += value;
      count++;
    }
    sum = sum / count;
    return sum.intValue();
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
  }

  private int doublePixelToUint8(Double value) {
    value = value * 255.0 / (max - min) * gamma;
//    System.out.println(value);
    int intval = value.intValue();
    if (intval > 255) intval = 255;
    if (intval < 0) intval = 0;
    return intval;
  }

  private void adjustWhiteBalanceOnDoublePixels(double rK, double gK, double bK) {
    for(int row=1; row<bayerPixels.length - 1; ++row)
      for(int col=1; col<bayerPixels[row].length - 1; ++col) {
        colouredPixelsRGB[row][col][0] = colouredPixelsRGB[row][col][0] * rK;
        colouredPixelsRGB[row][col][1] = colouredPixelsRGB[row][col][1] * gK;
        colouredPixelsRGB[row][col][2] = colouredPixelsRGB[row][col][2] * bK;
      }
  }

  private void convertDoublePixelsToBufferedImage() {
    img = new BufferedImage( bayerPixels[0].length, bayerPixels.length, BufferedImage.TYPE_INT_ARGB );

    for(int row=1; row<bayerPixels.length - 1; ++row)
      for(int col=1; col<bayerPixels[row].length - 1; ++col) {

        int r = doublePixelToUint8(colouredPixelsRGB[row][col][0]);
        int g = doublePixelToUint8(colouredPixelsRGB[row][col][1]);
        int b = doublePixelToUint8(colouredPixelsRGB[row][col][2]);

        int rgbcolor = 0xff000000 | r << 16 | g << 8 | b;
        img.setRGB(col, row, rgbcolor);
      }
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
          g = interpolate(getValAt(row, col, 'g'));
          r = interpolate(getValAt(row + 1, col, 'r'), getValAt(row - 1, col, 'r'));
          b = interpolate(getValAt(row, col + 1, 'b'), getValAt(row, col - 1, 'b'));
        } else if ((row % 2 == 0) && (col % 2 != 0)) {
          //B pixel
          b = interpolate(getValAt(row, col, 'b'));
          r = interpolate(getValAt(row + 1, col - 1, 'r'));
          g = interpolate(getValAt(row, col - 1, 'g'),
              getValAt(row, col + 1, 'g'),
              getValAt(row - 1, col, 'g'),
              getValAt(row + 1, col, 'g'));
        } else if ((row % 2 != 0) && (col % 2 == 0)) {
          //R pixel
          r = interpolate(getValAt(row, col, 'r'));
          b = interpolate(getValAt(row - 1, col + 1, 'b'));
          g = interpolate(getValAt(row, col - 1, 'g'),
              getValAt(row, col + 1, 'g'),
              getValAt(row - 1, col, 'g'),
              getValAt(row + 1, col, 'g'));
        } else if ((row % 2 != 0) && (col % 2 != 0)) {
          //second G pixel
          g = interpolate(getValAt(row, col, 'g'));
          r = interpolate(getValAt(row, col - 1, 'r'), getValAt(row, col + 1, 'r'));
          b = interpolate(getValAt(row - 1, col, 'b'), getValAt(row + 1, col, 'b'));
        }

        colouredPixelsRGB[row][col][0] = r;
        colouredPixelsRGB[row][col][1] = g;
        colouredPixelsRGB[row][col][2] = b;
      }
  }

  public PGMImage() {

  }

  public void readPGMArray(String filename){
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
      colouredPixelsRGB = new double[rows][cols][3];
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

  public void loadPicture(String filename) {
    bayerPixels = null;

    //read pgm array from file
    readPGMArray(filename);
    System.out.println("PGM read");

    //calculate minmaxes for exposure correction
    calculateMinMax();
    System.out.println("MinMax OK");

    //colorize bayer array
    colorizeBayerPixelsToDoublePixels();
    System.out.println("Colorizing OK");

    //adjust white balance
    adjustWhiteBalanceOnDoublePixels(2.008508, 0.925194, 1.07610);
    System.out.println("White Balance OK");

    //convert to buffered image
    convertDoublePixelsToBufferedImage();
    System.out.println("Converting OK");

  }

  public BufferedImage getImg() {
    return img;
  }

  public void changeGamma(double gamma) {
    this.gamma = gamma;
    convertDoublePixelsToBufferedImage();
    System.out.println("Converting OK");
  }
}
