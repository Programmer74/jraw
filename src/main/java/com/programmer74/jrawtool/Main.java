package com.programmer74.jrawtool;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
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
    JSlider slider = new JSlider(0, 4000);
    slider.setValue(2170);

    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        double rk = slider.getValue() * 1.0 / 1000;
        doubleImage.setWhiteBalance(rk, 1.0, 1.163);
        doubleImageComponent.repaint();
      }
    });

    f2.add(slider);
    f2.setSize(320, 240);
    f2.setVisible(true);


    doubleImageComponent.setAfterPaintCallback(e -> {
      //System.out.println("I was painted");
    });
  }
}