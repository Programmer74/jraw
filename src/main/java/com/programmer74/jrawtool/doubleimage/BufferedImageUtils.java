package com.programmer74.jrawtool.doubleimage;

import com.programmer74.jrawtool.components.PaintableImage;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedImageUtils {

  private static double[] getPixel(BufferedImage image, int x, int y) {
    int rgb = image.getRGB(x, y);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = (rgb & 0xFF);

    return new double[] { r * 1.0 / 255.0, g * 1.0 / 255.0, b * 1.0 / 255.0 };
  }

  private static void setPixel(BufferedImage image, int x, int y, double[] pixel) {
    int r = (int)(Math.round(pixel[0] * 255.0));
    int g = (int)(Math.round(pixel[1] * 255.0));
    int b = (int)(Math.round(pixel[2] * 255.0));

    if (r < 0) r = 0;
    if (g < 0) g = 0;
    if (b < 0) b = 0;

    if (r > 255) r = 255;
    if (g > 255) g = 255;
    if (b > 255) b = 255;

    int rgbcolor = 0xff000000 | r << 16 | g << 8 | b;
    image.setRGB(x, y, rgbcolor);
  }

  public static BufferedImage getShrinkedImage(BufferedImage originalImage, int maxw) {

    int width = originalImage.getWidth();
    int height = originalImage.getHeight();

    if ((width > maxw) || (height > maxw)) {
      double k = width * 1.0 / height;
      if (width > height) {
        width = maxw;
        height = (int)(width * 1.0 / k);
      } else {
        height = maxw;
        width = (int)(height * k);
      }
    }

    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = newImage.getGraphics();
    g.drawImage(originalImage, 0, 0, width, height, null);
    return newImage;
  }

  public static void applyConvolution(
      BufferedImage orgnl,
      BufferedImage applyTo,
      final int x, final int y,
      final double[][] convMatrix,
      final double strength) {
    int a = convMatrix[0].length;
    int width = orgnl.getWidth();
    int height = orgnl.getHeight();
    double[] pixel = getPixel(orgnl, x, y);
    for (int colorIndex = 0; colorIndex <= 2; colorIndex++) {
      double acc = 0;
      for (int dx = 0; dx < a; dx++) {
        for (int dy = 0; dy < a; dy++) {
          int sourceX = x - a / 2 + dx;
          int sourceY = y - a / 2 + dy;
          if (sourceX < 0) sourceX = 0;
          if (sourceX >= width) sourceX = width - 1;
          if (sourceY < 0) sourceY = 0;
          if (sourceY >= height) sourceY = height - 1;

          acc += getPixel(orgnl, sourceX, sourceY)[colorIndex] * convMatrix[dx][dy];
        }
      }
      pixel[colorIndex] = acc;
    }
    setPixel(applyTo, x, y, pixel);
  }

  public static void applyDilate(
      BufferedImage orgnl,
      BufferedImage applyTo,
      final int x, final int y,
      final int a) {
    int width = orgnl.getWidth();
    int height = orgnl.getHeight();
    double[] acc = getPixel(orgnl, x, y);
    for (int dx = 0; dx < a; dx++) {
      for (int dy = 0; dy < a; dy++) {
        int sourceX = x - a / 2 + dx;
        int sourceY = y - a / 2 + dy;
        if (sourceX < 0) sourceX = 0;
        if (sourceX >= width) sourceX = width - 1;
        if (sourceY < 0) sourceY = 0;
        if (sourceY >= height) sourceY = height - 1;

        double[] px = getPixel(orgnl, sourceX, sourceY);

        for (int colorIndex = 0; colorIndex <= 2; colorIndex++) {
          if (px[colorIndex] > acc[colorIndex]) {
            acc[colorIndex] = px[colorIndex];
          }
        }
      }
    }
    setPixel(applyTo, x, y, acc);
  }

  public static void applyDehue(
      BufferedImage orgnl,
      BufferedImage applyTo,
      BufferedImage mask,
      final int x, final int y) {


    double[] sourcePx = getPixel(orgnl, x, y);
    double[] maskPx = getPixel(mask, x, y);

    double[] sourcePxHsv = HSVRGBUtils.rgb2hsv(sourcePx);

    if (maskPx[1] > 0.5) {
      System.out.println("oh shit mask is green hue is " + sourcePxHsv[0]);
      if ((sourcePxHsv[0] < 50) || (sourcePxHsv[0] > 180)) {
        sourcePxHsv[1] = 0;
      }
    }

    double[] destPx = HSVRGBUtils.hsv2rgb(sourcePxHsv);

    setPixel(applyTo, x, y, destPx);
  }

  public static BufferedImage convolve(BufferedImage originalImage, double[][] convmatrix) {
    BufferedImage image = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        applyConvolution(originalImage, image, x, y, convmatrix, 1.0);
      }
    }
    return image;
  }

  public static BufferedImage dilate(BufferedImage originalImage, int a) {
    BufferedImage image = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        applyDilate(originalImage, image, x, y, a);
      }
    }
    return image;
  }

  public static BufferedImage dehue(BufferedImage originalImage, BufferedImage mask) {
    BufferedImage image = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        applyDehue(originalImage, image, mask, x, y);
      }
    }
    return image;
  }

  public static BufferedImage clearCopy(BufferedImage originalImage) {
    BufferedImage image = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
//        setPixel(image, x, y, getPixel(originalImage, x, y));
        image.setRGB(x, y, originalImage.getRGB(x, y));
      }
    }
    return image;
  }

  public static BufferedImage binarizeGPriority(BufferedImage originalImage, double rmax, double gmax, double bmax) {
    BufferedImage image = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        double[] pixel = getPixel(originalImage, x, y);
        if ((pixel[0] > pixel[1]) || ((pixel[2] > pixel[1])) ||
            ((pixel[0] + pixel[2]) > 1.5 * pixel[1])) {
          pixel[0] = 0;
          pixel[1] = 0;
          pixel[2] = 0;
        } else{
          pixel[0] = pixel[0] > rmax ? 1.0 : 0.0;
          pixel[1] = pixel[1] > gmax ? 1.0 : 0.0;
          pixel[2] = pixel[2] > bmax ? 1.0 : 0.0;
        }
        setPixel(image, x, y, pixel);
      }
    }
    return image;
  }

  public static BufferedImage rotate(BufferedImage originalImage, double angle) {
    int i, j, x, y, new_x, new_y;
    double v_sin, v_cos;
//    double[] neutral = new double[]{255, 255, 255};
    int neutral = 0x00ffffff;

    if(angle < -90 || 90 < angle) {
      return originalImage;
    }
    angle = angle * Math.PI / 180.0f;
    v_cos = Math.cos(angle);
    v_sin = Math.sin(angle);

    int width = originalImage.getWidth();
    int height = originalImage.getHeight();

    int newWidth = (int)Math.max(width * v_cos - height * v_sin, width * v_cos + height * v_sin);
    int newHeight = (int)Math.max(width * v_sin + height * v_cos, -width * v_sin + height * v_cos);
    BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

    for (i = 0; i < newWidth; i++) {
      for (j = 0; j < newHeight; j++) {
        x = i - newWidth / 2;
        y = j - newHeight / 2;
        new_x = (int)(((x * v_cos - y * v_sin)) + width / 2);
        new_y = (int)(((x * v_sin + y * v_cos)) + height / 2);
        if ((new_x >= width) || (new_y >= height) || (new_x < 0) || (new_y < 0)) {
          result.setRGB(i, j, neutral);
        } else {
          result.setRGB(i, j, originalImage.getRGB(new_x, new_y));
        }
      }
    }
    return result;
  }
}
