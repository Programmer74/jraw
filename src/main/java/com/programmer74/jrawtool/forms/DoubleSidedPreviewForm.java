package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.Application;
import com.programmer74.jrawtool.byteimage.ByteImage;
import com.programmer74.jrawtool.components.ImageViewer;
import com.programmer74.jrawtool.components.PaintableImage;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class DoubleSidedPreviewForm extends JInternalFrame {

  private Application parentApp;

  public DoubleSidedPreviewForm(final Application app, final PaintableImage firstImage, final PaintableImage secondImage,
                                final JDesktopPane parentPane) {
    //resizable, closable, maximizable, iconifiable
    super("Chromatic Aberrations Reducer", true, true, true, true);

    this.parentApp = app;
    addInternalFrameListener(new InternalFrameAdapter() {
      @Override public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
//        parentApp.closeApplication();
      }
    });

//    DoubleSidedImageViewer imageViewer = new DoubleSidedImageViewer(firstImage, secondImage);
//    add(imageViewer);

    ByteImage firstImg = new ByteImage(firstImage.burnPreview(800));
    ByteImage secondImg = new ByteImage(secondImage.burnPreview(800));

    ImageViewer firstImageViewer = new ImageViewer(firstImg);
    ImageViewer secondImageViewer = new ImageViewer(secondImg);

    JPanel viewersPanel = new JPanel();
    viewersPanel.setLayout(new BoxLayout(viewersPanel, BoxLayout.LINE_AXIS));
    viewersPanel.add(firstImageViewer);
    viewersPanel.add(secondImageViewer);

    add(viewersPanel);

    pack();
    setLocation((parentPane.getWidth() - getWidth()) / 2,
        (parentPane.getHeight() - getHeight()) / 2);
  }

  public void showForm() {
    setVisible(true);
    moveToFront();
  }
}
