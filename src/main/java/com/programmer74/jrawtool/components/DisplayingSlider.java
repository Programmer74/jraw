package com.programmer74.jrawtool.components;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DisplayingSlider extends JPanel {

  private final double min, max, defaultValue;
  private JSlider slider;
  private JLabel lblValue;
  private JLabel lblName;
  private ChangeListener sliderChangeListener;

  public DisplayingSlider(final String description, final Double min, final Double max, final Double defaultValue) {
    this.min = min;
    this.max = max;
    this.defaultValue = defaultValue;

    int minInt = (int)(min * 1000);
    int maxInt = (int)(max * 1000);

    this.lblName = new JLabel();
    this.lblName.setText(description);

    this.lblValue = new JLabel();
    this.lblValue.setText(defaultValue.toString());

    this.slider = new JSlider(minInt, maxInt);
    setValue(defaultValue);

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
    this.add(slider, BorderLayout.CENTER);
    this.add(lblValue, BorderLayout.EAST);

    lblName.setPreferredSize(new Dimension(75, 30));
    lblName.setMinimumSize(lblName.getPreferredSize());
    lblName.setMaximumSize(lblName.getPreferredSize());
    lblValue.setPreferredSize(new Dimension(60, 30));
    lblValue.setMinimumSize(lblValue.getPreferredSize());
    lblValue.setMaximumSize(lblValue.getPreferredSize());
    slider.setPreferredSize(new Dimension(110, 30));
    slider.setMinimumSize(slider.getPreferredSize());
    slider.setMaximumSize(slider.getPreferredSize());
    this.setSize(250, 50);
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

  public double getDefaultValue() {
    return defaultValue;
  }

  public JSlider getSlider() {
    return slider;
  }
}
