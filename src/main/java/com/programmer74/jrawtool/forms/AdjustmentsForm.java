package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.components.CurvesComponent;
import com.programmer74.jrawtool.components.DisplayingSlider;
import com.programmer74.jrawtool.components.DoubleImageComponent;
import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.doubleimage.DoubleImageDefaultValues;
import com.programmer74.jrawtool.doubleimage.DoubleImageKernelMatrixGenerator;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdjustmentsForm extends JFrame {

  private DoubleImageComponent doubleImageComponent;
  private DoubleImage doubleImage;

  private CurvesComponent curvesComponent;

  private DisplayingSlider redsSlider;
  private DisplayingSlider greensSlider;
  private DisplayingSlider bluesSlider;

  private Checkbox chbCurvesEnabled;
  private Checkbox chbConvolutionsEnabled;

  JTabbedPane tabPane;
  private JPanel colorsPanel;
  private JPanel curvesPanel;
  private JPanel filtersPanel;

  double wbRedsSliderDefVal = 0;
  double wbGreensSliderDefVal = 0;
  double wbBluesSliderDefVal = 0;

  public AdjustmentsForm(final DoubleImageComponent doubleImageComponent,
      final DoubleImage doubleImage, final HistogramComponent histogramComponent) {
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

    colorsPanel = new JPanel();
    colorsPanel.setLayout(new BoxLayout(colorsPanel, BoxLayout.PAGE_AXIS));

    redsSlider.setSliderChangeListener(sliderChangeListeners);
    colorsPanel.add(redsSlider);

    greensSlider.setSliderChangeListener(sliderChangeListeners);
    colorsPanel.add(greensSlider);

    bluesSlider.setSliderChangeListener(sliderChangeListeners);
    colorsPanel.add(bluesSlider);

    gammaSlider.setSliderChangeListener(sliderChangeListeners);
    colorsPanel.add(gammaSlider);

    exposureSlider.setSliderChangeListener(sliderChangeListeners);
    colorsPanel.add(exposureSlider);

    brightnessSlider.setSliderChangeListener(sliderChangeListeners);
    colorsPanel.add(brightnessSlider);

    contrastSlider.setSliderChangeListener(sliderChangeListeners);
    colorsPanel.add(contrastSlider);

    DisplayingSlider wbSlider = new DisplayingSlider("WB", -1.0, 1.0, 0.0);

    wbSlider.setSliderChangeListener(new ChangeListener() {
      @Override public void stateChanged(final ChangeEvent changeEvent) {
        double wb = wbSlider.getValue();
        redsSlider.setValue(getWbRedsSliderDefVal() - wb);
        bluesSlider.setValue(getWbBluesSliderDefVal() + wb);
      }
    });
    colorsPanel.add(wbSlider);

    DisplayingSlider tintSlider = new DisplayingSlider("Tint", -1.0, 1.0, 0.0);
    tintSlider.setSliderChangeListener((e) -> {
      double tint = tintSlider.getValue();
      redsSlider.setValue(getWbRedsSliderDefVal() + tint / 3);
      greensSlider.setValue(getWbGreensSliderDefVal() - tint * 2 / 3);
      bluesSlider.setValue(getWbBluesSliderDefVal() + tint / 3);
    });
    colorsPanel.add(tintSlider);

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

    curvesPanel = new JPanel();
    curvesPanel.setLayout(new BoxLayout(curvesPanel, BoxLayout.PAGE_AXIS));

    chbCurvesEnabled = new Checkbox();
    chbCurvesEnabled.setLabel("Enable curves");
    curvesPanel.add(chbCurvesEnabled);

    curvesComponent = new CurvesComponent(histogramComponent);
    curvesComponent.setOnChangeCallback((e) -> {
      if (chbCurvesEnabled.getState()) {
        doubleImage.setCustomPixelConverter(curvesComponent.getPixelConverter());
      } else {
        doubleImage.setDefaultPixelConverter();
      }
      doubleImageComponent.repaint();
    });
    chbCurvesEnabled.addItemListener((e) -> {
      curvesComponent.getOnChangeCallback().accept(0);
    });
    curvesPanel.add(curvesComponent);

    filtersPanel = new JPanel();

    chbConvolutionsEnabled = new Checkbox();
    chbConvolutionsEnabled.setLabel("Enable convolutions");
    chbConvolutionsEnabled.addItemListener((e) -> {
      if (chbConvolutionsEnabled.getState()) {
        doubleImage.setCustomConvolutionKernel(DoubleImageKernelMatrixGenerator.getMatrix());
      } else {
        doubleImage.setCustomConvolutionKernel(new double[][]{{1}});
      }
      doubleImageComponent.repaint();
    });
    filtersPanel.add(chbConvolutionsEnabled);


    tabPane = new JTabbedPane(JTabbedPane.NORTH);

    // Add tabs with no text
    tabPane.addTab(null, colorsPanel);
    tabPane.addTab(null, curvesPanel);
    tabPane.addTab(null, filtersPanel);

    // Create vertical labels to render tab titles
    JLabel labTab1 = new JLabel("Color");
    tabPane.setTabComponentAt(0, labTab1);

    JLabel labTab2 = new JLabel("Curves");
    tabPane.setTabComponentAt(1, labTab2);

    JLabel labTab3 = new JLabel("Filters");
    tabPane.setTabComponentAt(2, labTab3);

    add(tabPane);

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
