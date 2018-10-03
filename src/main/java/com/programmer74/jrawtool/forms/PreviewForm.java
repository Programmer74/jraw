package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.Application;
import com.programmer74.jrawtool.components.DoubleImageComponent;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PreviewForm extends JFrame {

  private Application parentApp;

  public PreviewForm(final Application app, final DoubleImageComponent doubleImageComponent, final String filename) {
    super("Image: " + filename);

    this.parentApp = app;
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        parentApp.closeApplication();
      }
    });

    add(doubleImageComponent);
    pack();
  }

  public void showForm() {
    setVisible(true);
  }
}
