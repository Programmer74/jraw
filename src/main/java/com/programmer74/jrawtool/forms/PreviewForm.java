package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.components.DoubleImageComponent;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PreviewForm extends JFrame {

  public PreviewForm(final DoubleImageComponent doubleImageComponent, final String filename) {
    super("Image: " + filename);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    add(doubleImageComponent);
    pack();
  }

  public void showForm() {
    setVisible(true);
  }
}
