package com.programmer74.jrawtool;

import com.programmer74.jrawtool.components.CurvesComponent;
import com.programmer74.jrawtool.components.DisplayingSlider;
import com.programmer74.jrawtool.components.DoubleImageComponent;
import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.converters.JpegImage;
import com.programmer74.jrawtool.converters.PGMImage;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;
import com.programmer74.jrawtool.forms.AdjustmentsForm;
import com.programmer74.jrawtool.forms.CurvesForm;
import com.programmer74.jrawtool.forms.HistogramForm;
import com.programmer74.jrawtool.forms.PreviewForm;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main{

  //./dcraw -4 -D -v -c DSC_1801.NEF > file
  public static void main(String[] args) {

    String filename = args[0];

    DoubleImage doubleImage;
    if (filename.toLowerCase().endsWith(".jpg")) {
      doubleImage = JpegImage.loadPicture(filename);
    } else {
      doubleImage = PGMImage.loadPicture(filename);
    }
    DoubleImageComponent doubleImageComponent = new DoubleImageComponent(doubleImage);

    DoubleImageDefaultValues defaults = doubleImage.getDefaultValues();

    PreviewForm previewForm = new PreviewForm(doubleImageComponent, filename);
    AdjustmentsForm adjustmentsForm = new AdjustmentsForm(doubleImageComponent, doubleImage);
    HistogramForm histogramForm = new HistogramForm(doubleImage);
    CurvesForm curvesForm = new CurvesForm(
        doubleImage, histogramForm.getHistogramComponent(), adjustmentsForm, previewForm
    );

    doubleImageComponent.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == 2) {
          //this is middleclick handler
          int onImageX = doubleImageComponent.getOnImageX(e.getX());
          int onImageY = doubleImageComponent.getOnImageY(e.getY());
          System.out.println("onImageCursor at " + onImageX + " : " + onImageY);

          double[] pixel = doubleImage.getPixels()[onImageX][onImageY];
          double r = pixel[0];
          double g = pixel[1];
          double b = pixel[2];
          double max = Math.max(r, Math.max(g, b));
          double rk = max / r;
          double gk = max / g;
          double bk = max / b;

          adjustmentsForm.getRedsSlider().setValue(rk);
          adjustmentsForm.getGreensSlider().setValue(gk);
          adjustmentsForm.getBluesSlider().setValue(bk);
        }
      }
    });

    doubleImageComponent.setAfterPaintCallback(e -> {
      //System.out.println("I was painted");
    });

    previewForm.showForm();
    adjustmentsForm.showForm();
    histogramForm.showForm();
    curvesForm.showForm();
  }
}