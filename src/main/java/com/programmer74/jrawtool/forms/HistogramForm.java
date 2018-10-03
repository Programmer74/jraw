package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import javax.swing.*;

public class HistogramForm extends JFrame {

  private HistogramComponent histogramComponent;

  public HistogramForm(final DoubleImage doubleImage) {
    super("Histogram");

    histogramComponent = new HistogramComponent(doubleImage);

    add(histogramComponent);
    pack();
    setSize(256, 542);
  }

  public void showForm() {
    setVisible(true);
  }

  public HistogramComponent getHistogramComponent() {
    return histogramComponent;
  }
}
