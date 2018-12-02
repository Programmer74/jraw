package com.programmer74.jrawtool.doubleimage;

import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.components.PaintableImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Function;

public class DoubleImage implements PaintableImage {
  //x,y,{r,g,bufferedImage}
  protected double[][][] pixels;

  private int width;
  private int height;

  private double gGamma = 1 / 2.2222;
  private double gA = 1.0;

  private double rWB = 2.170906;
  private double gWB = 1.0000;
  private double bWB = 1.163172;

  private double exposureStop = 0;
  private double brightness = 0;
  private double contrast = 1.0;

  private double hue = 0.0;
  private double saturationK = 1.0;
  private double value = 0.0;

  private BufferedImage bufferedImage;
  private BufferedImage bufferedImagePreviewFast;

  private BufferedImage originalImage = null;

  private boolean isDirty = true;
  private boolean isFastPreviewDirty = true;
  public boolean isSlowPreviewDirty = true;
  private boolean isSlowPreviewReady = false;

  protected Consumer<Integer> afterChunkPaintedCallback;
  protected Consumer<Integer> afterSlowPreviewRenderingBeginCallback;
  protected Consumer<Integer> afterSlowPreviewRenderingEndCallback;

  private DoubleImageAsyncPreviewGenerator previewGenerator;

  private int paintX, paintY, paintW, paintH, windowWidth, windowHeight;
  private int oldWindowWidth = 0, oldWindowHeight = 0;

  private Component parent;
  private HistogramComponent histogramComponent;
  private HistogramComponent rawHistogramComponent;

  private DoubleImageDefaultValues defaultValues;

  private Function<Double, Integer> customPixelConverter = null;
  private double[][] customConvolutionKernel = {{1}};

  public DoubleImage(final int width, final int height, final DoubleImageDefaultValues defaultValues) {
    this.width = width;
    this.height = height;
    this.defaultValues = defaultValues;
    applyDefaultValues(defaultValues);
    this.pixels = new double[width][height][3];
    this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    this.bufferedImagePreviewFast = new BufferedImage(width / 8, height / 8, BufferedImage.TYPE_INT_RGB);
    this.previewGenerator = new DoubleImageAsyncPreviewGenerator(this);
  }

  @Override
  public void setParent(final Component parent) {
    this.parent = parent;
  }

  public void setHistogramComponent(
      final HistogramComponent histogramComponent) {
    this.histogramComponent = histogramComponent;
  }

  public void setRawHistogramComponent(
      final HistogramComponent histogramComponent) {
    this.rawHistogramComponent = histogramComponent;
  }

  public void setCustomPixelConverter(
      final Function<Double, Integer> customPixelConverter) {
    this.customPixelConverter = customPixelConverter;
    markDirty();
  }

  public void setDefaultPixelConverter() {
    this.customPixelConverter = null;
    markDirty();
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  public double[][][] getPixels() {
    return pixels;
  }

  public double[] getPixel(int x, int y) {
    return pixels[x][y];
  }

  public void setPixel(int x, int y, double r, double g, double b) {
    pixels[x][y][0] = r;
    pixels[x][y][1] = g;
    pixels[x][y][2] = b;
  }

  public void setPixel(int x, int y, double[] pixel) {
    pixels[x][y][0] = pixel[0];
    pixels[x][y][1] = pixel[1];
    pixels[x][y][2] = pixel[2];
  }

  public void getColorizedPixel(int x, int y, double[] output) {
    double[] pixel = pixels[x][y].clone();
    adjustPixelParams(pixel);
    for (int i = 0; i < 2; i++) {
      output[i] = pixel[i];
    }
  }

  protected int doubleValueToUint8T(Double value) {
    if (customPixelConverter != null) {
      return customPixelConverter.apply(value);
    } else {
      return doubleValueToUint8TWithoutConverter(value);
    }
  }

  private int doubleValueToUint8TWithoutConverter(Double value) {
    value = value * 255.0;
    int intval = value.intValue();
    if (intval > 255) intval = 255;
    if (intval < 0) intval = 0;
    return intval;
  }

  private double calculateGammaCorrection(double input, double A, double gamma) {
    return A * Math.pow(input, gamma);
  }

  private double calculateExposureCorrection(double input, double stop) {
    return input * Math.pow(2, stop);
  }

  private double calculateBrightnessContrastCorrection(double input, double brightness, double contrast) {
    //R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
    double contrasted = (input - 0.5) * contrast + 0.5;
    return contrasted + brightness;
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

  protected void adjustBrightnessContrast(double[] pixel) {
    for (int i = 0; i < 3; i++) {
      pixel[i] = calculateBrightnessContrastCorrection(pixel[i], brightness, contrast);
    }
  }

  protected void adjustHSLParams(final double[] pixel) {
    double[] hsvpixel = HSVRGBUtils.rgb2hsv(pixel);
    hsvpixel[0] += hue;
    hsvpixel[1] *= saturationK;
    hsvpixel[2] += value;
    hsvpixel = HSVRGBUtils.hsv2rgb(hsvpixel);
    for (int i = 0; i < 3; i++) pixel[i] = hsvpixel[i];
  }

  protected void applyConvolution(final double[] pixel,
      final int x, final int y,
      final double[][] convMatrix,
      final double strength) {
    int a = convMatrix[0].length;
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

          acc += pixels[sourceX][sourceY][colorIndex] * convMatrix[dx][dy];
        }
      }
      pixel[colorIndex] = acc * strength + pixel[colorIndex] * (1 - strength);
    }
  }

  @Override
  public void markSlowPreviewDirty() {
    isSlowPreviewDirty = true;
    isSlowPreviewReady = false;
  }

  @Override
  public void markPreviewDirty() {
    markSlowPreviewDirty();
    isFastPreviewDirty = true;
  }

  @Override
  public void markDirty() {
    markPreviewDirty();
    isDirty = true;
  }

  public void paintOnBufferedImage(BufferedImage image) {
    paintOnBufferedImage(image, 0, 0, width, height);
  }
  private void paintOnBufferedImage(final BufferedImage image,
                                   final int offsetX, final int offsetY, final int width, final int height) {
   for (int x = offsetX; x < offsetX + width; x++) {
      for (int y = offsetY; y < offsetY + height; y++) {

        double[] pixel = pixels[x][y].clone();

        adjustPixelConvolutions(pixel, x, y);
        adjustPixelParams(pixel);

        int r = doubleValueToUint8T(pixel[0]);
        int g = doubleValueToUint8T(pixel[1]);
        int b = doubleValueToUint8T(pixel[2]);

        int rgbcolor = 0xff000000 | r << 16 | g << 8 | b;
        image.setRGB(x, y, rgbcolor);
      }
    }
  }

  public void adjustPixelParams(double[] pixel) {
    adjustWhiteBalance(pixel);
    adjustGamma(pixel);
    adjustExposure(pixel);
    adjustBrightnessContrast(pixel);
    adjustHSLParams(pixel);
  }

  protected void adjustPixelConvolutions(double[] pixel, int x, int y) {
    applyConvolution(pixel, x, y, customConvolutionKernel, 1);
  }

  public void paintFastPreviewOnSmallerBufferedImage(BufferedImage image, int lx, int ly, int rx, int ry) {

    if (histogramComponent != null) {
      histogramComponent.resetHistogram();
    }

    if (rawHistogramComponent != null) {
      rawHistogramComponent.resetHistogram();
    }

//    System.out.println("GAMMA: " + gGamma);

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {

        int sx = x * (rx - lx) / image.getWidth() + lx;
        int sy = y * (ry - ly) / image.getHeight() + ly;

        if (sx >= width) sx = width - 1;
        if (sy >= height) sy = height - 1;

        double[] pixel = pixels[sx][sy].clone();

        adjustWhiteBalance(pixel);
        adjustGamma(pixel);
        int r = doubleValueToUint8TWithoutConverter(pixel[0]);
        int g = doubleValueToUint8TWithoutConverter(pixel[1]);
        int b = doubleValueToUint8TWithoutConverter(pixel[2]);
        if (rawHistogramComponent != null) {
          rawHistogramComponent.addPixelToHistogram(r, g, b);
        }

        adjustPixelConvolutions(pixel, sx, sy);
        adjustPixelParams(pixel);

        r = doubleValueToUint8T(pixel[0]);
        g = doubleValueToUint8T(pixel[1]);
        b = doubleValueToUint8T(pixel[2]);

        int rgbcolor = 0xff000000 | r << 16 | g << 8 | b;
        image.setRGB(x, y, rgbcolor);

        if (histogramComponent != null) {
          histogramComponent.addPixelToHistogram(r, g, b);
        }
      }
    }

    if (histogramComponent != null) {
      histogramComponent.paintHistogram();
    }
  }

  public BufferedImage getBufferedImage() {
    if (isDirty) {
      paintOnBufferedImage(bufferedImage);
      isDirty = false;
    }
    return bufferedImage;
  }

  public BufferedImage getFastBufferedImage() {
    if (isFastPreviewDirty) {
      isFastPreviewDirty = false;
      paintFastPreviewOnSmallerBufferedImage(bufferedImagePreviewFast, 0, 0, width, height);
    }
    return bufferedImagePreviewFast;
  }

  private void applyPreviewCoordinates(
      int paintX, int paintY, int paintW, int paintH,
      int windowWidth, int windowHeight) {
    this.paintX = paintX;
    this.paintY = paintY;
    this.paintW = paintW;
    this.paintH = paintH;
    this.windowWidth = windowWidth;
    this.windowHeight = windowHeight;
  }

  private BufferedImage getBufferedImagePreview() {

//    System.out.println("PaintX " + paintX + " PaintY " + paintY + " PaintW " + paintW + " PaintH " + paintH
//        + " wW " + windowWidth + " wH " + windowHeight);

    Double lx = (-paintX) * 1.0 / paintW * width;
    Double ly = (-paintY) * 1.0 / paintH * height;

    Double rx = (windowWidth - paintX) * 1.0 / paintW * width;
    Double ry = (windowHeight - paintY) * 1.0 / paintH * height;


    if (lx < 0) lx = 0.0;
    if (ly < 0) ly = 0.0;
    if (rx > width) rx = width * 1.0;
    if (ry > height) ry = height * 1.0;

//    System.out.println("Which is lx " + lx + " ly " + ly + " rx " + rx + " ry " + ry);


    if (isSlowPreviewDirty) {
//        System.out.println("scheduling painting slow preview");
        isSlowPreviewDirty = false;
        isSlowPreviewReady = false;
        previewGenerator.schedulePreviewRendering(lx, ly, rx, ry);
        afterSlowPreviewRenderingBeginCallback.accept(0);
    }
    if (isFastPreviewDirty) {
      isFastPreviewDirty = false;
      paintFastPreviewOnSmallerBufferedImage(bufferedImagePreviewFast, 0, 0, width, height);
    }
    if (previewGenerator.isGeneratedPreviewReady()) {
//      System.out.println("slow preview ready");
      isSlowPreviewReady = true;
      afterSlowPreviewRenderingEndCallback.accept(0);
      return previewGenerator.getGeneratedPreview();
    }
//    System.out.println("slow preview NOT READY");
    return bufferedImagePreviewFast;
  }

  @Override
  public void paintPreviewOnGraphics(Graphics g,
                                     int paintX, int paintY, int paintW, int paintH,
                                     int windowWidth, int windowHeight) {

    if ((oldWindowHeight != windowHeight) || (oldWindowWidth != windowWidth)) {
      markSlowPreviewDirty();
      oldWindowHeight = windowHeight;
      oldWindowWidth = windowWidth;
    }

    applyPreviewCoordinates(
        paintX, paintY, paintW, paintH, windowWidth, windowHeight);

    BufferedImage preview = getBufferedImagePreview();

    if (isSlowPreviewReady) {
      g.drawImage(preview,
          Math.max(0, paintX),
          Math.max(0, paintY),
          Math.min(windowWidth, paintW),
          Math.min(windowHeight, paintH),
          parent);
    } else {
      g.drawImage(preview, paintX, paintY, paintW, paintH, parent);
    }
  }

  public BufferedImage getOriginalImage() {
    return originalImage;
  }

  public void setOriginalImage(BufferedImage originalImage) {
    this.originalImage = originalImage;
  }

  @Override
  public BufferedImage burnPreview(int maxw) {
    isDirty = true;
    BufferedImage original = getBufferedImage();
    return BufferedImageUtils.getShrinkedImage(original, maxw);
  }

  @Override
  public void setAfterChunkPaintedCallback(
      final Consumer<Integer> afterChunkPaintedCallback) {
    this.afterChunkPaintedCallback = afterChunkPaintedCallback;
  }

  @Override
  public void setAfterSlowPreviewRenderingBeginCallback(
      final Consumer<Integer> afterSlowPreviewRenderingBeginCallback) {
    this.afterSlowPreviewRenderingBeginCallback = afterSlowPreviewRenderingBeginCallback;
  }

  @Override
  public void setAfterSlowPreviewRenderingEndCallback(
      final Consumer<Integer> afterSlowPreviewRenderingEndCallback) {
    this.afterSlowPreviewRenderingEndCallback = afterSlowPreviewRenderingEndCallback;
  }

  public DoubleImageDefaultValues getDefaultValues() {
    return defaultValues;
  }

  public void applyDefaultValues(DoubleImageDefaultValues defaultValues) {
    this.rWB = defaultValues.getrK();
    this.gWB = defaultValues.getgK();
    this.bWB = defaultValues.getbK();
    this.exposureStop = defaultValues.getExposure();
    this.brightness = defaultValues.getBrigthness();
    this.contrast = defaultValues.getContrast();
    this.hue = defaultValues.getHue();
    this.saturationK = defaultValues.getSaturationK();
    this.value = defaultValues.getValue();
    this.gGamma = 1 / defaultValues.getGamma();
  }

  public void setWhiteBalance(double rK, double gK, double bK) {
    this.rWB = rK;
    this.gWB = gK;
    this.bWB = bK;
    markDirty();
  }

  public void setExposureStop(double exposureStop) {
    this.exposureStop = exposureStop;
    markDirty();
  }

  public void setBrightness(double brightness) {
    this.brightness = brightness;
    markDirty();
  }

  public void setContrast(double contrast) {
    this.contrast = contrast;
    markDirty();
  }

  public void setGamma(double gamma) {
    this.gGamma = 1 / gamma;
    markDirty();
  }

  public void setHue(double hue) {
    this.hue = hue;
    markDirty();
  }

  public void setSaturationK(double saturationK) {
    this.saturationK = saturationK;
    markDirty();
  }

  public void setValue(double value) {
    this.value = value;
    markDirty();
  }

  public void setCustomConvolutionKernel(final double[][] customConvolutionKernel) {
    this.customConvolutionKernel = customConvolutionKernel;
    markDirty();
  }
}
