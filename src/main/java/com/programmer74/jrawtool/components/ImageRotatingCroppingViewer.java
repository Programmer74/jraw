package com.programmer74.jrawtool.components;

import com.programmer74.jrawtool.doubleimage.DoubleImage;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class ImageRotatingCroppingViewer extends Component {

  private DoubleImage doubleImage;
  private BufferedImage rotatedImage;

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

  private double angleDegrees;

  private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
  private GraphicsDevice gd = ge.getDefaultScreenDevice();
  private GraphicsConfiguration gc = gd.getDefaultConfiguration();

  private Color semiTransparentBlack = new Color(0,0,0, 128);

  public ImageRotatingCroppingViewer(final DoubleImage doubleImage) {
    this.doubleImage = doubleImage;
    rotatedImage = doubleImage.getRotatedPreview(0, gc);
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

//    customPaints = (e) -> { /*nothing*/ };

    this.addMouseMotionListener(new MouseMotionAdapter() {
      @Override public void mouseDragged(final MouseEvent mouseEvent) {
        doAutoScale = false;

        int deltaX = pressedX - mouseEvent.getX();
        int deltaY = pressedY - mouseEvent.getY();

        paintX = paintX - deltaX;
        paintY = paintY - deltaY;

        pressedX = mouseEvent.getX();
        pressedY = mouseEvent.getY();

        mouseEvent.getComponent().repaint();
      }
    });

    this.addMouseWheelListener(new MouseWheelListener() {
      @Override public void mouseWheelMoved(final MouseWheelEvent mouseWheelEvent) {
        doAutoScale = false;
        double newScale = scale + mouseWheelEvent.getScrollAmount() * mouseWheelEvent.getWheelRotation() * -1.0 / 100;
        if (newScale < 0.1) newScale = 0.1;
        if (newScale > 2) newScale = 2;

        setScaleByMouse(newScale, mouseWheelEvent.getX(), mouseWheelEvent.getY() - getY());
      }
    });

    recalculatePaintParams();

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

  public double getAngleDegrees() {
    return angleDegrees;
  }

  public void setAngleDegrees(double angleDegrees) {
    this.angleDegrees = angleDegrees;
    rotatedImage = doubleImage.getRotatedPreview(angleDegrees, gc);
    this.repaint();
  }

  private void centerPaintedImage() {
    int componentWidth = getWidth();
    int componentHeight = getHeight();

    paintX = (componentWidth - paintW) / 2;
    paintY = (componentHeight - paintH) / 2;

    this.doAutoScale = true;
  }

  private void recalculatePaintParams() {
    int imageWidth = doubleImage.getWidth();
    int imageHeight = doubleImage.getHeight();

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
    double onImageX = (cursorX - paintX) * 1.0 / paintW;
    double onImageY = (cursorY - paintY) * 1.0 / paintH;
    this.doAutoScale = false;
    this.scale = newScale;
    recalculatePaintParams();

    double newOnImageX = (cursorX - paintX) * 1.0 / paintW;
    double newOnImageY = (cursorY - paintY) * 1.0 / paintH;
    double onImageXDelta = (newOnImageX - onImageX);
    double onImageYDelta = (newOnImageY - onImageY);
    paintX = paintX + (int)(paintW * onImageXDelta);
    paintY = paintY + (int)(paintH * onImageYDelta);
    this.repaint();
  }

  public double getScale() {
    return scale;
  }

  public double calculateScale() {
    int imageWidth = doubleImage.getWidth();
    int imageHeight = doubleImage.getHeight();

    int componentWidth = getWidth();
    int componentHeight = getHeight();

    double wK = imageWidth * 1.0 / componentWidth;
    double hK = imageHeight * 1.0 / componentHeight;

    scale = 1 / Math.max(wK, hK);
    return scale;
  }

  // overrides the paint method of Component class
  @Override
  public void paint(Graphics g) {
    recalculatePaintParams();

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.drawImage(rotatedImage, paintX, paintY, paintW, paintH, null);

    int x = 50;
    int y = 50;
    int w = getWidth() - 100;
    int h = getHeight() - 100;
    g.setColor(semiTransparentBlack);
//    g.setColor(Color.RED);
    g.fillRect(0, 0, getWidth(), y);
    g.fillRect(0, getHeight() - y, getWidth(), y);
    g.fillRect(0, 0, x, getHeight());
    g.fillRect(getWidth() - x, 0, x, getHeight());
//    g.fillRect(rx, 0, getWidth() - rx, getHeight() - ry);
//    g.fillRect(paintX, paintY, paintW, paintH);

  }

  @Override
  public Dimension getPreferredSize() {
    if (doubleImage == null) {
      return new Dimension(100, 100);
    } else {
      // make sure the window is not two small to be seen
      return new Dimension(Math.max(100, rotatedImage.getWidth()),
          Math.max(100, rotatedImage.getHeight()));
    }
  }

  private void setWaitCursor() {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }
  private void setNormalCursor() {
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }
}
