package com.programmer74.jrawtool;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main extends Component {

  PGMImage img = new PGMImage();

  // constructor that loads pgm image from a file
  public Main(String filename) {
    img.loadPicture(filename);
  }

  // overrides the paint method of Component class
  public void paint(Graphics g) {
    // simply draw the buffered image
    g.drawImage(img.getImg(), 0, 0, getWidth(), getHeight(), this);
  }
  // overrides the method in Component class, to determine the window size
  public Dimension getPreferredSize() {
    if (img == null) {
      return new Dimension(100, 100);
    } else {
      // make sure the window is not two small to be seen
      return new Dimension(Math.max(100, img.getImg().getWidth(null) / 6),
          Math.max(100, img.getImg().getHeight(null) / 6));
    }
  }


  //./dcraw -4 -D -v -c DSC_1801.NEF > file

  // The main method that will load and process a pgm image, and display the result.
  public static void main(String[] args) {
    // instantiate the PgmImage object according to the
    //  command line argument
    Main img;
    String filename ="default";
    String operation = "none";
   
      filename = args[0];
      img = new Main(filename);

    // set up the GUI for display the PgmImage object
    JFrame f = new JFrame("PGM Image: "+filename+" Image Operation: " + operation);
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.add(img);
    f.pack();
    f.setVisible(true);

    JFrame f2 = new JFrame("Settings");
    f2.setLayout(new FlowLayout());
    JSlider slider = new JSlider(0, 500);
    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        System.out.println(slider.getValue());
        img.img.changeGamma(slider.getValue() * 1.0 / 100);
        img.repaint();
      }
    });
    f2.add(slider);
    f2.setSize(320, 240);
    f2.setVisible(true);
  }
}