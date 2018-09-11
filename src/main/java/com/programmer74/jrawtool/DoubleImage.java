package com.programmer74.jrawtool;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DoubleImage {
  //x,y,{r,g,bufferedImage}
  private double[][][] pixels;

  private int width;
  private int height;

  private double gGamma = 1 / 2.2222;
  private double gA = 1.0;

  private double rWB = 2.170906;
  private double gWB = 1.0000;
  private double bWB = 1.163172;

  private double brightness = 1.0;
  private int exposureStop = 0;

  private BufferedImage bufferedImage;
  private BufferedImage bufferedImagePreview;

  private boolean isDirty = true;
  private boolean isPreviewDirty = true;
  private DoubleImageCropParams prevCropParams = new DoubleImageCropParams(0, 0, 0, 0);

  private Consumer<Integer> afterChunkPaintedCallback;
  private final int nThreads = 2;
  private ExecutorService executorService = Executors.newFixedThreadPool(nThreads);


  public DoubleImage(final int width, final int height) {
    this.width = width;
    this.height = height;
    this.pixels = new double[width][height][3];
    this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public double[][][] getPixels() {
    return pixels;
  }

  public void setPixel(int x, int y, double r, double g, double b) {
    pixels[x][y][0] = r;
    pixels[x][y][1] = g;
    pixels[x][y][2] = b;
  }

  private int doubleValueToUint8T(Double value) {
    value = value * 255.0;
    //    System.out.println(value);
    int intval = value.intValue();
    if (intval > 255) intval = 255;
    if (intval < 0) intval = 0;
    return intval;
  }

  private double calculateGammaCorrection(double input, double A, double gamma) {
    return A * Math.pow(input, gamma);
  }

  private double calculateExposureCorrection(double input, int stop) {
    return input * Math.pow(2, stop);
  }

  private void adjustGamma(double[] pixel) {
    for (int i = 0; i < 3; i++) {
      pixel[i] = calculateGammaCorrection(pixel[i], gA, gGamma);
    }
  }

  private void adjustWhiteBalance(double[] pixel) {
    pixel[0] = pixel[0] * rWB;
    pixel[1] = pixel[1] * gWB;
    pixel[2] = pixel[2] * bWB;
  }

  private void adjustExposure(double[] pixel) {
    for (int i = 0; i < 3; i++) {
      pixel[i] = calculateExposureCorrection(pixel[i], exposureStop);
    }
  }

  private void markDirty() {
    isPreviewDirty = true;
    isDirty = true;
  }

  public void paintOnBufferedImage(BufferedImage image) {
    paintOnBufferedImage(image, 0, 0, width, height, false);
  }

  public void paintOnBufferedImage(BufferedImage image,
                                   int offsetX, int offsetY, int width, int height, boolean shouldStop) {
    for (int x = offsetX; x < offsetX + width; x++) {
      for (int y = offsetY; y < offsetY + height; y++) {

        if (shouldStop) {
//          System.out.println("Requested to stop at " + x + ":" + y);
          return;
        }
        double[] pixel = pixels[x][y].clone();

        adjustWhiteBalance(pixel);
        adjustGamma(pixel);
        adjustExposure(pixel);

        int r = doubleValueToUint8T(pixel[0]);
        int g = doubleValueToUint8T(pixel[1]);
        int b = doubleValueToUint8T(pixel[2]);

        int rgbcolor = 0xff000000 | r << 16 | g << 8 | b;
        image.setRGB(x, y, rgbcolor);
      }
    }
  }

  public void paintOnBufferedImageForPreview(BufferedImage image, int lx, int ly, int rx, int ry) {

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {

        int sx = x * (rx - lx) / image.getWidth() + lx;
        int sy = y * (ry - ly) / image.getHeight() + ly;

        if (sx >= width) sx = width - 1;
        if (sy >= height) sy = height - 1;

        double[] pixel = pixels[sx][sy].clone();

        adjustWhiteBalance(pixel);
        adjustGamma(pixel);
        adjustExposure(pixel);

        int r = doubleValueToUint8T(pixel[0]);
        int g = doubleValueToUint8T(pixel[1]);
        int b = doubleValueToUint8T(pixel[2]);

        int rgbcolor = 0xff000000 | r << 16 | g << 8 | b;
        image.setRGB(x, y, rgbcolor);
      }
    }
  }

  public BufferedImage getBufferedImage() {
    if (isDirty) {
      paintOnBufferedImage(bufferedImage);
      isDirty = false;
    }
    return bufferedImage;
  }

  public BufferedImage getBufferedImagePreview(DoubleImageCropParams p, int width, int height) {

    System.out.println("lx " + p.lx + " ly " + p.ly + " rx " + p.rx + " ry " + p.ry);

    if ((bufferedImagePreview == null) ||
      ((bufferedImagePreview.getWidth() != width) || (bufferedImagePreview.getHeight() != height))) {
      System.out.println("New preview size: " + width + ":" + height);
        bufferedImagePreview = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        isPreviewDirty = true;
    }
    if (isPreviewDirty || p.shouldRepaintImage(prevCropParams)) {
        prevCropParams = p;
        isPreviewDirty = false;
        paintOnBufferedImageForPreview(bufferedImagePreview, p.lx, p.ly, p.rx, p.ry);

    }
    return bufferedImagePreview;
  }

  public void subscribeToAfterChunkPaintedCallback(Consumer<Integer> callback) {
    this.afterChunkPaintedCallback = callback;
  }

  private CountDownLatch latch = null;
  private void scheduleExecutor (final int x, final int y, final int w, final int h) {
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        paintOnBufferedImage(bufferedImage, x, y, w, h, isDirty);
        if (!isDirty) {
          afterChunkPaintedCallback.accept(0);
        }
        latch.countDown();
      }
    });
  }
  public BufferedImage getBufferedImageAsync() {
    if (isDirty) {

      if (latch != null) {
        try {
          latch.await(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      latch = new CountDownLatch(nThreads);

      isDirty = false;
      int w = 500;
      int h = 500;
      int x = 0;
      int y = 0;

      for (x = 0; x < width; x += w) {
        for (y = 0; y < height; y += h) {
          if (x + w > width) x = width - w;
          if (y + h > height) y = height - h;
          scheduleExecutor(x, y, w, h);
        }
      }
    }
    return bufferedImage;
  }

  public void setWhiteBalance(double rK, double gK, double bK) {
    this.rWB = rK;
    this.gWB = gK;
    this.bWB = bK;
    markDirty();
  }
}
