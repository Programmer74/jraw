package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.components.CurvesComponent;
import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import javax.swing.*;

public class CurvesForm extends JFrame {

  private HistogramComponent histogramComponent;
  private CurvesComponent curvesComponent;

  public CurvesForm(final DoubleImage doubleImage,
      final HistogramComponent histogramComponent,
      final AdjustmentsForm adjustmentsForm,
      final PreviewForm previewForm) {
    super("Curves");

    this.histogramComponent = histogramComponent;

    curvesComponent = new CurvesComponent(histogramComponent);
    curvesComponent.setOnChangeCallback((e) -> {
      if (adjustmentsForm.getChbCurvesEnabled().getState()) {
        doubleImage.setCustomPixelConverter(curvesComponent.getPixelConverter());
      } else {
        doubleImage.setDefaultPixelConverter();
      }
      previewForm.repaint();
    });

    adjustmentsForm.getChbCurvesEnabled().addItemListener((e) -> {
      curvesComponent.getOnChangeCallback().accept(0);
    });

    add(curvesComponent);
    setSize(256, 286);
  }

  public void showForm() {
    setVisible(true);
  }
}
