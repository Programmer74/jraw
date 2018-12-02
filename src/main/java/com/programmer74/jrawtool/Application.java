package com.programmer74.jrawtool;

import com.programmer74.jrawtool.components.ImageViewer;
import com.programmer74.jrawtool.components.PaintableImage;
import com.programmer74.jrawtool.converters.GenericConverter;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.forms.*;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class Application {

  private PreviewForm previewForm = null;
  private AdjustmentsForm adjustmentsForm = null;
  private HistogramForm histogramForm = null;
  private ImageCroppingRotatingForm rotatingForm = null;
  private DoubleSidedPreviewForm chromaticAberrationsReducerForm = null;
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
    rotatingForm.setVisible(false);
    mainForm.remove(rotatingForm);
    rotatingForm.dispose();
    if (chromaticAberrationsReducerForm != null) {
      chromaticAberrationsReducerForm.setVisible(false);
      mainForm.remove(chromaticAberrationsReducerForm);
      chromaticAberrationsReducerForm.dispose();
    }
  }

  public void openImageBrowser() {
//    mainForm.getMdiPane().add(pictureBrowserForm);
    pictureBrowserForm.showForm();
  }

  public void openCropForm() {
//    mainForm.getMdiPane().add(rotatingForm);
    rotatingForm.showForm();
  }

  public void openChromaticAberrationsReducerForm() {
//    mainForm.getMdiPane().add(rotatingForm);
    if (chromaticAberrationsReducerForm == null) {
      PaintableImage img1 = previewForm.getImageViewer().getPaintableImage();
      chromaticAberrationsReducerForm = new DoubleSidedPreviewForm(this, img1, img1, mainForm.getMdiPane());
      mainForm.getMdiPane().add(chromaticAberrationsReducerForm);
    }
    chromaticAberrationsReducerForm.showForm();
  }

  public void loadApplication(final String filename) {
    Thread t = new Thread(() -> {
      loadApplicationBlocking(filename, null);
    });
    t.start();
  }

  public void loadApplication(final DoubleImage doubleImage) {
    Thread t = new Thread(() -> {
      loadApplicationBlocking(null, doubleImage);
    });
    t.start();
  }

  public void loadApplicationBlocking(String filename, DoubleImage doubleImageLoadedFrom) {

    if (previewForm != null) {
      closeApplication();
    }

    InfoDialog infoDialog = new InfoDialog(mainForm);
    infoDialog.showDialog("Opening file");

    DoubleImage doubleImageC = null;
    if (filename != null) {
      infoDialog.appendText("Trying to open " + filename);
      doubleImageC = GenericConverter.loadPicture(filename,
          (status) -> {
            //System.out.println(status);
            infoDialog.appendText(status);
          });
      infoDialog.hideDialog();
    } else {
      doubleImageC = doubleImageLoadedFrom;
      doubleImageC.setOriginalImage(doubleImageLoadedFrom.getBufferedImage());
      infoDialog.hideDialog();
    }

    if (doubleImageC == null) {
      System.err.println("Seems that the filename is unsupported.");
      return;
    }

    final DoubleImage doubleImage = doubleImageC;
    ImageViewer imageViewer = new ImageViewer(doubleImage);

    final JDesktopPane parentPane = mainForm.getMdiPane();

    pictureBrowserForm.setVisible(false);

    previewForm = new PreviewForm(this, imageViewer, filename, parentPane);
    histogramForm = new HistogramForm(doubleImage, parentPane);
    adjustmentsForm = new AdjustmentsForm(
        imageViewer, doubleImage, histogramForm.getHistogramComponent(), parentPane);
    rotatingForm = new ImageCroppingRotatingForm(this, doubleImage, parentPane);

    mainForm.setDoubleImage(doubleImage);

    parentPane.add(histogramForm);
    parentPane.add(adjustmentsForm);
    parentPane.add(previewForm);
    parentPane.add(rotatingForm);


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