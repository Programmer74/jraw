package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.Application;
import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MenuForm extends JFrame {

  private Application parentApp;
  private DoubleImage doubleImage;

  public MenuForm(final DoubleImage doubleImage, final Application application) {
    super("Menu");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    this.doubleImage = doubleImage;
    parentApp = application;

    JMenuBar menubar = new JMenuBar();

    JMenu file = new JMenu("File");
    file.setMnemonic(KeyEvent.VK_F);

    JMenuItem eOpenItem = new JMenuItem("Open");
    eOpenItem.setMnemonic(KeyEvent.VK_O);
    eOpenItem.setToolTipText("Open image");
    eOpenItem.addActionListener((ActionEvent event) -> {
      handleOpen();
    });

    JMenuItem eSaveItem = new JMenuItem("Save");
    eSaveItem.setMnemonic(KeyEvent.VK_S);
    eSaveItem.setToolTipText("Open image");
    eSaveItem.addActionListener((ActionEvent event) -> {
      handleSave();
    });

    JMenuItem eExitItem = new JMenuItem("Exit");
    eExitItem.setMnemonic(KeyEvent.VK_E);
    eExitItem.setToolTipText("Exit application");
    eExitItem.addActionListener((ActionEvent event) -> {
      System.exit(0);
    });

    file.add(eOpenItem);
    file.add(eSaveItem);
    file.add(eExitItem);

    menubar.add(file);

    setJMenuBar(menubar);

    setSize(new Dimension(600, 50));
  }

  public void showForm() {
    setVisible(true);
  }

  private void handleOpen() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    FileFilter imageFilter = new FileNameExtensionFilter(
        "Image files", "pgm", "jpg");
    fileChooser.setFileFilter(imageFilter);
    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      parentApp.loadApplication(selectedFile.getAbsolutePath());
    }
  }
  private void handleSave() {
    if (doubleImage != null) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
      FileFilter imageFilter = new FileNameExtensionFilter(
          "Image files", "jpg");
      fileChooser.setFileFilter(imageFilter);
      int result = fileChooser.showSaveDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();

        try {
          ImageIO.write(doubleImage.getBufferedImage(), "jpg", selectedFile);
          System.out.println("Save OK");
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  public void setDoubleImage(final DoubleImage doubleImage) {
    this.doubleImage = doubleImage;
  }
}
