package com.programmer74.jrawtool;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main extends Component {
  // image buffer for graphical display
  private BufferedImage img;
  // image buffer for plain gray-scale pixel values
  private int[][] pixels;

  int min = 65535;
  int max = 0;

  private Double getValAt(int row, int col, char color) {
    Double val = pixels[row][col] * 255.0 / (max - min);
    if (color == 'r') val *= 2.008508;
    if (color == 'g') val *= 0.925194;
    if (color == 'b') val *= 1.076160;
    return val;
  }

  private int interpolate(Double ... values) {
    Double sum = 0D;
    int count = 0;
    for (Double value : values) {
      sum += value;
      count++;
    }
    sum = sum / count;
    if (sum.intValue() > 255) sum = 255D;
    if (sum.intValue() < 0) sum = 0D;
    return sum.intValue();
  }

  // translating raw gray scale pixel values to buffered image for display
  private void pix2img(){
    int pixel;
    img = new BufferedImage( pixels[0].length, pixels.length, BufferedImage.TYPE_INT_ARGB );

    //find min and max values


    for(int row=0; row<pixels.length; ++row)
      for(int col=0; col<pixels[row].length; ++col){
        pixel = pixels[row][col];
        if (pixel < min) min = pixel;
        if (pixel > max) max = pixel;
      }

    System.out.println(min + ":" + max);

    max = max / 2;

    // copy the pixels values
    for(int row=1; row<pixels.length - 1; ++row)
      for(int col=1; col<pixels[row].length - 1; ++col){

        int r = 0, g = 0, b = 0;

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

        int rgbcolor = 0xff000000 | r << 16 | g << 8 | b;
        img.setRGB(col, row, rgbcolor);
      }
  }

  // constructor that loads pgm image from a file
  public Main(String filename) {
    pixels = null;
    readPGM(filename);
    if (pixels != null)
      pix2img();
  }

  // load gray scale pixel values from a PGM format image
  public void readPGM(String filename){
    try {
      FileReader reader = new FileReader(filename);
      Scanner infile = new Scanner(reader);

      Path path = Paths.get(filename);
      byte[] data = Files.readAllBytes(path);

      // process the top 4 header lines
      String filetype=infile.nextLine();
//      if (!filetype.equalsIgnoreCase("p2")) {
//        System.out.println("[readPGM]Cannot load the image type of "+filetype);
//        return;
//      }
      System.out.println("File type: " + filetype);
      //infile.nextLine();
      int cols = infile.nextInt();
      int rows = infile.nextInt();
      System.out.println("Dimensions: " + cols + "x" + rows);
      int maxValue = infile.nextInt();
      System.out.println("Max value: " + maxValue);
      pixels = new int[rows][cols];
      System.out.println("Reading in image from " + filename + " of size " + rows + " by " + cols);
      // process the rest lines that hold the actual pixel values

      String header = filetype + "\n" + rows + " " + cols + "\n" + maxValue + "\n";
      int offset = header.length();

      int pixelH, pixelL;

      for (int r=0; r<rows; r++)
        for (int c=0; c<cols; c++) {
          pixelH = data[offset] & 0xFF;
          pixelL = data[offset + 1] & 0xFF;
          pixels[r][c] = pixelH << 8 | pixelL;
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
  // overrides the paint method of Component class
  public void paint(Graphics g) {
    // simply draw the buffered image
    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
  }
  // overrides the method in Component class, to determine the window size
  public Dimension getPreferredSize() {
    if (img == null) {
      return new Dimension(100, 100);
    } else {
      // make sure the window is not two small to be seen
      return new Dimension(Math.max(100, img.getWidth(null)),
          Math.max(100, img.getHeight(null)));
    }
  }


  //./dcraw -4 -D -v -c DSC_1801.NEF > file

  // The main method that will load and process a pgm image, and display the result.
  public static void main(String[] args) {
    // instantiate the PgmImage object according to the
    //  command line argument
    Main img;
    String filename ="default";
    String operation = "none";
   
      filename = args[0];
      img = new Main(filename);

    // set up the GUI for display the PgmImage object
    JFrame f = new JFrame("PGM Image: "+filename+" Image Operation: " + operation);
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.add(img);
    f.pack();
    f.setVisible(true);
  }
}