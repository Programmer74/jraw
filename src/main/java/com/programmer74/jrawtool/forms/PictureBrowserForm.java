package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.Application;
import com.programmer74.jrawtool.byteimage.ByteImage;
import com.programmer74.jrawtool.components.FileTreeComponent;
import com.programmer74.jrawtool.components.ImageRollComponent;
import com.programmer74.jrawtool.components.ImageViewer;
import com.programmer74.jrawtool.converters.GenericConverter;
import com.programmer74.jrawtool.converters.RawToPgmConverter;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PictureBrowserForm extends JInternalFrame {
  private Application parentApp;
  private FileTreeComponent fileTreeComponent;
  private JPanel imageViewerPanel;
  private ImageRollComponent imageRoll;
  private JSplitPane imageViewerSplitPanel;
  private JSplitPane bodySplitPanel;

  private final List<String> extensionsList = new ArrayList<>();

  public PictureBrowserForm(final Application app, final JDesktopPane parentPane) {
    //resizable, closable, maximizable, iconifiable
    super("Picker", true, true, true, true);

    this.parentApp = app;

    extensionsList.add("jpg");
    extensionsList.add("png");
    extensionsList.add("bmp");
    extensionsList.add("nef");
    extensionsList.add("cr2");

    fileTreeComponent = new FileTreeComponent(new File("/home/"), extensionsList, true);
    fileTreeComponent.setSelectedFileChanged((e) -> {
      if (!e.isDirectory()) {
        handleFileClicked(e);
      } else {
        handleDirClicked(e);
      }
    });

    imageViewerPanel = new JPanel();
    imageViewerPanel.setLayout(new BorderLayout());
    imageViewerPanel.setMinimumSize(new Dimension(600, 400));
    imageViewerPanel.setPreferredSize(new Dimension(600, 400));

    imageRoll = new ImageRollComponent();
    imageRoll.setMinimumSize(new Dimension(600, 200));
    imageRoll.setPreferredSize(new Dimension(600, 200));
    imageRoll.setSelectedFileChanged((e) -> {
      handleFileClicked(e);
    });
    imageRoll.setSelectedFileAccepted((e) -> {
      parentApp.loadApplication(e.getAbsolutePath());
      setVisible(false);
    });

    imageViewerSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imageViewerPanel, imageRoll);
    imageViewerSplitPanel.setOneTouchExpandable(true);
    imageViewerSplitPanel.setContinuousLayout(true);

    bodySplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTreeComponent, imageViewerSplitPanel);
    bodySplitPanel.setOneTouchExpandable(true);
    bodySplitPanel.setContinuousLayout(true);

    add(bodySplitPanel);
    pack();
    setLocation((parentPane.getWidth() - getWidth()) / 2,
        (parentPane.getHeight() - getHeight()) / 2);

    setDefaultCloseOperation(HIDE_ON_CLOSE);
  }

  public void showForm() {
    setVisible(true);
    moveToFront();
  }

  private void handleFileClicked(File file) {
    this.setTitle("Picker: " + file.getAbsolutePath());

    imageViewerPanel.removeAll();
    imageViewerPanel.revalidate();
    imageViewerPanel.repaint();

    ByteImage image = GenericConverter.loadPreview(file.getAbsolutePath());
    if (image == null) {
      return;
    }

    ImageViewer imageViewer = new ImageViewer(image);
    imageViewer.setCustomPaints((e) -> {
      doCustomPaints(e, file);
    });

    imageViewerPanel.add(imageViewer, BorderLayout.CENTER);
    imageViewerPanel.revalidate();
    imageViewerPanel.repaint();
  }

  private void doCustomPaints(Graphics g, File f) {

    g.setColor(new Color(0, 0, 0, 128));
    g.fillRect(24, 0, 500, 150);

    Graphics2D g2d = (Graphics2D) g;
    g2d.setFont(new Font("Serif", Font.BOLD, 24));
    g2d.setColor(Color.WHITE);
    g2d.drawString(f.getName(), 24, 24);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("Serif", Font.BOLD, 16));

    if (f.getName().toLowerCase().endsWith(".nef") || f.getName().toLowerCase().endsWith(".cr2")) {
      String rawInfo = RawToPgmConverter.extractRawInformationFromFile(f.getAbsolutePath());

      int i = 0;
      for (String line : rawInfo.split(System.getProperty("line.separator"))) {
        g2d.drawString(line, 24, 40 + i * 15);
        i++;
        if (i > 7) break;
      }
    } else {
      g2d.drawString("File: " + f.getAbsolutePath(), 24, 55);
    }
  }

  private void handleDirClicked(File file) {
    this.setTitle("Picker");

    imageViewerPanel.removeAll();
    imageViewerPanel.revalidate();
    imageViewerPanel.repaint();

    List<File> filesToShow = new ArrayList<>();

    for (File f : file.listFiles()) {

      String extension = GenericConverter.getFileExtension(f.getName());
      if (extensionsList.contains(extension)) {
        filesToShow.add(f);
      }
    }
    imageRoll.setFiles(filesToShow);
  }
}
