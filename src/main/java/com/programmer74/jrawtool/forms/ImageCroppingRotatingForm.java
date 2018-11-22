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
    viewer = new ImageRotatingCroppingViewer(doubleImage);
    imagePane.add(viewer);

    controlsPane = new JPanel();
    DisplayingSlider dsAngle = new DisplayingSlider("Angle", -180.0, 180.0, 0.0);
    dsAngle.setSliderChangeListener((e) -> viewer.setAngleDegrees(dsAngle.getValue()));
    controlsPane.add(dsAngle);

    mainPane = new JPanel();
    mainPane.setLayout(new BorderLayout());

    mainPane.add(controlsPane, BorderLayout.NORTH);
    mainPane.add(imagePane, BorderLayout.CENTER);
    add(mainPane);

    pack();
    setSize(320, 240);
    setLocation((parentPane.getWidth() - getWidth()) / 2,
        (parentPane.getHeight() - getHeight()) / 2);
  }

  public void showForm() {
    setVisible(true);
  }
}
