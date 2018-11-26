package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.Application;
import com.programmer74.jrawtool.components.DisplayingSlider;
import com.programmer74.jrawtool.components.ImageRotatingCroppingViewer;
import com.programmer74.jrawtool.components.ImageViewer;
import com.programmer74.jrawtool.doubleimage.DoubleImage;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

public class ImageCroppingRotatingForm extends JInternalFrame {

  private Application parentApp;

  private JPanel mainPane;
  private JPanel controlsPane;
  private JPanel imagePane;

  private ImageRotatingCroppingViewer viewer;

  public ImageCroppingRotatingForm(final Application app, final DoubleImage doubleImage,
                                   final JDesktopPane parentPane) {
    //resizable, closable, maximizable, iconifiable
    super("Rotate", true, true, true, true);

    this.parentApp = app;
    addInternalFrameListener(new InternalFrameAdapter() {
      @Override public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {

      }
    });

    imagePane = new JPanel();
    viewer = new ImageRotatingCroppingViewer(doubleImage.getOriginalImage());
    imagePane.add(viewer);

    controlsPane = new JPanel();
    DisplayingSlider dsAngle = new DisplayingSlider("Angle", -180.0, 180.0, 0.0);
    dsAngle.setSliderChangeListener((e) -> viewer.setAngleDegrees(dsAngle.getValue()));
    controlsPane.add(dsAngle);

    String[] ratios = { "3x2", "4x3", "16x9", "2x3", "3x4", "9x16" };

    JComboBox ratiosList = new JComboBox(ratios);
    ratiosList.setSelectedIndex(0);
    ratiosList.addActionListener((e) -> {
      String str = ratiosList.getSelectedItem().toString();
      int crossIndex = str.indexOf('x');
      String x = ratiosList.getSelectedItem().toString().substring(0, crossIndex);
      String y = ratiosList.getSelectedItem().toString().substring(crossIndex + 1);
      viewer.setCropRatio(Integer.parseInt(x), Integer.parseInt(y));
    });
    controlsPane.add(ratiosList);

    JButton cmdApply = new JButton("Apply");
    cmdApply.addActionListener((e) -> {
      BufferedImage rotatedAndCroppedImage = viewer.getRotatedAndCroppedImage(
          System.out::println
      );
      parentApp.loadApplication(rotatedAndCroppedImage);
    });
    controlsPane.add(cmdApply);

    mainPane = new JPanel();
    mainPane.setLayout(new BorderLayout());

    mainPane.add(controlsPane, BorderLayout.NORTH);
    mainPane.add(imagePane, BorderLayout.CENTER);
    add(mainPane);

    pack();
    setSize(640, 480);
    setLocation((parentPane.getWidth() - getWidth()) / 2,
        (parentPane.getHeight() - getHeight()) / 2);
  }

  public void showForm() {
    setVisible(true);
  }
}
