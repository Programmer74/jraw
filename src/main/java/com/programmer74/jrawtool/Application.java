package com.programmer74.jrawtool;

import com.programmer74.jrawtool.components.DoubleImageComponent;
import com.programmer74.jrawtool.converters.JpegImage;
import com.programmer74.jrawtool.converters.PGMImage;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import com.programmer74.jrawtool.forms.AdjustmentsForm;
import com.programmer74.jrawtool.forms.HistogramForm;
import com.programmer74.jrawtool.forms.MenuForm;
import com.programmer74.jrawtool.forms.PreviewForm;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Application {

  private PreviewForm previewForm = null;
  private AdjustmentsForm adjustmentsForm = null;
  private HistogramForm histogramForm = null;
  private MenuForm menuForm;

  public Application() {
    menuForm = new MenuForm(null, this);
    menuForm.showForm();
  }

  public void closeApplication() {
    previewForm.setVisible(false);
    previewForm.dispose();
    adjustmentsForm.setVisible(false);
    adjustmentsForm.dispose();
    histogramForm.setVisible(false);
    histogramForm.dispose();
  }

  public void loadApplication(String filename) {

    if (previewForm != null) {
      closeApplication();
    }

    DoubleImage doubleImage;
    if (filename.toLowerCase().endsWith(".jpg")) {
      doubleImage = JpegImage.loadPicture(filename);
    } else {
      doubleImage = PGMImage.loadPicture(filename);
    }
    DoubleImageComponent doubleImageComponent = new DoubleImageComponent(doubleImage);

    previewForm = new PreviewForm(this, doubleImageComponent, filename);
    histogramForm = new HistogramForm(doubleImage);
    adjustmentsForm = new AdjustmentsForm(doubleImageComponent, doubleImage, histogramForm.getHistogramComponent());
    menuForm.setDoubleImage(doubleImage);

    doubleImageComponent.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == 2) {
          //this is middleclick handler
          int onImageX = doubleImageComponent.getOnImageX(e.getX());
          int onImageY = doubleImageComponent.getOnImageY(e.getY());
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

    doubleImageComponent.setAfterPaintCallback(e -> {
      //System.out.println("I was painted");
    });

    previewForm.showForm();
    adjustmentsForm.showForm();
    histogramForm.showForm();
    adjustmentsForm.autoSetImageParamsForRawFootage();
  }

  //./dcraw -4 -D -v -c DSC_1801.NEF > file
  public static void main(String[] args) {
    Application application = new Application();
    if (args.length >= 1) {
      application.loadApplication(args[0]);
    }
  }
}