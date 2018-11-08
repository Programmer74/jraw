package com.programmer74.jrawtool.components;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class DisplayingSlider extends JPanel {

  private final double min, max;
  private double defaultValue;
  private JSlider slider;
  private JPanel sliderPanel;
  private JLabel lblValue;
  private JLabel lblName;
  private JButton cmdAutoAdjust;
  private ChangeListener sliderChangeListener;
  private Consumer<Component> autoAdjustHandler;

  public DisplayingSlider(final String description, final Double min, final Double max,
                          final Double defaultVal) {
    this(description, min, max, defaultVal, null);
  }

  public DisplayingSlider(final String description, final Double min, final Double max,
                          final Double defaultVal, final Consumer<Component> autoAdjustHandler) {
    this.min = min;
    this.max = max;
    this.defaultValue = defaultVal;
    this.autoAdjustHandler = autoAdjustHandler;

    int minInt = (int)(min * 1000);
    int maxInt = (int)(max * 1000);

    this.lblName = new JLabel();
    this.lblName.setText(description);

    this.lblValue = new JLabel();
    this.lblValue.setText(defaultVal.toString());

    this.sliderPanel = new JPanel();

    this.slider = new JSlider(minInt, maxInt);
    setValue(defaultValue);

    this.cmdAutoAdjust = new JButton("A");

    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (sliderChangeListener != null) {
          Double value = slider.getValue() * 1.0 / 1000;
          lblValue.setText(value.toString());
          sliderChangeListener.stateChanged(e);
        }
      }
    });

    MouseAdapter doubleClickHandler = new MouseAdapter() {
      @Override public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
          e.consume();
          //this is doubleclick handler
          setValue(defaultValue);
        }
      }
    };

    this.addMouseListener(doubleClickHandler);
    this.slider.addMouseListener(doubleClickHandler);

    this.setLayout(new BorderLayout());
    this.add(lblName, BorderLayout.WEST);
    this.sliderPanel.setLayout(new BorderLayout());

    this.add(lblValue, BorderLayout.EAST);

    lblName.setPreferredSize(new Dimension(75, 30));
    lblName.setMinimumSize(lblName.getPreferredSize());
    lblName.setMaximumSize(lblName.getPreferredSize());
    lblValue.setPreferredSize(new Dimension(60, 30));
    lblValue.setMinimumSize(lblValue.getPreferredSize());
    lblValue.setMaximumSize(lblValue.getPreferredSize());
    sliderPanel.setPreferredSize(new Dimension(110, 30));
    sliderPanel.setMinimumSize(sliderPanel.getPreferredSize());
    sliderPanel.setMaximumSize(sliderPanel.getPreferredSize());
    cmdAutoAdjust.setPreferredSize(new Dimension(16, 30));
    cmdAutoAdjust.setMinimumSize(cmdAutoAdjust.getPreferredSize());
    cmdAutoAdjust.setMaximumSize(cmdAutoAdjust.getPreferredSize());
    this.setSize(250, 50);

    setAutoAdjustHandler(autoAdjustHandler);
  }

  public double getValue() {
    return slider.getValue() * 1.0 / 1000;
  }

  public void setValue(Double value) {
    lblValue.setText(value.toString());
    int valInt = (int)(value * 1000);
    this.slider.setValue(valInt);
  }

  public void setSliderChangeListener(final ChangeListener sliderChangeListener) {
    this.sliderChangeListener = sliderChangeListener;
  }

  public Consumer<Component> getAutoAdjustHandler() {
    return autoAdjustHandler != null ? autoAdjustHandler : (x) -> {};
  }

  public void doAutoAdjust() {
    getAutoAdjustHandler().accept(this);
  }

  public void setAutoAdjustHandler(Consumer<Component> handler) {
    this.autoAdjustHandler = handler;
    this.sliderPanel.removeAll();
    this.sliderPanel.add(slider, BorderLayout.CENTER);
    if (this.autoAdjustHandler != null) {
      this.sliderPanel.add(cmdAutoAdjust, BorderLayout.EAST);
      this.cmdAutoAdjust.addActionListener((x) -> autoAdjustHandler.accept(this));
    }
    this.add(sliderPanel, BorderLayout.CENTER);
    this.revalidate();
    this.repaint();
  }

  public double getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(double defaultValue) {
    this.defaultValue = defaultValue;
    setValue(defaultValue);
  }

  public JSlider getSlider() {
    return slider;
  }
}
