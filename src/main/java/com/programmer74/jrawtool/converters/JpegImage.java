package com.programmer74.jrawtool.converters;

import com.programmer74.jrawtool.byteimage.ByteImage;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Consumer;

public class JpegImage {

  private DoubleImage doubleImage;
  private BufferedImage bufferedImage;

  private final int max = 255;

  // translating raw gray scale pixel values to buffered image for display
  private void convertJpegPixelsToDoublePixels(){

    // copy the pixels values
    for(int x=0; x<bufferedImage.getWidth(); ++x)
      for(int y=0; y<bufferedImage.getHeight(); ++y){

        int r = 0, g = 0, b = 0;
        int color = bufferedImage.getRGB(x, y);

        r = (color >> 16) & 0xff;
        g = (color >> 8) & 0xff;
        b = (color) & 0xff;

//        System.out.println(r + ":" + g + ":" + b);

        doubleImage.setPixel(x, y, r * 1.0 / max, g * 1.0 / max, b * 1.0 / max);
      }
  }

  private JpegImage() {

  }

  private void readBufferedImage(final String filename) {
    try {

      ImageInputStream iis = ImageIO.createImageInputStream(new FileInputStream(filename));
      bufferedImage = ImageIO.read(iis);
      doubleImage = new DoubleImage(bufferedImage.getWidth(), bufferedImage.getHeight(), getDefaultValues());
      System.out.println("Reading in image from " + filename +
          " of size " + bufferedImage.getWidth() + " by " + bufferedImage.getHeight());

    } catch(FileNotFoundException fe) {
      System.out.println("Had a problem opening a file.");
    } catch (Exception e) {
      System.out.println(e.toString() + " caught in readBufferedImage");
      e.printStackTrace();
    }
  }

  public static DoubleImage loadPicture(final String filename, final Consumer<String> statusUpdated) {
    JpegImage jpegImage = new JpegImage();
    //load from file to memory
    jpegImage.readBufferedImage(filename);
    statusUpdated.accept("JPEG read");

    jpegImage.convertJpegPixelsToDoublePixels();
    statusUpdated.accept("Converting OK");

    return jpegImage.doubleImage;
  }

  public static ByteImage loadPreview(final String filename) {
    try {
      ImageInputStream iis = ImageIO.createImageInputStream(new FileInputStream(filename));
      return new ByteImage(ImageIO.read(iis));
    } catch(FileNotFoundException fe) {
      System.out.println("Had a problem opening a file.");
    } catch (Exception e) {
      System.out.println(e.toString() + " caught in readBufferedImage");
      e.printStackTrace();
    }
    return null;
  }

  public static ByteImage loadPreview(final InputStream inputStream) {
    try {
      BufferedImage image = ImageIO.read(inputStream);
      inputStream.close();
      return new ByteImage(image);
    } catch(FileNotFoundException fe) {
      System.out.println("Had a problem opening a file.");
    } catch (Exception e) {
      System.out.println(e.toString() + " caught in readBufferedImage");
      e.printStackTrace();
    }
    return null;
  }

  public static DoubleImageDefaultValues getDefaultValues() {
    DoubleImageDefaultValues values = new DoubleImageDefaultValues();
    return values;
  }
}
