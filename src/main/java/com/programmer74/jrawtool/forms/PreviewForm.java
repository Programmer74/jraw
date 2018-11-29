package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.Application;
import com.programmer74.jrawtool.components.ImageViewer;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class PreviewForm extends JInternalFrame {

  private Application parentApp;

  private ImageViewer imageViewer;

  public PreviewForm(final Application app, final ImageViewer imageViewer,
      final String filename, final JDesktopPane parentPane) {
    //resizable, closable, maximizable, iconifiable
    super("Image: " + filename, true, true, true, true);

    this.parentApp = app;
    addInternalFrameListener(new InternalFrameAdapter() {
      @Override public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
        parentApp.closeApplication();
      }
    });

    this.imageViewer = imageViewer;

    add(imageViewer);

    pack();
    setLocation((parentPane.getWidth() - getWidth()) / 2,
        (parentPane.getHeight() - getHeight()) / 2);
  }

  public void showForm() {
    setVisible(true);
    moveToFront();
  }

  public ImageViewer getImageViewer() {
    return imageViewer;
  }
}
