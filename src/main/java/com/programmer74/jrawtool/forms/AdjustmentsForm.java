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
import java.awt.event.ActionEvent;
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

  private JButton cmdAutoWB;
  private JButton cmdAutoExposure;
  private JButton cmdAutoBC;

  public AdjustmentsForm(final DoubleImageComponent doubleImageComponent,
      final DoubleImage doubleImage, final HistogramComponent histogramComponent) {
    super("Adjustments");

    this.doubleImage = doubleImage;
    this.doubleImageComponent = doubleImageComponent;

    DoubleImageDefaultValues defaults = doubleImage.getDefaultValues();

    setLayout(new FlowLayout());

    setupColorsPanel(doubleImageComponent, doubleImage, defaults, histogramComponent);

    setupCurvesPanel(doubleImageComponent, doubleImage, histogramComponent);

    setupFiltersPanel(doubleImageComponent, doubleImage);


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

  private void setupFiltersPanel(DoubleImageComponent doubleImageComponent, DoubleImage doubleImage) {
    filtersPanel = new JPanel();
    filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.PAGE_AXIS));

    chbConvolutionsEnabled = new Checkbox();
    chbConvolutionsEnabled.setLabel("Enable convolutions");

    DisplayingSlider sharpeningSlider = new DisplayingSlider("Sharp", 0.0, 1.0, 0.0);
    DisplayingSlider gaussianBlurStSlider = new DisplayingSlider("GBSt", 0.0, 2.0, 0.0);
    DisplayingSlider gaussianBlurRadSlider = new DisplayingSlider("GBRd", 1.0, 9.0, 0.5);
    DisplayingSlider unsharpMaskingStSlider = new DisplayingSlider("USMSt", 0.0, 2.0, 0.0);
    DisplayingSlider unsharpMaskingRadSlider = new DisplayingSlider("USMRd", 1.0, 9.0, 0.5);


    Runnable updateMatrix = new Runnable() {
      @Override
      public void run() {
        if (chbConvolutionsEnabled.getState()) {
          doubleImage.setCustomConvolutionKernel(
              DoubleImageKernelMatrixGenerator.buildConvolutionMatrix(
                sharpeningSlider.getValue(), gaussianBlurStSlider.getValue(), (int)(gaussianBlurRadSlider.getValue()),
                  unsharpMaskingStSlider.getValue(), (int)(unsharpMaskingRadSlider.getValue())
              )
          );
        } else {
          doubleImage.setCustomConvolutionKernel(new double[][]{{1}});
        }
        doubleImageComponent.repaint();
      }
    };


    chbConvolutionsEnabled.addItemListener((e) -> { updateMatrix.run(); });
    filtersPanel.add(chbConvolutionsEnabled);


    sharpeningSlider.setSliderChangeListener((e) -> { updateMatrix.run(); });
    gaussianBlurStSlider.setSliderChangeListener((e) -> { updateMatrix.run(); });
    gaussianBlurRadSlider.setSliderChangeListener((e) -> { updateMatrix.run(); });
    unsharpMaskingStSlider.setSliderChangeListener((e) -> { updateMatrix.run(); });
    unsharpMaskingRadSlider.setSliderChangeListener((e) -> { updateMatrix.run(); });

    filtersPanel.add(sharpeningSlider);
    filtersPanel.add(gaussianBlurStSlider);
    filtersPanel.add(gaussianBlurRadSlider);
    filtersPanel.add(unsharpMaskingStSlider);
    filtersPanel.add(unsharpMaskingRadSlider);
  }

  private void setupCurvesPanel(DoubleImageComponent doubleImageComponent, DoubleImage doubleImage, HistogramComponent histogramComponent) {
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
  }

  private void setupColorsPanel(DoubleImageComponent doubleImageComponent,
      DoubleImage doubleImage,
      DoubleImageDefaultValues defaults,
      HistogramComponent histogramComponent) {
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

    cmdAutoWB = new JButton("AutoWB");
    cmdAutoWB.addActionListener(new AbstractAction() {
      @Override public void actionPerformed(final ActionEvent actionEvent) {
        autoSetColorGainsByAveragingWB(doubleImage);
      }
    });
    colorsPanel.add(cmdAutoWB);

    cmdAutoExposure = new JButton("AutoExposure");
    cmdAutoExposure.addActionListener(new AbstractAction() {
      @Override public void actionPerformed(final ActionEvent actionEvent) {
        autoSetExposureByAdjustingHistogramMax(histogramComponent, exposureSlider);
      }
    });
    colorsPanel.add(cmdAutoExposure);

    cmdAutoBC = new JButton("AutoBC");
    cmdAutoBC.addActionListener(new AbstractAction() {
      @Override public void actionPerformed(final ActionEvent actionEvent) {

      }
    });
    colorsPanel.add(cmdAutoBC);
  }

  private void autoSetExposureByAdjustingHistogramMax(final HistogramComponent histogramComponent,
      final DisplayingSlider exposureSlider) {
    int[] wPixels = histogramComponent.getwPixelsCount();
    int maxIndex = 0;
    int maxValue = 0;
    for (int i = 0; i < wPixels.length; i++) {
      if (wPixels[i] > maxValue) {
        maxValue = wPixels[i];
        maxIndex = i;
      }
    }
    //we assume that histogram peak is at the middle of the image,
    //therefore maxIndex should be 128. if not, we adjust it
    System.out.println("MAX INDEX IS " + maxIndex);
    double diff = 128.0 / (maxIndex * 1.0) - 1;
    exposureSlider.setValue(diff);
  }

  private void autoSetColorGainsByAveragingWB(final DoubleImage doubleImage) {
    double[][][] pixels = doubleImage.getPixels();
    double[] avgs = new double[] {0, 0, 0};

    int step = 10;
    int pixelCount = 0;

    for (int x = 0; x < doubleImage.getWidth(); x += step) {
      for (int y = 0; y < doubleImage.getHeight(); y += step) {
        double[] pixel = pixels[x][y].clone();
//            if ((pixel[0] / 2 > pixel[2]) ||
//                (pixel[2] / 2 > pixel[0])) {
//              //skip pixels that are too different on r/b
//              continue;
//            }
        pixelCount++;
        for (int i = 0; i < 3; i++) {
          avgs[i] += pixel[i];
        }
      }
    }

    for (int i = 0; i < 3; i++) {
      avgs[i] /= pixelCount;
    }

    double r = avgs[0];
    double g = avgs[1];
    double b = avgs[2];
    double max = Math.max(r, Math.max(g, b));
    double rk = max / r;
    double gk = max / g;
    double bk = max / b;

    redsSlider.setValue(rk);
    greensSlider.setValue(gk);
    bluesSlider.setValue(bk);
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
