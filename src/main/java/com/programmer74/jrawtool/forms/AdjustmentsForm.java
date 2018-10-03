package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.components.DisplayingSlider;
import com.programmer74.jrawtool.components.DoubleImageComponent;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class AdjustmentsForm extends JFrame {

  private DoubleImageComponent doubleImageComponent;
  private DoubleImage doubleImage;

  private DisplayingSlider redsSlider;
  private DisplayingSlider greensSlider;
  private DisplayingSlider bluesSlider;

  private Checkbox chbCurvesEnabled;

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
    wbSlider.setSliderChangeListener((e) -> {
      double wb = wbSlider.getValue();
      redsSlider.setValue(redsSlider.getDefaultValue() - wb);
      bluesSlider.setValue(bluesSlider.getDefaultValue() + wb);
    });
    add(wbSlider);

    DisplayingSlider tintSlider = new DisplayingSlider("Tint", -1.0, 1.0, 0.0);
    tintSlider.setSliderChangeListener((e) -> {
      double tint = tintSlider.getValue();
      redsSlider.setValue(redsSlider.getDefaultValue() + tint / 3);
      greensSlider.setValue(greensSlider.getDefaultValue() - tint * 2 / 3);
      bluesSlider.setValue(bluesSlider.getDefaultValue() + tint / 3);
    });
    add(tintSlider);

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
}
