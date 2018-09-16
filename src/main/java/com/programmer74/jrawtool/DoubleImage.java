package com.programmer74.jrawtool;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DoubleImage {
  //x,y,{r,g,bufferedImage}
  protected double[][][] pixels;

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

  private boolean isDirty = true;
  private boolean isFastPreviewDirty = true;
  public boolean isSlowPreviewDirty = true;
  private boolean isSlowPreviewReady = false;

  protected Consumer<Integer> afterChunkPaintedCallback;
  private final int nThreads = 2;
  private ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

  private DoubleImageAsyncPreviewGenerator previewGenerator;

  private int paintX, paintY, paintW, paintH, windowWidth, windowHeight;

  public DoubleImage(final int width, final int height) {
    this.width = width;
    this.height = height;
    this.pixels = new double[width][height][3];
    this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    this.bufferedImagePreviewFast = new BufferedImage(width / 6, height / 6, BufferedImage.TYPE_INT_RGB);
    this.previewGenerator = new DoubleImageAsyncPreviewGenerator(this);
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

  protected int doubleValueToUint8T(Double value) {
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

  protected void adjustGamma(double[] pixel) {
    for (int i = 0; i < 3; i++) {
      pixel[i] = calculateGammaCorrection(pixel[i], gA, gGamma);
    }
  }

  protected void adjustWhiteBalance(double[] pixel) {
    pixel[0] = pixel[0] * rWB;
    pixel[1] = pixel[1] * gWB;
    pixel[2] = pixel[2] * bWB;
  }

  protected void adjustExposure(double[] pixel) {
    for (int i = 0; i < 3; i++) {
      pixel[i] = calculateExposureCorrection(pixel[i], exposureStop);
    }
  }

  private void markSlowPreviewDirty() {
    isSlowPreviewDirty = true;
  }

  private void markPreviewDirty() {
    markSlowPreviewDirty();
    isFastPreviewDirty = true;
  }

  private void markDirty() {
    markPreviewDirty();
    isDirty = true;
  }

  public void paintOnBufferedImage(BufferedImage image) {
    paintOnBufferedImage(image, 0, 0, width, height);
  }
  public void paintOnBufferedImage(final BufferedImage image,
                                   final int offsetX, final int offsetY, final int width, final int height) {
   for (int x = offsetX; x < offsetX + width; x++) {
      for (int y = offsetY; y < offsetY + height; y++) {
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

  public void paintFastPreviewOnSmallerBufferedImage(BufferedImage image, int lx, int ly, int rx, int ry) {

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
    this.paintX = paintX;
    this.paintY = paintY;
    this.paintW = paintW;
    this.paintH = paintH;
    this.windowWidth = windowWidth;
    this.windowHeight = windowHeight;
    return generateBufferedImagePreview();
  }

  private BufferedImage generateBufferedImagePreview() {

    System.out.println("PaintX " + paintX + " PaintY " + paintY + " PaintW " + paintW + " PaintH " + paintH
        + " wW " + windowWidth + " wH " + windowHeight);

    Double lx = (-paintX) * 1.0 / paintW * width;
    Double ly = (-paintY) * 1.0 / paintH * height;

    Double rx = (windowWidth - paintX) * 1.0 / paintW * width;
    Double ry = (windowHeight - paintY) * 1.0 / paintH * height;


    if (lx < 0) lx = 0.0;
    if (ly < 0) ly = 0.0;
    if (rx > width) rx = width * 1.0;
    if (ry > height) ry = height * 1.0;

    System.out.println("Which is lx " + lx + " ly " + ly + " rx " + rx + " ry " + ry);


    if (isSlowPreviewDirty) {
        System.out.println("scheduling painting slow preview");
        isSlowPreviewDirty = false;
        isSlowPreviewReady = false;
        previewGenerator.schedulePreviewRendering(lx, ly, rx, ry, getWidth(), getHeight());
    }
    if (isFastPreviewDirty) {
      isFastPreviewDirty = false;
      paintFastPreviewOnSmallerBufferedImage(bufferedImagePreviewFast, 0, 0, width, height);
    }
    if (previewGenerator.isGeneratedPreviewReady()) {
      System.out.println("slow preview ready");
      isSlowPreviewReady = true;
      return previewGenerator.getGeneratedPreview();
    }
    System.out.println("slow preview NOT READY");
    return bufferedImagePreviewFast;
  }

  public boolean wasSlowPreviewReady() {
    return isSlowPreviewReady;
  }

  public void subscribeToAfterChunkPaintedCallback(Consumer<Integer> callback) {
    this.afterChunkPaintedCallback = callback;
  }

  public void setWhiteBalance(double rK, double gK, double bK) {
    this.rWB = rK;
    this.gWB = gK;
    this.bWB = bK;
    markDirty();
  }
}
