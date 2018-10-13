package com.programmer74.jrawtool.components;

import com.programmer74.jrawtool.doubleimage.DoubleImage;
import java.awt.*;

public class HistogramComponent extends Component {

  int[] rPixelsCount = new int[256];
  int[] gPixelsCount = new int[256];
  int[] bPixelsCount = new int[256];
  int[] wPixelsCount = new int[256];

  static final int histogramHeight = 128;

  public int[] getwPixelsCount() {
    return wPixelsCount;
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
    g.clearRect(0, 0, 256, histogramHeight * 4);

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
    g.fillRect(0,0, 256, histogramHeight * 4);

    for (int i = 0; i < 256; i++) {
      int wHeight = (int)(wPixelsCount[i] * 1.0 * histogramHeight / maxW);
      int rHeight = (int)(rPixelsCount[i] * 1.0 * histogramHeight / maxR);
      int gHeight = (int)(gPixelsCount[i] * 1.0 * histogramHeight / maxG);
      int bHeight = (int)(bPixelsCount[i] * 1.0 * histogramHeight / maxB);

      g.setColor(Color.WHITE);
      g.fillRect(i, histogramHeight - wHeight, 1, wHeight);
      g.setColor(Color.RED);
      g.fillRect(i, histogramHeight * 2 - rHeight, 1, rHeight);
      g.setColor(Color.GREEN);
      g.fillRect(i, histogramHeight * 3 - gHeight, 1, gHeight);
      g.setColor(Color.BLUE);
      g.fillRect(i, histogramHeight * 4 - bHeight, 1, bHeight);
    }
  }
}
