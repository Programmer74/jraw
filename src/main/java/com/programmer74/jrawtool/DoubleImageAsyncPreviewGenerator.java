package com.programmer74.jrawtool;

import java.awt.image.BufferedImage;

public class DoubleImageAsyncPreviewGenerator {

  private final DoubleImage parent;
  private Thread workerThread = null;

  private BufferedImage image = null;
  private boolean paintingShouldStop = false;
  private boolean paintingIsReady = false;

  public DoubleImageAsyncPreviewGenerator(DoubleImage doubleImage) {
    this.parent = doubleImage;
  }

  public void schedulePreviewRendering(final Double lx, final Double ly, final Double rx, final Double ry, int width, int height) {
    schedulePreviewRendering(lx.intValue(), ly.intValue(), rx.intValue(), ry.intValue(), width, height);
  }

  public void schedulePreviewRendering(final int lx, final int ly, final int rx, final int ry, final int width, final int height) {
    if (workerThread != null) {
      paintingShouldStop = true;
      try {
        workerThread.join(100);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    image = new BufferedImage(rx - lx, ry - ly, BufferedImage.TYPE_INT_RGB);
    paintingIsReady = false;
    paintingShouldStop = false;

    workerThread = new Thread(() -> {
      paintSlowPreviewOnBufferedImage(image, lx, ly, rx, ry);
    });
    workerThread.start();
  }

  public BufferedImage getGeneratedPreview() {
    if (paintingIsReady) {
      return image;
    }
    return null;
  }

  public boolean isGeneratedPreviewReady() {
    return paintingIsReady;
  }

  //=======================
  //Internals

  private void paintSlowPreviewOnBufferedImage(final BufferedImage image,
                                              final int lx, final int ly, final int rx, final int ry) {
    System.out.println("Should paint at " + lx + ":" + ly + " - " +  rx + ":" + ry);

    int fromX = 0, toX = 0;
    int fromY = 0, toY = 0;

    for (fromX = lx, toX = 0; fromX < rx && toX < image.getWidth(); fromX++, toX++) {
      for (fromY = ly, toY = 0; fromY < ry && toY < image.getHeight(); fromY++, toY++) {
        if (paintingShouldStop) {
          System.out.println("Slow Preview: requested to stop at " + fromX + ":" + fromY);
          paintingShouldStop = false;
          return;
        }
        double[] pixel = parent.pixels[fromX][fromY].clone();

        parent.adjustWhiteBalance(pixel);
        parent.adjustGamma(pixel);
        parent.adjustExposure(pixel);

        int r = parent.doubleValueToUint8T(pixel[0]);
        int g = parent.doubleValueToUint8T(pixel[1]);
        int b = parent.doubleValueToUint8T(pixel[2]);

        int rgbcolor = 0xff000000 | r << 16 | g << 8 | b;
        image.setRGB(toX, toY, rgbcolor);
      }
    }
    System.out.println("Stopped at " + toX + ":" + toY + " - " +  fromX + ":" + fromY);
    System.out.println("Painted at " + lx + ":" + ly + " - " +  rx + ":" + ry);
    paintingIsReady = true;
    System.out.println("Callbacking callback...");
    parent.afterChunkPaintedCallback.accept(0);
  }
//
//
//  private CountDownLatch latch;
//  private void schedulePaintingFullBufferedImageExecutor (final int lx, final int ly, final int rx, final int ry) {
//    executorService.submit(new Runnable() {
//      @Override
//      public void run() {
//        System.out.println("slow preview: begin painting");
//        System.out.println("lx " + lx + " ly " + ly + " rx " + rx + " ry " + ry);
////        paintOnBufferedImage(bufferedImagePreviewSlow, lx, ly, rx - lx, ry - ly, isSlowPreviewDirty);
//        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        paintSlowPreviewOnBufferedImage(img, lx, ly, rx - lx, ry - ly);
//        System.out.println("slow preview: done or cancelled painting");
//        latch.countDown();
//        if (!isSlowPreviewDirty) {
//          System.out.println("slow preview: setting image");
//          bufferedImagePreviewSlow = img;
//          System.out.println("slow preview: setting image done");
//          isSlowPreviewReady = true;
//          System.out.println("slow preview: ready");
//          afterChunkPaintedCallback.accept(0);
//        } else {
//          System.out.println("slow preview: for some reason isSlowPreviewDirty is true");
//          generateBufferedImagePreview();
//        }
//      }
//    });
//  }
//  public void schedulePaintingFullBufferedImage(int lx, int ly, int rx, int ry) {
//    if (isSlowPreviewDirty) {
//
//      if (latch != null) {
//        if (latch.getCount() != 0) {
//          System.err.println("No scheduling: painting already in process");
//          try {
//            latch.await();
//          } catch (Exception ex) {
//            ex.printStackTrace();
//          }
//          System.err.println("No scheduling: painting ended");
//          generateBufferedImagePreview();
//          return;
//        }
//      }
//
//      isSlowPreviewDirty = false;
//      isSlowPreviewReady = false;
//
//      latch = new CountDownLatch(1);
//
//      System.out.println("slow preview beginning...");
//      schedulePaintingFullBufferedImageExecutor(lx, ly, rx, ry);
//    }
//  }

}
