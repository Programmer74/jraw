package com.programmer74.jrawtool.components;

import com.programmer74.jrawtool.doubleimage.DoubleImage;
import java.awt.*;

public class HistogramComponent extends Component {

  int[] rPixelsCount = new int[256];
  int[] gPixelsCount = new int[256];
  int[] bPixelsCount = new int[256];
  int[] wPixelsCount = new int[256];

  public HistogramComponent(DoubleImage image) {
    image.setHistogramComponent(this);
  }

  public void resetHistogram() {
    for (int i = 0; i < 256; i++) {
      rPixelsCount[i] = 0;
      gPixelsCount[i] = 0;
      bPixelsCount[i] = 0;
      wPixelsCount[i] = 0;
    }
  }

  public void addPixelToHistogram(int r, int g, int b) {
    rPixelsCount[r]++;
    gPixelsCount[g]++;
    bPixelsCount[b]++;

    Double w = ((0.3 * r) + (0.59 * g) + (0.11 * b));
    wPixelsCount[w.intValue()]++;
  }

  public void paintHistogram() {
    repaint();
  }

  @Override public void paint(final Graphics g) {
    g.clearRect(0, 0, 256, 1024);

    int maxW = 0;
    int maxR = 0;
    int maxG = 0;
    int maxB = 0;
    for (int i = 0; i < 256; i++) {
      maxW = Math.max(maxW, wPixelsCount[i]);
      maxR = Math.max(maxR, rPixelsCount[i]);
      maxG = Math.max(maxG, gPixelsCount[i]);
      maxB = Math.max(maxB, bPixelsCount[i]);
    }

    g.setColor(Color.BLACK);
    g.fillRect(0,0, 256, 1024);

    for (int i = 0; i < 256; i++) {
      int wHeight = (int)(wPixelsCount[i] * 256.0 / maxW);
      int rHeight = (int)(rPixelsCount[i] * 256.0 / maxR);
      int gHeight = (int)(gPixelsCount[i] * 256.0 / maxG);
      int bHeight = (int)(bPixelsCount[i] * 256.0 / maxB);

      g.setColor(Color.WHITE);
      g.fillRect(i, 256 - wHeight, 1, wHeight);
      g.setColor(Color.RED);
      g.fillRect(i, 512 - rHeight, 1, rHeight);
      g.setColor(Color.GREEN);
      g.fillRect(i, 768 - gHeight, 1, gHeight);
      g.setColor(Color.BLUE);
      g.fillRect(i, 1024 - bHeight, 1, bHeight);
    }
  }
}
