package com.programmer74.jrawtool.components;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ImageRotatingCroppingViewer extends Component {

  private BufferedImage originalImage;
  private BufferedImage shrinkedImage;
  private BufferedImage rotatedImage;


  private double angleDegrees;
  private BufferedImage oldRotatedBufferedImage;
  private double oldRotationAngle = -9999.0;

  private double scale;
  private int paintX;
  private int paintY;
  private int paintW;
  private int paintH;

  private int pressedX = 0;
  private int pressedY = 0;

  private boolean doAutoScale = true;

  private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
  private GraphicsDevice gd = ge.getDefaultScreenDevice();
  private GraphicsConfiguration gc = gd.getDefaultConfiguration();
  private Map<Object, Object> hints = new HashMap<>();

  private Color semiTransparentBlack = new Color(0,0,0, 192);

  private int cropRatioX = 3;
  private int cropRatioY = 2;

  private int cropFromX = -1;
  private int cropFromY = -1;
  private int cropFromW = -1;
  private int cropFromH = -1;

  public ImageRotatingCroppingViewer(final BufferedImage originalImage) {
    this.originalImage = originalImage;
    this.shrinkedImage = getShrinkedImage(originalImage);
    rotatedImage = getRotatedImage(shrinkedImage, 0, gc);
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
        if (newScale < 0.5) newScale = 0.5;
        if (newScale > 4) newScale = 4;

        setScaleByMouse(newScale, mouseWheelEvent.getX(), mouseWheelEvent.getY() - getY());
      }
    });

    recalculatePaintParams();
    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
    rotatedImage = getRotatedImage(shrinkedImage, angleDegrees, gc);
    this.repaint();
  }

  private BufferedImage getRotatedImage(BufferedImage originalImage, double angleInDegrees, GraphicsConfiguration gc) {
    double angle = angleInDegrees * Math.PI / 180;
    if (oldRotationAngle == angle) {
      return oldRotatedBufferedImage;
    }

    double sin = Math.abs(Math.sin(angle));
    double cos = Math.abs(Math.cos(angle));
    int w = originalImage.getWidth();
    int h = originalImage.getHeight();
    int neww = (int) Math.floor(w * cos + h * sin);
    int newh = (int) Math.floor(h * cos + w * sin);
    int transparency = originalImage.getColorModel().getTransparency();
    BufferedImage result = gc.createCompatibleImage(neww, newh, transparency);
    Graphics2D g = result.createGraphics();

    g.setRenderingHints(hints);
    g.translate((neww - w) / 2, (newh - h) / 2);
    g.rotate(angle, w / 2, h / 2);
    g.drawRenderedImage(originalImage, null);

    if (oldRotatedBufferedImage != null) {
      oldRotatedBufferedImage.flush();
    }
    oldRotatedBufferedImage = result;
    oldRotationAngle = angle;
    return result;
  }

  private BufferedImage getShrinkedImage(BufferedImage originalImage) {

    int width = originalImage.getWidth();
    int height = originalImage.getHeight();

    int maxw = 800;

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

    BufferedImage newImage = new BufferedImage(width, height, originalImage.getType());
    Graphics g = newImage.getGraphics();
    g.drawImage(originalImage, 0, 0, width, height, null);
    return newImage;
  }



  private void centerPaintedImage() {
    int componentWidth = getWidth();
    int componentHeight = getHeight();

    paintX = (componentWidth - paintW) / 2;
    paintY = (componentHeight - paintH) / 2;

    this.doAutoScale = true;
  }

  private void recalculatePaintParams() {
    int imageWidth = shrinkedImage.getWidth();
    int imageHeight = shrinkedImage.getHeight();

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
    int imageWidth = shrinkedImage.getWidth();
    int imageHeight = shrinkedImage.getHeight();

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

    double k = cropRatioX * 1.0 / cropRatioY;
    cropFromW = 0;
    cropFromH = 0;

    if (getWidth() > getHeight()) {
      cropFromH = getHeight();
      cropFromW = (int)(cropFromH * k);
      if (cropFromW > getWidth()) {
        cropFromW = getWidth();
        cropFromH = (int)(cropFromW / k);
      }
    } else {
      cropFromW = getWidth();
      cropFromH = (int)(cropFromW / k);
      if (cropFromH > getHeight()) {
        cropFromH = getHeight();
        cropFromW = (int)(cropFromH * k);
      }
    }
    cropFromX = (getWidth() - cropFromW) / 2;
    cropFromY = (getHeight() - cropFromH) / 2;

    g.setColor(Color.RED);
    g.drawRect(cropFromX, cropFromY, cropFromW, cropFromH);


    g.setColor(semiTransparentBlack);
    g.fillRect(0, 0, cropFromX, getHeight());
    g.fillRect(cropFromX + cropFromW, 0, cropFromX, getHeight());

    g.fillRect(0, 0, getWidth(), cropFromY);
    g.fillRect(0, cropFromY + cropFromH, getWidth(), cropFromY);
  }

  @Override
  public Dimension getPreferredSize() {
    if (shrinkedImage == null) {
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
  public void setCropRatio(int x, int y) {
    cropRatioX = x;
    cropRatioY = y;
    repaint();
  }

  public BufferedImage getRotatedAndCroppedImage(Consumer<String> status) {

    status.accept("Rotating image...");
    BufferedImage rotatedImage = getRotatedImage(originalImage, angleDegrees, gc);

    status.accept("Original size: " + originalImage.getWidth() + "x" + originalImage.getHeight());
    status.accept("Shrinked size: " + shrinkedImage.getWidth() + "x" + shrinkedImage.getHeight());

    double scaleX = originalImage.getWidth() * 1.0 / shrinkedImage.getWidth();
    double scaleY = originalImage.getHeight() * 1.0 / shrinkedImage.getHeight();

    status.accept("X transform: " + cropFromX + " to " + cropFromX * scaleX);
    status.accept("Y transform: " + cropFromY + " to " + cropFromY * scaleY);
    status.accept("W transform: " + cropFromW + " to " + cropFromW * scaleX);
    status.accept("H transform: " + cropFromH + " to " + cropFromH * scaleY);

    status.accept("Rotated. Applying coordinates...");


    int newW = (int)(cropFromW * scaleX);
    int newH = (int)(cropFromH * scaleY);

    int newX = (int)(cropFromX * scaleX);
    int newY = (int)(cropFromY * scaleY);

    status.accept("Painting...");
    BufferedImage croppedImage = new BufferedImage(newW, newH, originalImage.getType());
    Graphics g = croppedImage.getGraphics();

    g.drawImage(rotatedImage, -newX, -newY, originalImage.getWidth(), originalImage.getHeight(), null);

    return croppedImage;
  }
}
