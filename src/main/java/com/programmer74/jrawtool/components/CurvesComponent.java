package com.programmer74.jrawtool.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CurvesComponent extends Component {

  private final int width = 256;
  private final int height = 256;

  private int mouseMoveX;
  private int mouseMoveY;

  private int mouseDownX;
  private int mouseDownY;
  private boolean mousePressed = false;

  private List<Point> points = new ArrayList<>();
  private int closestPointIndex = -1;

  private int pointSize = 10;

  private HistogramComponent histogramComponent;

  private Consumer<Integer> onChangeCallback;

  private final List<Double> controlPointsX = new ArrayList<>();
  private final List<Double> controlPointsY = new ArrayList<>();

  private final int fastOutputArrayCount = 256;
  private final double[] fastOutputArray = new double[fastOutputArrayCount];

  private void sortPoints() {
    points.sort(new Comparator<Point>() {
      @Override public int compare(final Point point, final Point t1) {
        int xr = Integer.compare(point.x, t1.x);
        if (xr != 0) return xr;
        return Integer.compare(point.y, t1. y);
      }
    });
  }

  public CurvesComponent(HistogramComponent histogramComponent) {
    MouseAdapter doubleClickHandler = new MouseAdapter() {
      @Override public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
          e.consume();
          //this is doubleclick handler
          if (e.getButton() == 3) {
            if (closestPointIndex > -1) {
              points.remove(closestPointIndex);
              sortPoints();
              if (closestPointIndex == 0) {
                closestPointIndex = -1;
              }
            }
          } else {
            points.add(new Point(e.getX(), e.getY()));
            sortPoints();
          }
          repaint();
          if (onChangeCallback != null) {
            onChangeCallback.accept(0);
          }
        }
      }

      @Override public void mousePressed(final MouseEvent mouseEvent) {
        mouseDownX = mouseEvent.getX();
        mouseDownY = mouseEvent.getY();
        mousePressed = true;
      }

      @Override public void mouseReleased(final MouseEvent mouseEvent) {
        mousePressed = false;
      }
    };

    this.addMouseListener(doubleClickHandler);

    this.addMouseMotionListener(new MouseMotionListener() {
      @Override public void mouseDragged(final MouseEvent mouseEvent) {
        if (closestPointIndex > -1) {
          mouseMoveX = mouseEvent.getX();
          mouseMoveY = mouseEvent.getY();
          int x = points.get(closestPointIndex).x - (mouseDownX - mouseMoveX);
          int y = points.get(closestPointIndex).y - (mouseDownY - mouseMoveY);
          mouseDownX = mouseMoveX;
          mouseDownY = mouseMoveY;
          if ((x < 0) || (y < 0) || (x > width) || (y > height)) return;
          if (closestPointIndex > 0) {
            if (x < points.get(closestPointIndex - 1).x + pointSize) return;
          }
          if (closestPointIndex < points.size() - 1) {
            if (x > points.get(closestPointIndex + 1).x - pointSize) return;
          }
          points.get(closestPointIndex).setLocation(x, y);
        }
        repaint();
        if (onChangeCallback != null) {
          onChangeCallback.accept(0);
        }
      }

      @Override public void mouseMoved(final MouseEvent mouseEvent) {
        mouseMoveX = mouseEvent.getX();
        mouseMoveY = mouseEvent.getY();
        repaint();
      }
    });

    this.histogramComponent = histogramComponent;
  }

  public void setOnChangeCallback(final Consumer<Integer> onChangeCallback) {
    this.onChangeCallback = onChangeCallback;
  }

  public Consumer<Integer> getOnChangeCallback() {
    return onChangeCallback;
  }

  @Override
  public void paint(Graphics g) {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width, height);

    //draw histogram
    g.setColor(Color.GRAY);
    int maxW = 0;
    for (int i = 0; i < 256; i++) {
      maxW = Math.max(maxW, histogramComponent.getwPixelsCount()[i]);
    }
    for (int i = 0; i < 256; i++) {
      int wHeight = (int) (histogramComponent.getwPixelsCount()[i] * 1.0 * 256 / maxW);
      g.fillRect(i, 256 - wHeight, 1, wHeight);
    }

    double minDist = 100000;
    int i = 0;
    Point mouse = new Point(mouseMoveX, mouseMoveY);

    int fromx = 0;
    int fromy = 255;
    int tox = 0;
    int toy = 0;

    for (Point point : points) {
      int x = (int)point.getX();
      int y = (int)point.getY();
      g.setColor(Color.red);
      g.drawOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);
      if (!mousePressed) {
        if (point.distance(mouse) < minDist) {
          minDist = point.distance(mouse);
          closestPointIndex = i;
        }
      }
      i++;

      tox = x;
      toy = y;
      g.setColor(Color.red);
      g.drawLine(fromx, fromy, tox, toy);
      fromx = tox;
      fromy = toy;
    }

    tox = 255;
    toy = 0;
    g.setColor(Color.red);
    g.drawLine(fromx, fromy, tox, toy);

    if (closestPointIndex > -1) {
      Point closest = points.get(closestPointIndex);
      int x = (int) closest.getX();
      int y = (int) closest.getY();
      g.setColor(Color.blue);
      g.drawOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);
    }

    prepareOutput();
    prepareFastOutput();
    g.setColor(Color.BLACK);
    for (double d = 0.0; d < 1.0; d += 0.005) {
      int x = (int)(d * 255);
      int y = (int)(calculateOutput(d) * 255);
      g.fillRect(x - 1, 254 - y, 2, 2);
    }
  }

  // overrides the method in Component class, to determine the window size
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(width, height);
  }

  public void prepareOutput() {
    controlPointsX.clear();
    controlPointsY.clear();

    controlPointsX.add(0.0);
    controlPointsY.add(0.0);
    sortPoints();
    for (Point p : points) {
      controlPointsX.add(p.getX() / 255.0);
      controlPointsY.add(1 - (p.getY() / 255.0));
    }
    controlPointsX.add(1.0);
    controlPointsY.add(1.0);
  }

  public double calculateOutput(double input) {
    int i = 0;
    double lx = 0;
    double ly = 0;
    while (input > controlPointsX.get(i)) {
      lx = controlPointsX.get(i);
      ly = controlPointsY.get(i);
      i++;
    }
    double rx = controlPointsX.get(i);
    double ry = controlPointsY.get(i);

    return ly + (input - lx) / (rx - lx) * (ry - ly);
  }

  public void prepareFastOutput() {
    for (int i = 0; i < fastOutputArrayCount; i++) {
      fastOutputArray[i] = calculateOutput(i * 1.0 / fastOutputArrayCount);
    }
  }

  public double calculateFastOutput(double input) {
    int i = (int)(input * fastOutputArrayCount);
    if (i <= 0) i = 0;
    if (i >= fastOutputArrayCount) i = fastOutputArrayCount - 1;
    return fastOutputArray[i];
  }

  public Function<Double, Integer> getPixelConverter() {
    return new Function<Double, Integer>() {
      @Override public Integer apply(final Double aDouble) {
        return (int)(255 * calculateFastOutput(aDouble));
      }
    };
  }
}
