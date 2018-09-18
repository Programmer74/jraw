package com.programmer74.jrawtool;

import com.programmer74.jrawtool.components.DisplayingSlider;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.components.DoubleImageComponent;
import com.programmer74.jrawtool.pgmimage.PGMImage;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main{

  //./dcraw -4 -D -v -c DSC_1801.NEF > file
  public static void main(String[] args) {

    String filename = args[0];

    DoubleImage doubleImage = PGMImage.loadPicture(filename);
    DoubleImageComponent doubleImageComponent = new DoubleImageComponent(doubleImage);

    JFrame f = new JFrame("Image: " + filename);
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    f.add(doubleImageComponent);
    f.pack();
    f.setVisible(true);

    JFrame f2 = new JFrame("Settings");
    f2.setLayout(new FlowLayout());

    DisplayingSlider redsSlider = new DisplayingSlider("Reds", 0.0, 3.0, 2.17);
    DisplayingSlider greensSlider = new DisplayingSlider("Greens", 0.0, 3.0, 1.0);
    DisplayingSlider bluesSlider = new DisplayingSlider("Blue", 0.0, 3.0, 1.163);


    DisplayingSlider gammaSlider = new DisplayingSlider("Gamma", 1.0, 3.0, 2.2222);
    DisplayingSlider exposureSlider = new DisplayingSlider("Exp", -2.0, 2.0, 0.0);
    DisplayingSlider brightnessSlider = new DisplayingSlider("Bri", -1.0, 1.0, 0.0);
    DisplayingSlider contrastSlider = new DisplayingSlider("Con", 0.0, 2.0, 1.0);


    ChangeListener sliderChangeListeners = new ChangeListener() {
      @Override public void stateChanged(final ChangeEvent changeEvent) {
        double rk = redsSlider.getValue();
        double gk = greensSlider.getValue();
        double bk = bluesSlider.getValue();

        double gamma = gammaSlider.getValue();
        double exp = exposureSlider.getValue();
        double bri = brightnessSlider.getValue();
        double con = contrastSlider.getValue();

        doubleImage.setWhiteBalance(rk, gk, bk);
        doubleImage.setGamma(gamma);
        doubleImage.setExposureStop(exp);
        doubleImage.setBrightness(bri);
        doubleImage.setContrast(con);
        doubleImageComponent.repaint();
      }
    };

    redsSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(redsSlider);

    greensSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(greensSlider);

    bluesSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(bluesSlider);

    gammaSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(gammaSlider);

    exposureSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(exposureSlider);

    brightnessSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(brightnessSlider);

    contrastSlider.setSliderChangeListener(sliderChangeListeners);
    f2.add(contrastSlider);

    f2.setSize(320, 480);
    f2.setVisible(true);

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

          redsSlider.setValue(rk);
          greensSlider.setValue(gk);
          bluesSlider.setValue(bk);
        }
      }
    });

    doubleImageComponent.setAfterPaintCallback(e -> {
      //System.out.println("I was painted");
    });
  }
}