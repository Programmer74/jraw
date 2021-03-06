package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import javax.swing.*;

public class HistogramForm extends JInternalFrame {

  private HistogramComponent histogramComponent;

  public HistogramForm(final DoubleImage doubleImage, final JDesktopPane parentPane) {
    //resizable, closable, maximizable, iconifiable
    super("Histogram", false, false, false, true);

    histogramComponent = new HistogramComponent();
    doubleImage.setHistogramComponent(histogramComponent);

    add(histogramComponent);
    pack();
    setSize(256, 542);
    setLocation(parentPane.getWidth() - getWidth(), 0);
  }

  public void showForm() {
    setVisible(true);
  }

  public HistogramComponent getHistogramComponent() {
    return histogramComponent;
  }
}
