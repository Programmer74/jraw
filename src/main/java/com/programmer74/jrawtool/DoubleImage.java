package com.programmer74.jrawtool;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
  private BufferedImage bufferedImagePreviewFast;
  private BufferedImage bufferedImagePreviewSlow;

  private boolean isDirty = true;
  private boolean isFastPreviewDirty = true;
  public boolean isSlowPreviewDirty = true;
  private boolean slowPreviewReady = false;

  private Consumer<Integer> afterChunkPaintedCallback;
  private final int nThreads = 2;
  private ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

  private DoubleImageCropParams prevParams = new DoubleImageCropParams(0,0,0,0);


  public DoubleImage(final int width, final int height) {
    this.width = width;
    this.height = height;
    this.pixels = new double[width][height][3];
    this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    this.bufferedImagePreviewFast = new BufferedImage(width / 6, height / 6, BufferedImage.TYPE_INT_RGB);
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
    isSlowPreviewDirty = true;
    isFastPreviewDirty = true;
    isDirty = true;
  }

  public void paintOnBufferedImage(BufferedImage image) {
    paintOnBufferedImage(image, 0, 0, width, height, false);
  }

  public void paintOnBufferedImage(final BufferedImage image,
                                   final int offsetX, final int offsetY, final int width, final int height, final boolean shouldStop) {
   for (int x = offsetX; x < offsetX + width; x++) {
      for (int y = offsetY; y < offsetY + height; y++) {
        if (shouldStop) {
          System.out.println("Requested to stop at " + x + ":" + y);
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

  public void paintSlowPreviewOnBufferedImage(final BufferedImage image,
                                   final int offsetX, final int offsetY, final int width, final int height) {
    for (int x = offsetX; x < offsetX + width; x++) {
      for (int y = offsetY; y < offsetY + height; y++) {
        if (isSlowPreviewDirty) {
          System.out.println("Requested to stop at " + x + ":" + y);
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
      if ((x % 100) == 0) System.out.println(x);
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

  public BufferedImage getBufferedImagePreview(
      int paintX, int paintY, int paintW, int paintH,
      int windowWidth, int windowHeight) {

    Double lx = (-paintX) * 1.0 / paintW * width;
    Double rx = (getWidth() - paintX) * 1.0 / paintW * width;
    Double ly = (-paintY) * 1.0 / paintH * height;
    Double ry = (getHeight() - paintY) * 1.0 / paintH * height;

    if (lx < 0) lx = 0.0;
    if (ly < 0) ly = 0.0;
    if (rx > width) rx = width * 1.0;
    if (ry > height) ry = height * 1.0;

    if ((bufferedImagePreviewSlow == null) ||
        (bufferedImagePreviewSlow.getWidth() != windowWidth) ||
        (bufferedImagePreviewSlow.getHeight() != windowHeight)) {
      System.out.println("New preview size: " + windowWidth + ":" + windowHeight);
//        isSlowPreviewDirty = true;
    }
    if (isSlowPreviewDirty) {
        System.out.println("scheduling painting slow preview");
        schedulePaintingFullBufferedImage(lx.intValue(), ly.intValue(), rx.intValue(), ry.intValue());
    }
    if (isFastPreviewDirty) {
      isFastPreviewDirty = false;
      paintOnBufferedImageForPreview(bufferedImagePreviewFast, 0, 0, width, height);
    }
    if (slowPreviewReady) {
      System.out.println("slow preview ready");
      return bufferedImagePreviewSlow;
    }
    System.out.println("slow preview NOT READY");
    return bufferedImagePreviewFast;
  }

  public void subscribeToAfterChunkPaintedCallback(Consumer<Integer> callback) {
    this.afterChunkPaintedCallback = callback;
  }

  private CountDownLatch latch;
  private void scheduleExecutor (final int lx, final int ly, final int rx, final int ry) {
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        System.out.println("slow preview: begin painting");
        System.out.println("lx " + lx + " ly " + ly + " rx " + rx + " ry " + ry);
//        paintOnBufferedImage(bufferedImagePreviewSlow, lx, ly, rx - lx, ry - ly, isSlowPreviewDirty);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        paintSlowPreviewOnBufferedImage(img, lx, ly, rx - lx, ry - ly);
        System.out.println("slow preview: done or cancelled painting");
        latch.countDown();
        if (!isSlowPreviewDirty) {
          System.out.println("slow preview: setting image");
          bufferedImagePreviewSlow = img;
          System.out.println("slow preview: setting image done");
          slowPreviewReady = true;
          System.out.println("slow preview: ready");
          afterChunkPaintedCallback.accept(0);
        } else {
          System.out.println("slow preview: for some reason isSlowPreviewDirty is true");
          schedulePaintingFullBufferedImage(lx, ly, rx, ry);
        }
      }
    });
  }
  public void schedulePaintingFullBufferedImage(int lx, int ly, int rx, int ry) {
    if (isSlowPreviewDirty) {

      if (latch != null) {
        if (latch.getCount() != 0) {
          return;
        }
      }

      latch = new CountDownLatch(1);
      isSlowPreviewDirty = false;
      slowPreviewReady = false;
      int w = 500;
      int h = 500;
      int x = 0;
      int y = 0;

      System.out.println("slow preview beginning...");

//      for (x = 0; x < width; x += w) {
//        for (y = 0; y < height; y += h) {
//          if (x + w > width) x = width - w;
//          if (y + h > height) y = height - h;
//          scheduleExecutor(x, y, w, h);
//        }
//      }
      scheduleExecutor(lx, ly, rx, ry);
    }
  }

  public void setWhiteBalance(double rK, double gK, double bK) {
    this.rWB = rK;
    this.gWB = gK;
    this.bWB = bK;
    markDirty();
  }
}
