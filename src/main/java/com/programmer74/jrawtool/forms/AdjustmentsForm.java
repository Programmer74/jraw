package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.components.DisplayingSlider;
import com.programmer74.jrawtool.components.DoubleImageComponent;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdjustmentsForm extends JFrame {

  private DoubleImageComponent doubleImageComponent;
  private DoubleImage doubleImage;

  private DisplayingSlider redsSlider;
  private DisplayingSlider greensSlider;
  private DisplayingSlider bluesSlider;

  private Checkbox chbCurvesEnabled;

  double wbRedsSliderDefVal = 0;
  double wbGreensSliderDefVal = 0;
  double wbBluesSliderDefVal = 0;

  public AdjustmentsForm(final DoubleImageComponent doubleImageComponent,
      final DoubleImage doubleImage) {
    super("Adjustments");

    this.doubleImage = doubleImage;
    this.doubleImageComponent = doubleImageComponent;

    DoubleImageDefaultValues defaults = doubleImage.getDefaultValues();

    setLayout(new FlowLayout());

    redsSlider = new DisplayingSlider("Reds", 0.0, 3.0, defaults.getrK());
    greensSlider = new DisplayingSlider("Greens", 0.0, 3.0, defaults.getgK());
    bluesSlider = new DisplayingSlider("Blue", 0.0, 3.0, defaults.getbK());
    setWbSlidersDefVal();

    DisplayingSlider gammaSlider = new DisplayingSlider("Gamma", 0.0, 3.0, defaults.getGamma());
    DisplayingSlider exposureSlider = new DisplayingSlider("Exp", -2.0, 2.0, defaults.getExposure());
    DisplayingSlider brightnessSlider = new DisplayingSlider("Bri", -1.0, 1.0, defaults.getBrigthness());
    DisplayingSlider contrastSlider = new DisplayingSlider("Con", 0.0, 2.0, defaults.getContrast());

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
    add(redsSlider);

    greensSlider.setSliderChangeListener(sliderChangeListeners);
    add(greensSlider);

    bluesSlider.setSliderChangeListener(sliderChangeListeners);
    add(bluesSlider);

    gammaSlider.setSliderChangeListener(sliderChangeListeners);
    add(gammaSlider);

    exposureSlider.setSliderChangeListener(sliderChangeListeners);
    add(exposureSlider);

    brightnessSlider.setSliderChangeListener(sliderChangeListeners);
    add(brightnessSlider);

    contrastSlider.setSliderChangeListener(sliderChangeListeners);
    add(contrastSlider);

    DisplayingSlider wbSlider = new DisplayingSlider("WB", -1.0, 1.0, 0.0);

    wbSlider.setSliderChangeListener(new ChangeListener() {
      @Override public void stateChanged(final ChangeEvent changeEvent) {
        double wb = wbSlider.getValue();
        redsSlider.setValue(getWbRedsSliderDefVal() - wb);
        bluesSlider.setValue(getWbBluesSliderDefVal() + wb);
      }
    });
    add(wbSlider);

    DisplayingSlider tintSlider = new DisplayingSlider("Tint", -1.0, 1.0, 0.0);
    tintSlider.setSliderChangeListener((e) -> {
      double tint = tintSlider.getValue();
      redsSlider.setValue(getWbRedsSliderDefVal() + tint / 3);
      greensSlider.setValue(getWbGreensSliderDefVal() - tint * 2 / 3);
      bluesSlider.setValue(getWbBluesSliderDefVal() + tint / 3);
    });
    add(tintSlider);

    MouseAdapter wbChanger = new MouseAdapter() {
      @Override public void mousePressed(final MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
          e.consume();
          //this is doubleclick handler
          setWbSlidersDefValDefaults();
        } else {
          System.out.println("WB PRESSED");
          setWbSlidersDefVal();
          super.mousePressed(e);
        }
      }
    };

    wbSlider.getSlider().addMouseListener(wbChanger);
    tintSlider.getSlider().addMouseListener(wbChanger);

    chbCurvesEnabled = new Checkbox();
    chbCurvesEnabled.setLabel("Enable curves");
    add(chbCurvesEnabled);


    pack();
    setSize(320, 480);

  }

  public void showForm() {
    setVisible(true);
  }

  public DisplayingSlider getRedsSlider() {
    return redsSlider;
  }

  public DisplayingSlider getGreensSlider() {
    return greensSlider;
  }

  public DisplayingSlider getBluesSlider() {
    return bluesSlider;
  }

  public Checkbox getChbCurvesEnabled() {
    return chbCurvesEnabled;
  }

  public double getWbRedsSliderDefVal() {
    return wbRedsSliderDefVal;
  }

  public double getWbGreensSliderDefVal() {
    return wbGreensSliderDefVal;
  }

  public double getWbBluesSliderDefVal() {
    return wbBluesSliderDefVal;
  }

  public void setWbSlidersDefVal() {
    this.wbRedsSliderDefVal = redsSlider.getValue();
    this.wbGreensSliderDefVal = greensSlider.getValue();
    this.wbBluesSliderDefVal = bluesSlider.getValue();
  }

  public void setWbSlidersDefValDefaults() {
    this.wbRedsSliderDefVal = redsSlider.getDefaultValue();
    this.wbGreensSliderDefVal = greensSlider.getDefaultValue();
    this.wbBluesSliderDefVal = bluesSlider.getDefaultValue();
  }
}
