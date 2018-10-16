package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.Application;
import com.programmer74.jrawtool.components.DoubleImageComponent;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class PreviewForm extends JInternalFrame {

  private Application parentApp;

  public PreviewForm(final Application app, final DoubleImageComponent doubleImageComponent,
      final String filename, final JDesktopPane parentPane) {
    //resizable, closable, maximizable, iconifiable
    super("Image: " + filename, true, true, true, true);

    this.parentApp = app;
    addInternalFrameListener(new InternalFrameAdapter() {
      @Override public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
        parentApp.closeApplication();
      }
    });

    add(doubleImageComponent);

    pack();
    setLocation((parentPane.getWidth() - getWidth()) / 2,
        (parentPane.getHeight() - getHeight()) / 2);
  }

  public void showForm() {
    setVisible(true);
    moveToFront();
  }
}
