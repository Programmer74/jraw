package com.programmer74.jrawtool;

import com.programmer74.jrawtool.components.ImageViewer;
import com.programmer74.jrawtool.converters.GenericConverter;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.forms.AdjustmentsForm;
import com.programmer74.jrawtool.forms.HistogramForm;
import com.programmer74.jrawtool.forms.MainForm;
import com.programmer74.jrawtool.forms.PictureBrowserForm;
import com.programmer74.jrawtool.forms.PreviewForm;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Application {

  private PreviewForm previewForm = null;
  private AdjustmentsForm adjustmentsForm = null;
  private HistogramForm histogramForm = null;
  private MainForm mainForm;
  private PictureBrowserForm pictureBrowserForm;

  public Application() {
    mainForm = new MainForm(null, this);
    mainForm.showForm();
    mainForm.setLocationRelativeTo(null);
    pictureBrowserForm = new PictureBrowserForm(this, mainForm.getMdiPane());
    mainForm.getMdiPane().add(pictureBrowserForm);
    pictureBrowserForm.showForm();
  }

  public void closeApplication() {
    previewForm.setVisible(false);
    mainForm.remove(previewForm);
    previewForm.dispose();
    adjustmentsForm.setVisible(false);
    mainForm.remove(adjustmentsForm);
    adjustmentsForm.dispose();
    histogramForm.setVisible(false);
    mainForm.remove(histogramForm);
    histogramForm.dispose();
  }

  public void openImageBrowser() {
//    mainForm.getMdiPane().add(pictureBrowserForm);
    pictureBrowserForm.showForm();
  }

  public void loadApplication(String filename) {

    if (previewForm != null) {
      closeApplication();
    }

    DoubleImage doubleImage = GenericConverter.loadPicture(filename);
    if (doubleImage == null) {
      System.err.println("Seems that the filename is unsupported.");
      return;
    }
    ImageViewer imageViewer = new ImageViewer(doubleImage);

    final JDesktopPane parentPane = mainForm.getMdiPane();

    pictureBrowserForm.setVisible(false);

    previewForm = new PreviewForm(this, imageViewer, filename, parentPane);
    histogramForm = new HistogramForm(doubleImage, parentPane);
    adjustmentsForm = new AdjustmentsForm(
        imageViewer, doubleImage, histogramForm.getHistogramComponent(), parentPane);
    mainForm.setDoubleImage(doubleImage);

    parentPane.add(histogramForm);
    parentPane.add(adjustmentsForm);
    parentPane.add(previewForm);

    imageViewer.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == 2) {
          //this is middleclick handler
          int onImageX = imageViewer.getOnImageX(e.getX());
          int onImageY = imageViewer.getOnImageY(e.getY());
          System.out.println("onImageCursor at " + onImageX + " : " + onImageY);

          double[] pixel = doubleImage.getPixels()[onImageX][onImageY];
          double r = pixel[0];
          double g = pixel[1];
          double b = pixel[2];
          double max = Math.max(r, Math.max(g, b));
          double rk = max / r;
          double gk = max / g;
          double bk = max / b;

          adjustmentsForm.getRedsSlider().setValue(rk);
          adjustmentsForm.getGreensSlider().setValue(gk);
          adjustmentsForm.getBluesSlider().setValue(bk);
        }
      }
    });

    imageViewer.setAfterPaintCallback(e -> {
      //System.out.println("I was painted");
    });

    adjustmentsForm.showForm();
    histogramForm.showForm();
    previewForm.showForm();
    adjustmentsForm.autoSetImageParamsForRawFootage();
  }

  public static void main(String[] args) {
    Application application = new Application();
    if (args.length >= 1) {
      application.loadApplication(args[0]);
    }
  }
}