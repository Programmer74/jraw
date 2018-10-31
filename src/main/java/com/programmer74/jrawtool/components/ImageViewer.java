package com.programmer74.jrawtool.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.function.Consumer;

public class ImageViewer extends Component {

  private PaintableImage paintableImage;

  private double scale;
  private int paintX;
  private int paintY;
  private int paintW;
  private int paintH;

  private int pressedX = 0;
  private int pressedY = 0;

  private boolean doAutoScale = true;

  private Consumer<Object> afterPreviewCreatedCallback;

  private Consumer<Graphics> customPaints;

  public ImageViewer(final PaintableImage paintableImage) {
    this.paintableImage = paintableImage;
    this.paintableImage.setParent(this);
    this.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(final MouseEvent mouseEvent) {
        pressedX = mouseEvent.getX();
        pressedY = mouseEvent.getY();
      }
      @Override public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
          e.consume();
          //this is doubleclick handler
          setAutoScale();
        }
      }
      @Override public void mouseMoved(final MouseEvent e) {

      }
    });

    customPaints = (e) -> { /*nothing*/ };

    this.addMouseMotionListener(new MouseMotionAdapter() {
      @Override public void mouseDragged(final MouseEvent mouseEvent) {
        doAutoScale = false;

        int deltaX = pressedX - mouseEvent.getX();
        int deltaY = pressedY - mouseEvent.getY();

        paintX = paintX - deltaX;
        paintY = paintY - deltaY;

        pressedX = mouseEvent.getX();
        pressedY = mouseEvent.getY();

        forceImageInsidePreviewPanel();
        paintableImage.markSlowPreviewDirty();
        mouseEvent.getComponent().repaint();
      }
    });

    this.addMouseWheelListener(new MouseWheelListener() {
      @Override public void mouseWheelMoved(final MouseWheelEvent mouseWheelEvent) {
        doAutoScale = false;
        double newScale = scale + mouseWheelEvent.getScrollAmount() * mouseWheelEvent.getWheelRotation() * -1.0 / 100;
        if (newScale < 0.1) newScale = 0.1;
        if (newScale > 2) newScale = 2;

        setScaleByMouse(newScale, mouseWheelEvent.getX(), mouseWheelEvent.getY());
        System.out.println("scroll");
      }
    });

    recalculatePaintParams();
    paintableImage.setAfterChunkPaintedCallback((e) -> this.repaint());
    paintableImage.setAfterSlowPreviewRenderingBeginCallback((e) -> setWaitCursor());
    paintableImage.setAfterSlowPreviewRenderingEndCallback((e) -> setNormalCursor());
  }

  public void setCustomPaints(final Consumer<Graphics> customPaints) {
    this.customPaints = customPaints;
  }

  public int getOnImageX(int cursorX) {
    return (cursorX - paintX) * paintableImage.getWidth() / paintW;
  }

  public int getOnImageY(int cursorY) {
    return (cursorY - paintY) * paintableImage.getHeight() / paintH;
  }

  public void setAfterPaintCallback(final Consumer<Object> afterPaintCallback) {
    this.afterPreviewCreatedCallback = afterPaintCallback;
  }

  public void setScale(final double newScale) {
    setScaleByMouse(newScale, getWidth() / 2, getHeight() / 2);
    repaint();
  }

  public void setAutoScale() {
    setScale(calculateScale());
    centerPaintedImage();
    repaint();
  }

  private void centerPaintedImage() {
    int componentWidth = getWidth();
    int componentHeight = getHeight();

    paintX = (componentWidth - paintW) / 2;
    paintY = (componentHeight - paintH) / 2;

    this.doAutoScale = true;
  }

  private void forceImageInsidePreviewPanel() {
    if (paintW < getWidth()) {
      if (paintX < 0) paintX = 0;
      if ((paintX + paintW) > getWidth()) paintX = getWidth() - paintW;
    } else {
      if ((paintX + paintW) < getWidth()) paintX = getWidth() - paintW;
      if (paintX > 0) paintX = 0;
    }

    if (paintH < getHeight()) {
      if (paintY < 0) paintY = 0;
      if ((paintY + paintH) > getHeight()) paintY = getHeight() - paintH;
    } else {
      if ((paintY + paintH) < getHeight()) paintY = getHeight() - paintH;
      if (paintY > 0) paintY = 0;
    }
  }

  private void recalculatePaintParams() {
    int imageWidth = paintableImage.getWidth();
    int imageHeight = paintableImage.getHeight();

    if (doAutoScale) {
      scale = calculateScale();
    }

    Double screenImageWidth = imageWidth * scale;
    Double screenImageHeight = imageHeight * scale;

    paintW = screenImageWidth.intValue();
    paintH = screenImageHeight.intValue();

    if (doAutoScale) {
      centerPaintedImage();
    }
  }

  public void setScaleByMouse(final double newScale, final int cursorX, final int cursorY) {
//    System.out.println("Mouse cursor at " + cursorX + ":" + cursorY);

//    System.out.println("Current scale: " + scale + " new scale: " + newScale);

    double onImageX = (cursorX - paintX) * 1.0 / paintW;
    double onImageY = (cursorY - paintY) * 1.0 / paintH;
//    System.out.println("old onImageCursor at " + onImageX + " : " + onImageY);

    this.doAutoScale = false;
    this.scale = newScale;
    recalculatePaintParams();

    double newOnImageX = (cursorX - paintX) * 1.0 / paintW;
    double newOnImageY = (cursorY - paintY) * 1.0 / paintH;
//    System.out.println("new onImageCursor at " + newOnImageX + " : " + newOnImageY);

    double onImageXDelta = (newOnImageX - onImageX);
    double onImageYDelta = (newOnImageY - onImageY);
    paintX = paintX + (int)(paintW * onImageXDelta);
    paintY = paintY + (int)(paintH * onImageYDelta);

    forceImageInsidePreviewPanel();
    paintableImage.markSlowPreviewDirty();
    this.repaint();
  }

  public double getScale() {
    return scale;
  }

  public double calculateScale() {
    int imageWidth = paintableImage.getWidth();
    int imageHeight = paintableImage.getHeight();

    int componentWidth = getWidth();
    int componentHeight = getHeight();

    double wK = imageWidth * 1.0 / componentWidth;
    double hK = imageHeight * 1.0 / componentHeight;

    scale = 1 / Math.max(wK, hK);
//    System.out.println("Calculated Scale : " + scale);
    return scale;
  }

  // overrides the paint method of Component class
  @Override
  public void paint(Graphics g) {
    recalculatePaintParams();
    forceImageInsidePreviewPanel();
    paintableImage.paintPreviewOnGraphics(g, paintX, paintY, paintW, paintH, getWidth(), getHeight());
    if (afterPreviewCreatedCallback != null) {
      afterPreviewCreatedCallback.accept(this);
    }
    customPaints.accept(g);
  }

  // overrides the method in Component class, to determine the window size
  @Override
  public Dimension getPreferredSize() {
    if (paintableImage == null) {
      return new Dimension(100, 100);
    } else {
      // make sure the window is not two small to be seen
      return new Dimension(Math.max(100, paintableImage.getWidth() / 6),
          Math.max(100, paintableImage.getHeight() / 6));
    }
  }

  private void setWaitCursor() {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }
  private void setNormalCursor() {
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }
}
